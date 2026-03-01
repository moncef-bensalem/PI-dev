package controllerModule5;

import databaseModule5.EvaluationDAO;
import databaseModule5.ScoreCompetenceDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import modelModule5.Evaluation;
import modelModule5.ScoreCompetence;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class UpdateEvaluationController implements Initializable {

    @FXML
    private TextField commentaireGlobalField;
    @FXML
    private Label commentaireErrorLabel;

    @FXML
    private ComboBox<Evaluation.DecisionPreliminaire> decisionPreliminaireCombo;
    @FXML
    private Label decisionErrorLabel;

    @FXML
    private VBox reviewDeadlineContainer;
    @FXML
    private DatePicker reviewDeadlinePicker;
    @FXML
    private Label reviewDeadlineErrorLabel;

    @FXML
    private ListView<ScoreCompetence> scoreCompetencesListView;
    @FXML
    private Label scoreCountLabel;

    private EvaluationDAO evaluationDAO;
    private ScoreCompetenceDAO scoreCompetenceDAO;
    private Evaluation currentEvaluation;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationDAO = new EvaluationDAO();
        scoreCompetenceDAO = new ScoreCompetenceDAO();

        decisionPreliminaireCombo.setItems(FXCollections.observableArrayList(
                Evaluation.DecisionPreliminaire.FAVORABLE,
                Evaluation.DecisionPreliminaire.DEFAVORABLE,
                Evaluation.DecisionPreliminaire.A_REVOIR
        ));

        decisionPreliminaireCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            boolean isARevoir = newValue == Evaluation.DecisionPreliminaire.A_REVOIR;
            if (reviewDeadlineContainer != null) {
                reviewDeadlineContainer.setVisible(isARevoir);
                reviewDeadlineContainer.setManaged(isARevoir);
            }
            if (isARevoir && reviewDeadlinePicker != null) {
                reviewDeadlineErrorLabel.setText("");
                Platform.runLater(() -> reviewDeadlinePicker.show());
            }
            if (!isARevoir && reviewDeadlinePicker != null) {
                reviewDeadlinePicker.setValue(null);
                reviewDeadlineErrorLabel.setText("");
            }
        });

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
            commentaireGlobalField.setText(currentEvaluation.getCommentaireGlobal());
            decisionPreliminaireCombo.setValue(currentEvaluation.getDecisionPreliminaire());
            if (currentEvaluation.getDecisionPreliminaire() == Evaluation.DecisionPreliminaire.A_REVOIR) {
                if (reviewDeadlineContainer != null) {
                    reviewDeadlineContainer.setVisible(true);
                    reviewDeadlineContainer.setManaged(true);
                }
                if (reviewDeadlinePicker != null) {
                    reviewDeadlinePicker.setValue(currentEvaluation.getReviewDeadline());
                }
            } else {
                if (reviewDeadlineContainer != null) {
                    reviewDeadlineContainer.setVisible(false);
                    reviewDeadlineContainer.setManaged(false);
                }
                if (reviewDeadlinePicker != null) {
                    reviewDeadlinePicker.setValue(null);
                }
            }
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
                    infoLabel.getStyleClass().addAll("font-size-13", "text-dark");
                    infoLabel.setMaxWidth(Double.MAX_VALUE);
                    javafx.scene.layout.HBox.setHgrow(infoLabel, javafx.scene.layout.Priority.ALWAYS);

                    Button deleteButton = new Button("Supprimer");
                    deleteButton.getStyleClass().addAll("button", "button-red");
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
        scoreCountLabel.setText(count + " score(s) de compétence");
    }

    @FXML
    private void handleAddScoreCompetence() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateScoreCompetence.fxml"));
            Parent root = loader.load();

            CreateScoreCompetenceController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Score de Compétence");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            ScoreCompetence newScore = controller.getScoreCompetence();
            if (newScore != null) {
                scoreCompetenceDAO.addWithEvaluationId(newScore, currentEvaluation.getIdEvaluation());
                loadScoreCompetences();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la boîte de dialogue d'ajout de score : " + e.getMessage());
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

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        currentEvaluation.setDateCreation(LocalDateTime.now());
        currentEvaluation.setCommentaireGlobal(commentaireGlobalField.getText().trim());
        currentEvaluation.setDecisionPreliminaire(decisionPreliminaireCombo.getValue());
        if (decisionPreliminaireCombo.getValue() == Evaluation.DecisionPreliminaire.A_REVOIR) {
            currentEvaluation.setReviewDeadline(reviewDeadlinePicker.getValue());
        } else {
            currentEvaluation.setReviewDeadline(null);
        }

        evaluationDAO.update(currentEvaluation);

        saved = true;
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation mise à jour avec succès !");
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeWindow();
    }

    private boolean validateForm() {
        boolean isValid = true;

        String commentaire = commentaireGlobalField.getText().trim();
        if (commentaire.isEmpty()) {
            commentaireErrorLabel.setText("Le commentaire global est obligatoire");
            isValid = false;
        } else {
            commentaireErrorLabel.setText("");
        }

        if (decisionPreliminaireCombo.getValue() == null) {
            decisionErrorLabel.setText("Veuillez sélectionner une décision");
            isValid = false;
        } else {
            decisionErrorLabel.setText("");
        }

        if (decisionPreliminaireCombo.getValue() == Evaluation.DecisionPreliminaire.A_REVOIR) {
            if (reviewDeadlinePicker.getValue() == null) {
                reviewDeadlineErrorLabel.setText("Veuillez choisir une date limite pour A_REVOIR");
                isValid = false;
            } else {
                reviewDeadlineErrorLabel.setText("");
            }
        } else {
            reviewDeadlineErrorLabel.setText("");
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
