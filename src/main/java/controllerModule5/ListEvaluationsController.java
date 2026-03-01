package controllerModule5;

import databaseModule5.EvaluationDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import modelModule5.Evaluation;
import modelModule5.ai.AnalysisResult;
import serviceModule5.AiAnalysisTempStorage;
import serviceModule5.AiVideoAnalysisService;
import serviceModule5.AiVideoAnalysisService.AiVideoAnalysisException;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListEvaluationsController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ListEvaluationsController.class.getName());

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
    private Button aiAnalyseButton;

    @FXML
    private PieChart decisionPieChart;

    @FXML
    private TitledPane pieChartPopup;

    @FXML
    private VBox calendarContainer;

    @FXML
    private Label calendarMonthLabel;

    @FXML
    private GridPane calendarGrid;

    private EvaluationDAO evaluationDAO;
    private DateTimeFormatter dateFormatter;
    private ObservableList<Evaluation> allEvaluations;
    private ObservableList<Evaluation> displayedEvaluations;
    private boolean sortAscending = true;
    private LocalDate currentCalendarMonth;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationDAO = new EvaluationDAO();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        allEvaluations = FXCollections.observableArrayList();
        displayedEvaluations = FXCollections.observableArrayList();
        currentCalendarMonth = LocalDate.now().withDayOfMonth(1);

        setupFilterComboBox();
        setupSearchField();
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
        });
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
        renderCalendar();
    }

    @FXML
    private void handleSortByScore() {
        Comparator<Evaluation> comparator = Comparator.comparingDouble(Evaluation::getMoyenneScore);
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
        refreshGrid();
        updatePieChart();
        renderCalendar();
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
        renderCalendar();
    }

    private void renderCalendar() {
        if (calendarGrid == null || calendarMonthLabel == null) {
            return;
        }

        calendarGrid.getChildren().clear();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        calendarMonthLabel.setText(currentCalendarMonth.format(monthFormatter));

        String[] dayNames = {"L", "M", "M", "J", "V", "S", "D"};
        for (int col = 0; col < 7; col++) {
            Label header = new Label(dayNames[col]);
            header.getStyleClass().add("calendar-day-header");
            GridPane.setHgrow(header, Priority.ALWAYS);
            calendarGrid.add(header, col, 0);
        }

        LocalDate firstOfMonth = currentCalendarMonth;
        int lengthOfMonth = firstOfMonth.lengthOfMonth();
        int firstDayCol = firstOfMonth.getDayOfWeek().getValue() - 1;

        int row = 1;
        int col = firstDayCol;

        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = firstOfMonth.withDayOfMonth(day);

            VBox cell = new VBox(2);
            cell.getStyleClass().add("calendar-cell");
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("calendar-day-number");
            cell.getChildren().add(dayLabel);

            List<Evaluation> evalsForDay = allEvaluations.stream()
                .filter(e -> e.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.A_REVOIR)
                .filter(e -> date.equals(e.getReviewDeadline()))
                .collect(Collectors.toList());

            if (!evalsForDay.isEmpty()) {
                String ids = evalsForDay.stream()
                    .map(e -> String.valueOf(e.getFkEntretienId()))
                    .distinct()
                    .collect(Collectors.joining(", "));

                Label idsLabel = new Label(ids);
                idsLabel.getStyleClass().add("calendar-eval-label");
                cell.getChildren().add(idsLabel);
                cell.getStyleClass().add("calendar-cell-has-eval");
            }

            GridPane.setHgrow(cell, Priority.ALWAYS);
            GridPane.setVgrow(cell, Priority.ALWAYS);
            calendarGrid.add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentCalendarMonth = currentCalendarMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentCalendarMonth = currentCalendarMonth.plusMonths(1);
        renderCalendar();
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
        openCreateEvaluationDialog();
    }

    private void openCreateEvaluationDialog() {
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

    @FXML
    private void handleAiVideoAnalyse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une vidéo d'entretien");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov", "*.avi", "*.mkv"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) evaluationsGrid.getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return;
        }

        LOGGER.info(() -> "[AI] Vidéo sélectionnée: " + selectedFile.getAbsolutePath());

        aiAnalyseButton.setDisable(true);

        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Analyse vidéo en cours");
        progressAlert.setHeaderText(null);
        progressAlert.setContentText("Analyse de la vidéo en cours, veuillez patienter...");
        progressAlert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        progressAlert.initOwner(stage);
        progressAlert.initModality(Modality.APPLICATION_MODAL);
        progressAlert.show();

        Task<AnalysisResult> task = new Task<>() {
            @Override
            protected AnalysisResult call() throws Exception {
                AiVideoAnalysisService service = new AiVideoAnalysisService();
                return service.analyseInterview(selectedFile);
            }
        };

        task.setOnSucceeded(event -> {
            AnalysisResult result = task.getValue();
            try {
                new AiAnalysisTempStorage().save(result);
            } finally {
                aiAnalyseButton.setDisable(false);
                if (progressAlert.isShowing()) {
                    progressAlert.close();
                }
            }

            openCreateEvaluationDialog();
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();

            aiAnalyseButton.setDisable(false);
            if (progressAlert.isShowing()) {
                progressAlert.close();
            }

            LOGGER.log(Level.WARNING, "[AI] Échec analyse vidéo.", ex);

            String baseMessage = "L’analyse vidéo a échoué. Veuillez réessayer plus tard ou vérifier le fichier.";
            String details = (ex instanceof AiVideoAnalysisException) ? ex.getMessage() : null;
            String fullMessage = (details != null && !details.isBlank())
                    ? baseMessage + "\n\nDétail : " + details
                    : baseMessage;

            showAlert(Alert.AlertType.ERROR, "Erreur d'analyse vidéo", fullMessage);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
