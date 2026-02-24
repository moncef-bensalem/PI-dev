package controller;

import database.EvaluationDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Evaluation;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListEvaluationsController implements Initializable {

    @FXML
    private FlowPane evaluationsGrid;

    @FXML
    private Label totalEvaluationsLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button sortButton;

    @FXML
    private Button clearFiltersButton;

    @FXML
    private PieChart decisionPieChart;

    @FXML
    private TitledPane pieChartPopup;

    @FXML
    private TitledPane reviewDeadlinePane;

    @FXML
    private DatePicker reviewDeadlinePicker;

    private EvaluationDAO evaluationDAO;
    private DateTimeFormatter dateFormatter;
    private ObservableList<Evaluation> allEvaluations;
    private ObservableList<Evaluation> displayedEvaluations;
    private boolean sortAscending = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationDAO = new EvaluationDAO();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        allEvaluations = FXCollections.observableArrayList();
        displayedEvaluations = FXCollections.observableArrayList();

        setupFilterComboBox();
        setupSearchField();
        setupReviewDeadlinePane();
        loadEvaluations();
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "Tous",
            "Favorables",
            "Défavorables",
            "à revoir"
        ));
        filterComboBox.setValue("Tous");
        filterComboBox.setOnAction(event -> {
            applyFilters();
            updateReviewDeadlineVisibility();
        });
    }

    private void setupReviewDeadlinePane() {
        if (reviewDeadlinePane != null) {
            reviewDeadlinePane.setVisible(false);
            reviewDeadlinePane.setManaged(false);
        }

        if (reviewDeadlinePicker != null) {
            reviewDeadlinePicker.setDayCellFactory(createReviewDeadlineCellFactory());
            reviewDeadlinePicker.setOnAction(event -> {
                LocalDate selectedDate = reviewDeadlinePicker.getValue();
                if (selectedDate != null) {
                    long count = allEvaluations.stream()
                        .filter(e -> e.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.A_REVOIR)
                        .filter(e -> selectedDate.equals(e.getReviewDeadline()))
                        .count();

                    if (count > 0) {
                        showAlert(
                            Alert.AlertType.INFORMATION,
                            "Évaluations à revoir",
                            "Il y a " + count + " évaluation(s) A_REVOIR avec une deadline le "
                                + selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        );
                    } else {
                        showAlert(
                            Alert.AlertType.INFORMATION,
                            "Aucune échéance",
                            "Aucune évaluation A_REVOIR n'a de deadline le "
                                + selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        );
                    }
                }
            });
        }
    }

    private Callback<DatePicker, DateCell> createReviewDeadlineCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (!empty && date != null && hasReviewDeadlineOn(date)) {
                    setStyle("-fx-background-color: #FFE0B2; -fx-border-color: #FB8C00;");
                    setTooltip(new Tooltip("Au moins une évaluation A_REVOIR à revoir ce jour"));
                }
            }
        };
    }

    private boolean hasReviewDeadlineOn(LocalDate date) {
        if (date == null) {
            return false;
        }

        return allEvaluations.stream()
            .anyMatch(e ->
                e.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.A_REVOIR
                    && date.equals(e.getReviewDeadline())
            );
    }

    private void updateReviewDeadlineVisibility() {
        if (reviewDeadlinePane != null && filterComboBox != null) {
            boolean show = "à revoir".equals(filterComboBox.getValue());
            reviewDeadlinePane.setVisible(show);
            reviewDeadlinePane.setManaged(show);
        }
    }

    private boolean matchesDecisionFilter(String filterValue, Evaluation.DecisionPreliminaire decision) {
        return switch (filterValue) {
            case "Favorables" -> decision == Evaluation.DecisionPreliminaire.FAVORABLE;
            case "Défavorables" -> decision == Evaluation.DecisionPreliminaire.DEFAVORABLE;
            case "à revoir" -> decision == Evaluation.DecisionPreliminaire.A_REVOIR;
            default -> false;
        };
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filterValue = filterComboBox.getValue();

        List<Evaluation> filtered = allEvaluations.stream()
            .filter(e -> {
               
                boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(e.getIdEvaluation()).contains(searchText) ||
                    (e.getCommentaireGlobal() != null && e.getCommentaireGlobal().toLowerCase().contains(searchText)) ||
                    String.valueOf(e.getFkEntretienId()).contains(searchText) ||
                    String.valueOf(e.getFkRecruteurId()).contains(searchText);
                
                
                boolean matchesFilter = "Tous".equals(filterValue) || 
                    matchesDecisionFilter(filterValue, e.getDecisionPreliminaire());
                
                return matchesSearch && matchesFilter;
            })
            .collect(Collectors.toList());

        displayedEvaluations.setAll(filtered);
        refreshGrid();
        updatePieChart();
    }

    @FXML
    private void handleSortByDecision() {
        Comparator<Evaluation> comparator = Comparator.comparing(Evaluation::getDecisionPreliminaire);
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        
        List<Evaluation> sorted = displayedEvaluations.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
        
        displayedEvaluations.setAll(sorted);
        sortAscending = !sortAscending;
        refreshGrid();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterComboBox.setValue("Tous");
        displayedEvaluations.setAll(allEvaluations);
        sortAscending = true;
        updateReviewDeadlineVisibility();
        refreshGrid();
        updatePieChart();
    }

    private void refreshGrid() {
        evaluationsGrid.getChildren().clear();
        for (Evaluation evaluation : displayedEvaluations) {
            evaluationsGrid.getChildren().add(createEvaluationCard(evaluation));
        }
        totalEvaluationsLabel.setText("Total : " + displayedEvaluations.size() + " évaluation" + (displayedEvaluations.size() != 1 ? "s" : ""));
    }

    private VBox createEvaluationCard(Evaluation evaluation) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("card", getDecisionCardStyle(evaluation.getDecisionPreliminaire()));
        card.setPadding(new Insets(15));
        card.setPrefWidth(320);
        card.setMinWidth(280);
        card.setMaxWidth(350);
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseClicked(event -> openEvaluationDetails(evaluation));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Évaluation #" + evaluation.getIdEvaluation());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212121;");

        Circle decisionIndicator = new Circle(8);
        decisionIndicator.setFill(getDecisionColor(evaluation.getDecisionPreliminaire()));

        Label decisionLabel = new Label(evaluation.getDecisionPreliminaire().toString());
        decisionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + getDecisionHexColor(evaluation.getDecisionPreliminaire()) + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("button", "button-red");
        deleteButton.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
        deleteButton.setOnAction(event -> {
            event.consume();
            handleDeleteEvaluation(evaluation);
        });
        deleteButton.setOnMouseClicked(event -> event.consume());

        header.getChildren().addAll(idLabel, decisionIndicator, decisionLabel, spacer, deleteButton);

        Label dateLabel = new Label("Créée le : " + evaluation.getDateCreation().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #424242;");

        Label commentLabel = new Label("Commentaire : " + (evaluation.getCommentaireGlobal() != null ? evaluation.getCommentaireGlobal() : "Aucun commentaire"));
        commentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #212121;");
        commentLabel.setWrapText(true);
        commentLabel.setMaxHeight(40);

        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label entretienLabel = new Label("ID Entretien : " + evaluation.getFkEntretienId());
        entretienLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #616161;");

        Label recruteurLabel = new Label("ID Recruteur : " + evaluation.getFkRecruteurId());
        recruteurLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #616161;");

        footer.getChildren().addAll(entretienLabel, recruteurLabel);

        card.getChildren().addAll(header, dateLabel, commentLabel, footer);

        return card;
    }

    private String getDecisionCardStyle(Evaluation.DecisionPreliminaire decision) {
        return switch (decision) {
            case FAVORABLE -> "card-favorable";
            case DEFAVORABLE -> "card-defavorable";
            case A_REVOIR -> "card-arevoir";
        };
    }

    private void openEvaluationDetails(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsEvaluation.fxml"));
            Parent root = loader.load();

            DetailsEvaluationController controller = loader.getController();
            controller.setEvaluation(evaluation);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'Évaluation - #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isDeleted()) {
                loadEvaluations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée avec succès.");
            } else {
                loadEvaluations();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails de l'évaluation : " + e.getMessage());
        }
    }

    private void handleDeleteEvaluation(Evaluation evaluation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer l'Évaluation #" + evaluation.getIdEvaluation());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette évaluation ? Cette action ne peut pas être annulée.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                evaluationDAO.delete(evaluation);
                loadEvaluations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée avec succès.");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Color getDecisionColor(Evaluation.DecisionPreliminaire decision) {
        return switch (decision) {
            case FAVORABLE -> Color.web("#4CAF50");
            case DEFAVORABLE -> Color.web("#F44336");
            case A_REVOIR -> Color.web("#FF9800");
        };
    }

    private String getDecisionHexColor(Evaluation.DecisionPreliminaire decision) {
        return switch (decision) {
            case FAVORABLE -> "#4CAF50";
            case DEFAVORABLE -> "#F44336";
            case A_REVOIR -> "#FF9800";
        };
    }

    private void loadEvaluations() {
        allEvaluations.setAll(evaluationDAO.getAll());
        displayedEvaluations.setAll(allEvaluations);
        refreshGrid();
        updatePieChart();
        if (reviewDeadlinePicker != null) {
            // Force refresh of the calendar cell styles
            reviewDeadlinePicker.setValue(reviewDeadlinePicker.getValue());
        }
    }

    private void updatePieChart() {
        long favorableCount = displayedEvaluations.stream()
            .filter(e -> e.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.FAVORABLE)
            .count();
        long defavorableCount = displayedEvaluations.stream()
            .filter(e -> e.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.DEFAVORABLE)
            .count();
        long arevoirCount = displayedEvaluations.stream()
            .filter(e -> e.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.A_REVOIR)
            .count();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        if (favorableCount > 0) {
            pieChartData.add(new PieChart.Data("Favorable", favorableCount));
        }
        if (defavorableCount > 0) {
            pieChartData.add(new PieChart.Data("Défavorable", defavorableCount));
        }
        if (arevoirCount > 0) {
            pieChartData.add(new PieChart.Data("A Revoir", arevoirCount));
        }

        decisionPieChart.setData(pieChartData);
        decisionPieChart.setTitle("Répartition des Décisions");

        Platform.runLater(() -> {
            for (PieChart.Data data : decisionPieChart.getData()) {
                String color = switch (data.getName()) {
                    case "Favorable" -> "#4CAF50";
                    case "Défavorable" -> "#F44336";
                    case "A Revoir" -> "#FF9800";
                    default -> "#757575";
                };

                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + color + ";");
                }

                for (Node legendItem : decisionPieChart.lookupAll(".chart-legend-item")) {
                    if (legendItem instanceof Label legendLabel && legendLabel.getText().equals(data.getName())) {
                        Node symbol = legendLabel.getGraphic();
                        if (symbol != null) {
                            symbol.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 100%; -fx-padding: 5;");
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void handleAddEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateEvaluation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Créer une Nouvelle Évaluation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadEvaluations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la boîte de dialogue de création d'évaluation : " + e.getMessage());
        }
    }
}
