package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ScoreCompetence;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateScoreCompetenceController implements Initializable {

    @FXML
    private TextField nomCritereField;
    @FXML
    private Label nomCritereErrorLabel;

    @FXML
    private TextField noteAttribueeField;
    @FXML
    private Label noteErrorLabel;

    @FXML
    private TextField appreciationSpecifiqueField;

    private ScoreCompetence scoreCompetence;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scoreCompetence = null;
        saved = false;
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        String nomCritere = nomCritereField.getText().trim();
        float noteAttribuee = Float.parseFloat(noteAttribueeField.getText().trim());
        String appreciationSpecifique = appreciationSpecifiqueField.getText().trim();
        if (appreciationSpecifique.isEmpty()) {
            appreciationSpecifique = null;
        }

        scoreCompetence = new ScoreCompetence(nomCritere, noteAttribuee, appreciationSpecifique);
        saved = true;

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        scoreCompetence = null;
        saved = false;
        closeWindow();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate nomCritere (mandatory)
        String nomCritere = nomCritereField.getText().trim();
        if (nomCritere.isEmpty()) {
            nomCritereErrorLabel.setText("Criteria name is required");
            isValid = false;
        } else {
            nomCritereErrorLabel.setText("");
        }

        // Validate noteAttribuee (0-20)
        String noteText = noteAttribueeField.getText().trim();
        if (noteText.isEmpty()) {
            noteErrorLabel.setText("Score is required");
            isValid = false;
        } else {
            try {
                float note = Float.parseFloat(noteText);
                if (note < 0 || note > 20) {
                    noteErrorLabel.setText("Score must be between 0 and 20");
                    isValid = false;
                } else {
                    noteErrorLabel.setText("");
                }
            } catch (NumberFormatException e) {
                noteErrorLabel.setText("Score must be a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Returns the created ScoreCompetence if saved, null otherwise
     */
    public ScoreCompetence getScoreCompetence() {
        return saved ? scoreCompetence : null;
    }

    private void closeWindow() {
        Stage stage = (Stage) nomCritereField.getScene().getWindow();
        stage.close();
    }
}
