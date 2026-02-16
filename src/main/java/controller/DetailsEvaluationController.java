package controller;

import database.EvaluationDAO;
import database.ScoreCompetenceDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Evaluation;
import model.ScoreCompetence;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailsEvaluationController implements Initializable {

    @FXML
    private Label idLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label decisionLabel;

    @FXML
    private Label commentLabel;

    @FXML
    private Label entretienLabel;

    @FXML
    private Label recruteurLabel;

    @FXML
    private ListView<VBox> scoreCompetenceListView;

    private Evaluation evaluation;
    private DateTimeFormatter dateFormatter;
    private EvaluationDAO evaluationDAO;
    private ScoreCompetenceDAO scoreCompetenceDAO;
    private boolean deleted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        evaluationDAO = new EvaluationDAO();
        scoreCompetenceDAO = new ScoreCompetenceDAO();
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        populateFields();
        loadScoreCompetences();
    }

    public boolean isDeleted() {
        return deleted;
    }

    @FXML
    private void close() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openUpdateEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateEvaluation.fxml"));
            Parent root = loader.load();

            UpdateEvaluationController controller = loader.getController();
            controller.setEvaluation(evaluation);

            Stage stage = new Stage();
            stage.setTitle("Update Evaluation #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh the details if the evaluation was updated
            if (controller.isSaved()) {
                // Reload evaluation from database to get updated data
                Evaluation updatedEvaluation = evaluationDAO.getOne(evaluation);
                if (updatedEvaluation != null) {
                    this.evaluation = updatedEvaluation;
                    populateFields();
                    loadScoreCompetences();
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open update dialog: " + e.getMessage());
        }
    }

    @FXML
    private void deleteEvaluation() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Evaluation #" + evaluation.getIdEvaluation());
        confirmDialog.setContentText("Are you sure you want to delete this evaluation? This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                evaluationDAO.delete(evaluation);
                deleted = true;
                showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation deleted successfully.");
                close();
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

    private void populateFields() {
        if (evaluation != null) {
            if (idLabel != null) idLabel.setText("Evaluation #" + evaluation.getIdEvaluation());
            if (dateLabel != null) dateLabel.setText("Created: " + evaluation.getDateCreation().format(dateFormatter));
            if (decisionLabel != null) decisionLabel.setText("Decision: " + evaluation.getDecisionPreliminaire().toString());
            if (commentLabel != null) commentLabel.setText("Comment: " + (evaluation.getCommentaireGlobal() != null ? evaluation.getCommentaireGlobal() : "No comment"));
            if (entretienLabel != null) entretienLabel.setText("Interview ID: " + evaluation.getFkEntretienId());
            if (recruteurLabel != null) recruteurLabel.setText("Recruiter ID: " + evaluation.getFkRecruteurId());
        }
    }

    private void loadScoreCompetences() {
        if (scoreCompetenceListView != null && evaluation != null) {
            scoreCompetenceListView.getItems().clear();
            var scores = scoreCompetenceDAO.getByEvaluationId(evaluation.getIdEvaluation());
            for (ScoreCompetence score : scores) {
                scoreCompetenceListView.getItems().add(createScoreCompetenceCard(score));
            }
        }
    }

    private VBox createScoreCompetenceCard(ScoreCompetence score) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 10; -fx-cursor: hand;");
        card.setPrefWidth(440);

        // Make card clickable to open details
        card.setOnMouseClicked(event -> openScoreCompetenceDetails(score));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label critereLabel = new Label(score.getNomCritere());
        critereLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label noteLabel = new Label("Note: " + score.getNoteAttribuee() + "/10");
        noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        HBox.setHgrow(critereLabel, Priority.ALWAYS);
        critereLabel.setMaxWidth(Double.MAX_VALUE);

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 4;");
        deleteButton.setOnAction(event -> {
            event.consume();
            deleteScoreCompetence(score);
        });

        header.getChildren().addAll(critereLabel, noteLabel, deleteButton);

        Label appreciationLabel = new Label("Appreciation: " + (score.getAppreciationSpecifique() != null ? score.getAppreciationSpecifique() : "N/A"));
        appreciationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        appreciationLabel.setWrapText(true);

        card.getChildren().addAll(header, appreciationLabel);
        return card;
    }

    private void openScoreCompetenceDetails(ScoreCompetence score) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsScoreCompetence.fxml"));
            Parent root = loader.load();

            DetailsScoreCompetenceController controller = loader.getController();
            controller.setScoreCompetence(score);

            Stage stage = new Stage();
            stage.setTitle("Score Competence Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh the list if the score was deleted
            if (controller.isDeleted()) {
                loadScoreCompetences();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Score Competence details: " + e.getMessage());
        }
    }

    private void deleteScoreCompetence(ScoreCompetence score) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Score Competence: " + score.getNomCritere());
        confirmDialog.setContentText("Are you sure you want to delete this score competence? This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                scoreCompetenceDAO.delete(score);
                loadScoreCompetences();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Score Competence deleted successfully.");
            }
        });
    }
}
