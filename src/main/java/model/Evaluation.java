package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    private int idEvaluation;
    private LocalDateTime dateCreation;
    private String commentaireGlobal;
    private DecisionPreliminaire decisionPreliminaire;
    private int fkEntretienId;
    private int fkRecruteurId;
    private LocalDate reviewDeadline;
    private List<ScoreCompetence> scoreCompetences;

    public enum DecisionPreliminaire {
        FAVORABLE,
        DEFAVORABLE,
        A_REVOIR
    }

    public Evaluation() {
        this.scoreCompetences = new ArrayList<>();
    }

    public Evaluation(LocalDateTime dateCreation, String commentaireGlobal, DecisionPreliminaire decisionPreliminaire, int fkEntretienId, int fkRecruteurId) {
        this.dateCreation = dateCreation;
        this.commentaireGlobal = commentaireGlobal;
        this.decisionPreliminaire = decisionPreliminaire;
        this.fkEntretienId = fkEntretienId;
        this.fkRecruteurId = fkRecruteurId;
        this.scoreCompetences = new ArrayList<>();
    }

    public int getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(int idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCommentaireGlobal() {
        return commentaireGlobal;
    }

    public void setCommentaireGlobal(String commentaireGlobal) {
        this.commentaireGlobal = commentaireGlobal;
    }

    public DecisionPreliminaire getDecisionPreliminaire() {
        return decisionPreliminaire;
    }

    public void setDecisionPreliminaire(DecisionPreliminaire decisionPreliminaire) {
        this.decisionPreliminaire = decisionPreliminaire;
    }

    public int getFkEntretienId() {
        return fkEntretienId;
    }

    public void setFkEntretienId(int fkEntretienId) {
        this.fkEntretienId = fkEntretienId;
    }

    public int getFkRecruteurId() {
        return fkRecruteurId;
    }

    public void setFkRecruteurId(int fkRecruteurId) {
        this.fkRecruteurId = fkRecruteurId;
    }

    public LocalDate getReviewDeadline() {
        return reviewDeadline;
    }

    public void setReviewDeadline(LocalDate reviewDeadline) {
        this.reviewDeadline = reviewDeadline;
    }

    public List<ScoreCompetence> getScoreCompetences() {
        return scoreCompetences;
    }

    public void setScoreCompetences(List<ScoreCompetence> scoreCompetences) {
        this.scoreCompetences = scoreCompetences;
    }

    public void addScoreCompetence(ScoreCompetence scoreCompetence) {
        this.scoreCompetences.add(scoreCompetence);
    }

    public void removeScoreCompetence(ScoreCompetence scoreCompetence) {
        this.scoreCompetences.remove(scoreCompetence);
    }

    public float getMoyenneScore() {
        if (scoreCompetences == null || scoreCompetences.isEmpty()) {
            return 0f;
        }
        float sum = 0f;
        for (ScoreCompetence sc : scoreCompetences) {
            sum += sc.getNoteAttribuee();
        }
        return sum / scoreCompetences.size();
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "idEvaluation=" + idEvaluation +
                ", dateCreation=" + dateCreation +
                ", reviewDeadline=" + reviewDeadline +
                ", decisionPreliminaire=" + decisionPreliminaire +
                ", fkEntretienId=" + fkEntretienId +
                ", fkRecruteurId=" + fkRecruteurId +
                ", scoreCompetences=" + scoreCompetences +
                '}';
    }
}