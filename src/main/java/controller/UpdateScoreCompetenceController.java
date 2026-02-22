package controller;

import database.ScoreCompetenceDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ScoreCompetence;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateScoreCompetenceController implements Initializable {

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
    private ScoreCompetenceDAO scoreCompetenceDAO;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scoreCompetenceDAO = new ScoreCompetenceDAO();
        saved = false;
    }

    public void setScoreCompetence(ScoreCompetence scoreCompetence) {
        this.scoreCompetence = scoreCompetence;
        populateFields();
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

        scoreCompetence.setNomCritere(nomCritere);
        scoreCompetence.setNoteAttribuee(noteAttribuee);
        scoreCompetence.setAppreciationSpecifique(appreciationSpecifique);

        scoreCompetenceDAO.update(scoreCompetence);
        saved = true;

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Score de compétence mis à jour avec succès.");
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeWindow();
    }

    private boolean validateForm() {
        boolean isValid = true;

        String nomCritere = nomCritereField.getText().trim();
        if (nomCritere.isEmpty()) {
            nomCritereErrorLabel.setText("Le nom du critère est obligatoire");
            isValid = false;
        } else {
            nomCritereErrorLabel.setText("");
        }

        String noteText = noteAttribueeField.getText().trim();
        if (noteText.isEmpty()) {
            noteErrorLabel.setText("La note est obligatoire");
            isValid = false;
        } else {
            try {
                float note = Float.parseFloat(noteText);
                if (note < 0 || note > 20) {
                    noteErrorLabel.setText("La note doit être comprise entre 0 et 20");
                    isValid = false;
                } else {
                    noteErrorLabel.setText("");
                }
            } catch (NumberFormatException e) {
                noteErrorLabel.setText("La note doit être un nombre valide");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Returns true if the score competence was saved successfully
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Returns the updated ScoreCompetence
     */
    public ScoreCompetence getScoreCompetence() {
        return scoreCompetence;
    }

    private void populateFields() {
        if (scoreCompetence != null) {
            nomCritereField.setText(scoreCompetence.getNomCritere());
            noteAttribueeField.setText(String.valueOf(scoreCompetence.getNoteAttribuee()));
            appreciationSpecifiqueField.setText(scoreCompetence.getAppreciationSpecifique() != null ? scoreCompetence.getAppreciationSpecifique() : "");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomCritereField.getScene().getWindow();
        stage.close();
    }
}
