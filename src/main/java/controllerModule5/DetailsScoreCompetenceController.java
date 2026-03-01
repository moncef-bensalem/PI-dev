package controllerModule5;

import databaseModule5.ScoreCompetenceDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import modelModule5.ScoreCompetence;

import java.io.IOException;
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
    private boolean updated = false;

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

    public boolean isUpdated() {
        return updated;
    }

    @FXML
    private void close() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void deleteScoreCompetence() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le Score de Compétence : " + scoreCompetence.getNomCritere());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce score de compétence ? Cette action ne peut pas être annulée.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                scoreCompetenceDAO.delete(scoreCompetence);
                deleted = true;
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Score de compétence supprimé avec succès.");
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
            if (idLabel != null) idLabel.setText("Compétence #" + scoreCompetence.getIdDetail());
            if (critereLabel != null) critereLabel.setText(scoreCompetence.getNomCritere());
            if (noteLabel != null) noteLabel.setText(scoreCompetence.getNoteAttribuee() + "/20");
            if (appreciationLabel != null) appreciationLabel.setText(scoreCompetence.getAppreciationSpecifique() != null ? scoreCompetence.getAppreciationSpecifique() : "N/A");
        }
    }

    @FXML
    private void openUpdate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateScoreCompetence.fxml"));
            Parent root = loader.load();

            UpdateScoreCompetenceController controller = loader.getController();
            controller.setScoreCompetence(scoreCompetence);

            Stage stage = new Stage();
            stage.setTitle("Mettre à jour le Score de Compétence");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaved()) {
                updated = true;
                scoreCompetence = controller.getScoreCompetence();
                populateFields();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de mise à jour : " + e.getMessage());
        }
    }
}
