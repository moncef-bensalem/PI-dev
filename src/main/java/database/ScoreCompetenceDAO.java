package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import interfaces.Services;
import model.ScoreCompetence;

public class ScoreCompetenceDAO implements Services<ScoreCompetence> {
    private Connection cnx;

    public ScoreCompetenceDAO() {
        try {
            this.cnx = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void add(ScoreCompetence scoreCompetence) {
        String req = "INSERT INTO `score_competence`(`nom_critere`, `note_attribuee`, `appreciation_specifique`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, scoreCompetence.getNomCritere());
            pstm.setFloat(2, scoreCompetence.getNoteAttribuee());
            pstm.setString(3, scoreCompetence.getAppreciationSpecifique());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addWithEvaluationId(ScoreCompetence scoreCompetence, int evaluationId) {
        String req = "INSERT INTO `score_competence`(`nom_critere`, `note_attribuee`, `appreciation_specifique`, `fk_evaluation_id`) VALUES (?,?,?,?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, scoreCompetence.getNomCritere());
            pstm.setFloat(2, scoreCompetence.getNoteAttribuee());
            pstm.setString(3, scoreCompetence.getAppreciationSpecifique());
            pstm.setInt(4, evaluationId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(ScoreCompetence scoreCompetence) {
        String req = "UPDATE `score_competence` SET `nom_critere`=?, `note_attribuee`=?, `appreciation_specifique`=? WHERE `id_detail`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, scoreCompetence.getNomCritere());
            pstm.setFloat(2, scoreCompetence.getNoteAttribuee());
            pstm.setString(3, scoreCompetence.getAppreciationSpecifique());
            pstm.setInt(4, scoreCompetence.getIdDetail());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(ScoreCompetence scoreCompetence) {
        String req = "DELETE FROM `score_competence` WHERE `id_detail`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, scoreCompetence.getIdDetail());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<ScoreCompetence> getAll() {
        List<ScoreCompetence> scoreCompetences = new ArrayList<>();
        String req = "SELECT * FROM `score_competence`";
        try {
            Statement stm = this.cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                ScoreCompetence sc = new ScoreCompetence();
                sc.setIdDetail(rs.getInt("id_detail"));
                sc.setNomCritere(rs.getString("nom_critere"));
                sc.setNoteAttribuee(rs.getFloat("note_attribuee"));
                sc.setAppreciationSpecifique(rs.getString("appreciation_specifique"));
                scoreCompetences.add(sc);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return scoreCompetences;
    }

    @Override
    public ScoreCompetence getOne(ScoreCompetence scoreCompetence) {
        ScoreCompetence result = null;
        String req = "SELECT * FROM `score_competence` WHERE `id_detail`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, scoreCompetence.getIdDetail());
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                result = new ScoreCompetence();
                result.setIdDetail(rs.getInt("id_detail"));
                result.setNomCritere(rs.getString("nom_critere"));
                result.setNoteAttribuee(rs.getFloat("note_attribuee"));
                result.setAppreciationSpecifique(rs.getString("appreciation_specifique"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public List<ScoreCompetence> getByEvaluationId(int evaluationId) {
        List<ScoreCompetence> scoreCompetences = new ArrayList<>();
        String req = "SELECT * FROM `score_competence` WHERE `fk_evaluation_id`=?";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, evaluationId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                ScoreCompetence sc = new ScoreCompetence();
                sc.setIdDetail(rs.getInt("id_detail"));
                sc.setNomCritere(rs.getString("nom_critere"));
                sc.setNoteAttribuee(rs.getFloat("note_attribuee"));
                sc.setAppreciationSpecifique(rs.getString("appreciation_specifique"));
                scoreCompetences.add(sc);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return scoreCompetences;
    }
}