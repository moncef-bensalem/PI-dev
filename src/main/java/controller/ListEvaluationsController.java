package controller;

import database.EvaluationDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Evaluation;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ListEvaluationsController implements Initializable {

    @FXML
    private ListView<Evaluation> evaluationsListView;

    @FXML
    private Label totalEvaluationsLabel;

    private EvaluationDAO evaluationDAO;
    private DateTimeFormatter dateFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationDAO = new EvaluationDAO();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        setupListView();
        loadEvaluations();
    }

    private void setupListView() {
        evaluationsListView.setCellFactory(param -> new ListCell<Evaluation>() {
            @Override
            protected void updateItem(Evaluation evaluation, boolean empty) {
                super.updateItem(evaluation, empty);

                if (empty || evaluation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createEvaluationCard(evaluation));
                }
            }
        });
    }

    private VBox createEvaluationCard(Evaluation evaluation) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2); -fx-cursor: hand;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(720);

        card.setOnMouseClicked(event -> openEvaluationDetails(evaluation));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Evaluation #" + evaluation.getIdEvaluation());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Circle decisionIndicator = new Circle(8);
        decisionIndicator.setFill(getDecisionColor(evaluation.getDecisionPreliminaire()));

        Label decisionLabel = new Label(evaluation.getDecisionPreliminaire().toString());
        decisionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + getDecisionHexColor(evaluation.getDecisionPreliminaire()) + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 4;");
        deleteButton.setOnAction(event -> {
            event.consume();
            handleDeleteEvaluation(evaluation);
        });
        deleteButton.setOnMouseClicked(event -> event.consume());

        header.getChildren().addAll(idLabel, decisionIndicator, decisionLabel, spacer, deleteButton);

        Label dateLabel = new Label("Created: " + evaluation.getDateCreation().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label commentLabel = new Label("Comment: " + (evaluation.getCommentaireGlobal() != null ? evaluation.getCommentaireGlobal() : "No comment"));
        commentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
        commentLabel.setWrapText(true);

        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label entretienLabel = new Label("Interview ID: " + evaluation.getFkEntretienId());
        entretienLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        Label recruteurLabel = new Label("Recruiter ID: " + evaluation.getFkRecruteurId());
        recruteurLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        footer.getChildren().addAll(entretienLabel, recruteurLabel);

        card.getChildren().addAll(header, dateLabel, commentLabel, footer);

        return card;
    }

    private void openEvaluationDetails(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsEvaluation.fxml"));
            Parent root = loader.load();

            DetailsEvaluationController controller = loader.getController();
            controller.setEvaluation(evaluation);

            Stage stage = new Stage();
            stage.setTitle("Evaluation Details - #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isDeleted()) {
                loadEvaluations();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation deleted successfully.");
            } else {
                loadEvaluations();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open evaluation details: " + e.getMessage());
        }
    }

    private void handleDeleteEvaluation(Evaluation evaluation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Evaluation #" + evaluation.getIdEvaluation());
        confirmDialog.setContentText("Are you sure you want to delete this evaluation? This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                evaluationDAO.delete(evaluation);
                loadEvaluations();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation deleted successfully.");
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
        var evaluations = evaluationDAO.getAll();
        evaluationsListView.getItems().setAll(evaluations);
        totalEvaluationsLabel.setText("Total: " + evaluations.size() + " evaluation" + (evaluations.size() != 1 ? "s" : ""));
    }

    @FXML
    private void handleAddEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateEvaluation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create New Evaluation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadEvaluations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open create evaluation dialog: " + e.getMessage());
        }
    }
}
