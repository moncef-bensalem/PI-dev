package controller;

import database.ScoreCompetenceDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.ScoreCompetence;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailsScoreCompetenceController implements Initializable {

    @FXML
    private Label idLabel;

    @FXML
    private Label critereLabel;

    @FXML
    private Label noteLabel;

    @FXML
    private Label appreciationLabel;

    private ScoreCompetence scoreCompetence;
    private ScoreCompetenceDAO scoreCompetenceDAO;
    private boolean deleted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scoreCompetenceDAO = new ScoreCompetenceDAO();
    }

    public void setScoreCompetence(ScoreCompetence scoreCompetence) {
        this.scoreCompetence = scoreCompetence;
        populateFields();
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
    private void deleteScoreCompetence() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Score Competence: " + scoreCompetence.getNomCritere());
        confirmDialog.setContentText("Are you sure you want to delete this score competence? This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                scoreCompetenceDAO.delete(scoreCompetence);
                deleted = true;
                showAlert(Alert.AlertType.INFORMATION, "Success", "Score Competence deleted successfully.");
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
        if (scoreCompetence != null) {
            if (idLabel != null) idLabel.setText("Score Competence #" + scoreCompetence.getIdDetail());
            if (critereLabel != null) critereLabel.setText("Criteria: " + scoreCompetence.getNomCritere());
            if (noteLabel != null) noteLabel.setText("Note: " + scoreCompetence.getNoteAttribuee() + "/10");
            if (appreciationLabel != null) appreciationLabel.setText("Appreciation: " + (scoreCompetence.getAppreciationSpecifique() != null ? scoreCompetence.getAppreciationSpecifique() : "N/A"));
        }
    }
}
