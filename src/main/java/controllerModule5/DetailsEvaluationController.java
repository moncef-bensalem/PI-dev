package controllerModule5;

import databaseModule5.EvaluationDAO;
import databaseModule5.ScoreCompetenceDAO;
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
import modelModule5.Evaluation;
import modelModule5.ScoreCompetence;

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
            stage.setTitle("Mettre à jour l'Évaluation #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaved()) {
                Evaluation updatedEvaluation = evaluationDAO.getOne(evaluation);
                if (updatedEvaluation != null) {
                    this.evaluation = updatedEvaluation;
                    populateFields();
                    loadScoreCompetences();
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la boîte de dialogue de mise à jour : " + e.getMessage());
        }
    }

    @FXML
    private void deleteEvaluation() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer l'Évaluation #" + evaluation.getIdEvaluation());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette évaluation ? Cette action ne peut pas être annulée.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                evaluationDAO.delete(evaluation);
                deleted = true;
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée avec succès.");
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
            if (idLabel != null) idLabel.setText("Évaluation #" + evaluation.getIdEvaluation());
            if (dateLabel != null) dateLabel.setText(evaluation.getDateCreation().format(dateFormatter));
            if (decisionLabel != null) decisionLabel.setText(evaluation.getDecisionPreliminaire().toString());
            if (commentLabel != null) commentLabel.setText(evaluation.getCommentaireGlobal() != null ? evaluation.getCommentaireGlobal() : "Aucun commentaire");
            if (entretienLabel != null) entretienLabel.setText(String.valueOf(evaluation.getFkEntretienId()));
            if (recruteurLabel != null) recruteurLabel.setText(String.valueOf(evaluation.getFkRecruteurId()));
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
        card.getStyleClass().add("card-light");
        card.setPrefWidth(440);
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseClicked(event -> openScoreCompetenceDetails(score));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label critereLabel = new Label(score.getNomCritere());
        critereLabel.getStyleClass().addAll("font-size-14", "font-bold", "text-dark");

        Label noteLabel = new Label("Note : " + score.getNoteAttribuee() + "/20");
        noteLabel.getStyleClass().addAll("font-size-13", "font-bold", "text-blue");

        HBox.setHgrow(critereLabel, Priority.ALWAYS);
        critereLabel.setMaxWidth(Double.MAX_VALUE);

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("button", "button-red");
        deleteButton.setOnAction(event -> {
            event.consume();
            deleteScoreCompetence(score);
        });

        header.getChildren().addAll(critereLabel, noteLabel, deleteButton);

        Label appreciationLabel = new Label("Appréciation : " + (score.getAppreciationSpecifique() != null ? score.getAppreciationSpecifique() : "N/A"));
        appreciationLabel.getStyleClass().addAll("font-size-12", "text-gray");
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
            stage.setTitle("Détails du Score de Compétence");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isDeleted()) {
                loadScoreCompetences();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du score de compétence : " + e.getMessage());
        }
    }

    private void deleteScoreCompetence(ScoreCompetence score) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le Score de Compétence : " + score.getNomCritere());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce score de compétence ? Cette action ne peut pas être annulée.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                scoreCompetenceDAO.delete(score);
                loadScoreCompetences();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Score de compétence supprimé avec succès.");
            }
        });
    }
}
