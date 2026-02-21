package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import interfaces.Services;
import model.Evaluation;
import model.ScoreCompetence;

public class EvaluationDAO implements Services<Evaluation> {
    private Connection cnx;
    private ScoreCompetenceDAO scoreCompetenceDAO;

    public EvaluationDAO() {
        try {
            this.cnx = DatabaseConnection.getConnection();
            this.scoreCompetenceDAO = new ScoreCompetenceDAO();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void add(Evaluation evaluation) {
        String req = "INSERT INTO `evaluation`(`date_creation`, `commentaire_global`, `decision_preliminaire`, `fk_entretien_id`, `fk_recruteur_id`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            pstm.setTimestamp(1, Timestamp.valueOf(evaluation.getDateCreation()));
            pstm.setString(2, evaluation.getCommentaireGlobal());
            pstm.setString(3, evaluation.getDecisionPreliminaire().name());
            pstm.setInt(4, evaluation.getFkEntretienId());
            pstm.setInt(5, evaluation.getFkRecruteurId());
            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                evaluation.setIdEvaluation(generatedId);

                if (evaluation.getScoreCompetences() != null) {
                    for (ScoreCompetence sc : evaluation.getScoreCompetences()) {
                        scoreCompetenceDAO.addWithEvaluationId(sc, generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Evaluation evaluation) {
        String req = "UPDATE `evaluation` SET `date_creation`=?, `commentaire_global`=?, `decision_preliminaire`=?, `fk_entretien_id`=?, `fk_recruteur_id`=? WHERE `id_evaluation`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setTimestamp(1, Timestamp.valueOf(evaluation.getDateCreation()));
            pstm.setString(2, evaluation.getCommentaireGlobal());
            pstm.setString(3, evaluation.getDecisionPreliminaire().name());
            pstm.setInt(4, evaluation.getFkEntretienId());
            pstm.setInt(5, evaluation.getFkRecruteurId());
            pstm.setInt(6, evaluation.getIdEvaluation());
            pstm.executeUpdate();
            // Note: Score competences are managed separately via ScoreCompetenceDAO
            // (add/delete operations in UpdateEvaluationController)
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Evaluation evaluation) {
        String req = "DELETE FROM `evaluation` WHERE `id_evaluation`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, evaluation.getIdEvaluation());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> evaluations = new ArrayList<>();
        String req = "SELECT * FROM `evaluation`";
        try {
            Statement stm = this.cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                Evaluation e = new Evaluation();
                e.setIdEvaluation(rs.getInt("id_evaluation"));
                e.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                e.setCommentaireGlobal(rs.getString("commentaire_global"));
                e.setDecisionPreliminaire(Evaluation.DecisionPreliminaire.valueOf(rs.getString("decision_preliminaire")));
                e.setFkEntretienId(rs.getInt("fk_entretien_id"));
                e.setFkRecruteurId(rs.getInt("fk_recruteur_id"));
                evaluations.add(e);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return evaluations;
    }

    @Override
    public Evaluation getOne(Evaluation evaluation) {
        Evaluation result = null;
        String req = "SELECT * FROM `evaluation` WHERE `id_evaluation`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, evaluation.getIdEvaluation());
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                result = new Evaluation();
                result.setIdEvaluation(rs.getInt("id_evaluation"));
                result.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                result.setCommentaireGlobal(rs.getString("commentaire_global"));
                result.setDecisionPreliminaire(Evaluation.DecisionPreliminaire.valueOf(rs.getString("decision_preliminaire")));
                result.setFkEntretienId(rs.getInt("fk_entretien_id"));
                result.setFkRecruteurId(rs.getInt("fk_recruteur_id"));

                List<ScoreCompetence> scores = scoreCompetenceDAO.getByEvaluationId(result.getIdEvaluation());
                result.setScoreCompetences(scores);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
}