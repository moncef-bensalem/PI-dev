package controller;

import database.EvaluationDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Evaluation;
import model.ScoreCompetence;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CreateEvaluationController implements Initializable {

    @FXML
    private TextField commentaireGlobalField;
    @FXML
    private Label commentaireErrorLabel;

    @FXML
    private ComboBox<Evaluation.DecisionPreliminaire> decisionPreliminaireCombo;
    @FXML
    private Label decisionErrorLabel;

    @FXML
    private TextField fkEntretienIdField;
    @FXML
    private Label entretienErrorLabel;

    @FXML
    private TextField fkRecruteurIdField;
    @FXML
    private Label recruteurErrorLabel;

    @FXML
    private ListView<ScoreCompetence> scoreCompetencesListView;
    @FXML
    private Label scoreCountLabel;

    private EvaluationDAO evaluationDAO;
    private Evaluation currentEvaluation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationDAO = new EvaluationDAO();
        currentEvaluation = new Evaluation();
        currentEvaluation.setDateCreation(LocalDateTime.now());

        // Initialize decision combo box
        decisionPreliminaireCombo.setItems(FXCollections.observableArrayList(
                Evaluation.DecisionPreliminaire.FAVORABLE,
                Evaluation.DecisionPreliminaire.DEFAVORABLE,
                Evaluation.DecisionPreliminaire.A_REVOIR
        ));

        setupScoreCompetencesListView();
        updateScoreCount();
    }

    private void setupScoreCompetencesListView() {
        scoreCompetencesListView.setCellFactory(param -> new ListCell<ScoreCompetence>() {
            @Override
            protected void updateItem(ScoreCompetence score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                } else {
                    setText(String.format("%s: %.1f/20%s",
                            score.getNomCritere(),
                            score.getNoteAttribuee(),
                            score.getAppreciationSpecifique() != null && !score.getAppreciationSpecifique().isEmpty()
                                    ? " - " + score.getAppreciationSpecifique()
                                    : ""));
                }
            }
        });
    }

    @FXML
    private void handleAddScoreCompetence() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateScoreCompetence.fxml"));
            Parent root = loader.load();

            CreateScoreCompetenceController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add Competence Score");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            ScoreCompetence newScore = controller.getScoreCompetence();
            if (newScore != null) {
                currentEvaluation.addScoreCompetence(newScore);
                refreshScoreList();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open add score dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        // Set evaluation data
        currentEvaluation.setCommentaireGlobal(commentaireGlobalField.getText().trim());
        currentEvaluation.setDecisionPreliminaire(decisionPreliminaireCombo.getValue());
        currentEvaluation.setFkEntretienId(Integer.parseInt(fkEntretienIdField.getText().trim()));
        currentEvaluation.setFkRecruteurId(Integer.parseInt(fkRecruteurIdField.getText().trim()));

        // Save to database
        evaluationDAO.add(currentEvaluation);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation created successfully!");
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate commentaireGlobal
        String commentaire = commentaireGlobalField.getText().trim();
        if (commentaire.isEmpty()) {
            commentaireErrorLabel.setText("Global comment is required");
            isValid = false;
        } else {
            commentaireErrorLabel.setText("");
        }

        // Validate decisionPreliminaire
        if (decisionPreliminaireCombo.getValue() == null) {
            decisionErrorLabel.setText("Please select a decision");
            isValid = false;
        } else {
            decisionErrorLabel.setText("");
        }

        // Validate fkEntretienId
        String entretienId = fkEntretienIdField.getText().trim();
        if (entretienId.isEmpty()) {
            entretienErrorLabel.setText("Interview ID is required");
            isValid = false;
        } else {
            try {
                Integer.parseInt(entretienId);
                entretienErrorLabel.setText("");
            } catch (NumberFormatException e) {
                entretienErrorLabel.setText("Interview ID must be a number");
                isValid = false;
            }
        }

        // Validate fkRecruteurId
        String recruteurId = fkRecruteurIdField.getText().trim();
        if (recruteurId.isEmpty()) {
            recruteurErrorLabel.setText("Recruiter ID is required");
            isValid = false;
        } else {
            try {
                Integer.parseInt(recruteurId);
                recruteurErrorLabel.setText("");
            } catch (NumberFormatException e) {
                recruteurErrorLabel.setText("Recruiter ID must be a number");
                isValid = false;
            }
        }

        return isValid;
    }

    private void refreshScoreList() {
        scoreCompetencesListView.getItems().setAll(currentEvaluation.getScoreCompetences());
        updateScoreCount();
    }

    private void updateScoreCount() {
        int count = currentEvaluation.getScoreCompetences().size();
        scoreCountLabel.setText(count + " competence score(s) added");
    }

    private void closeWindow() {
        Stage stage = (Stage) commentaireGlobalField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
