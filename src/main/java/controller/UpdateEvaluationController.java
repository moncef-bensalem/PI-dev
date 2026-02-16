package controller;

import database.EvaluationDAO;
import database.ScoreCompetenceDAO;
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
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UpdateEvaluationController implements Initializable {

    @FXML
    private TextField dateCreationField;

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
    private TextField fkRecruteurIdField;

    @FXML
    private ListView<ScoreCompetence> scoreCompetencesListView;
    @FXML
    private Label scoreCountLabel;

    private EvaluationDAO evaluationDAO;
    private ScoreCompetenceDAO scoreCompetenceDAO;
    private Evaluation currentEvaluation;
    private DateTimeFormatter dateFormatter;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationDAO = new EvaluationDAO();
        scoreCompetenceDAO = new ScoreCompetenceDAO();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Initialize decision combo box
        decisionPreliminaireCombo.setItems(FXCollections.observableArrayList(
                Evaluation.DecisionPreliminaire.FAVORABLE,
                Evaluation.DecisionPreliminaire.DEFAVORABLE,
                Evaluation.DecisionPreliminaire.A_REVOIR
        ));

        setupScoreCompetencesListView();
    }

    public void setEvaluation(Evaluation evaluation) {
        this.currentEvaluation = evaluation;
        populateFields();
        loadScoreCompetences();
    }

    public boolean isSaved() {
        return saved;
    }

    private void populateFields() {
        if (currentEvaluation != null) {
            // Display current date (will be updated on save)
            dateCreationField.setText(LocalDateTime.now().format(dateFormatter));

            commentaireGlobalField.setText(currentEvaluation.getCommentaireGlobal());
            decisionPreliminaireCombo.setValue(currentEvaluation.getDecisionPreliminaire());

            // FK fields are read-only
            fkEntretienIdField.setText(String.valueOf(currentEvaluation.getFkEntretienId()));
            fkRecruteurIdField.setText(String.valueOf(currentEvaluation.getFkRecruteurId()));
        }
    }

    private void setupScoreCompetencesListView() {
        scoreCompetencesListView.setCellFactory(param -> new ListCell<ScoreCompetence>() {
            @Override
            protected void updateItem(ScoreCompetence score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Label infoLabel = new Label(String.format("%s: %.1f/20%s",
                            score.getNomCritere(),
                            score.getNoteAttribuee(),
                            score.getAppreciationSpecifique() != null && !score.getAppreciationSpecifique().isEmpty()
                                    ? " - " + score.getAppreciationSpecifique()
                                    : ""));
                    infoLabel.setMaxWidth(Double.MAX_VALUE);
                    javafx.scene.layout.HBox.setHgrow(infoLabel, javafx.scene.layout.Priority.ALWAYS);

                    Button deleteButton = new Button("Delete");
                    deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 4;");
                    deleteButton.setOnAction(event -> deleteScoreCompetence(score));

                    hbox.getChildren().addAll(infoLabel, deleteButton);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void loadScoreCompetences() {
        if (currentEvaluation != null) {
            var scores = scoreCompetenceDAO.getByEvaluationId(currentEvaluation.getIdEvaluation());
            currentEvaluation.setScoreCompetences(scores);
            refreshScoreList();
        }
    }

    private void refreshScoreList() {
        scoreCompetencesListView.getItems().setAll(currentEvaluation.getScoreCompetences());
        updateScoreCount();
    }

    private void updateScoreCount() {
        int count = currentEvaluation.getScoreCompetences().size();
        scoreCountLabel.setText(count + " competence score(s)");
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
                // Save the new score to database with the current evaluation ID
                scoreCompetenceDAO.addWithEvaluationId(newScore, currentEvaluation.getIdEvaluation());
                // Reload scores from database
                loadScoreCompetences();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open add score dialog: " + e.getMessage());
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

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        // Update evaluation data
        currentEvaluation.setDateCreation(LocalDateTime.now()); // Auto-update date
        currentEvaluation.setCommentaireGlobal(commentaireGlobalField.getText().trim());
        currentEvaluation.setDecisionPreliminaire(decisionPreliminaireCombo.getValue());
        // fkEntretienId and fkRecruteurId are not updatable

        // Save to database
        evaluationDAO.update(currentEvaluation);

        saved = true;
        showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation updated successfully!");
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        saved = false;
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

        return isValid;
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
