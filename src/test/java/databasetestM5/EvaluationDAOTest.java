package databasetestM5;

import databaseModule5.DatabaseConnection;
import databaseModule5.EvaluationDAO;
import databaseModule5.ScoreCompetenceDAO;
import modelModule5.Evaluation;
import modelModule5.ScoreCompetence;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EvaluationDAOTest {

    private static Connection connection;
    private static EvaluationDAO evaluationDAO;
    private static ScoreCompetenceDAO scoreCompetenceDAO;

    @BeforeAll
    static void setUpAll() {
        try {
            connection = DatabaseConnection.getConnection();
            assumeTrue(connection != null, "Database connection must be available for EvaluationDAO tests");
            evaluationDAO = new EvaluationDAO();
            scoreCompetenceDAO = new ScoreCompetenceDAO();
        } catch (SQLException e) {
            assumeTrue(false, "Unable to establish database connection: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseConnection.closeConnection();
    }

    @BeforeEach
    void setUp() throws SQLException {
        if (connection != null) {
            connection.setAutoCommit(false);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }

    private Evaluation buildTestEvaluation(String uniqueSuffix) {
        LocalDateTime now = LocalDateTime.now();
        String comment = "Evaluation test - " + uniqueSuffix;
        Evaluation.DecisionPreliminaire decision = Evaluation.DecisionPreliminaire.FAVORABLE;
        int entretienId = 1;
        int recruteurId = 1;

        Evaluation evaluation = new Evaluation(now, comment, decision, entretienId, recruteurId);
        evaluation.addScoreCompetence(new ScoreCompetence("Critere-" + uniqueSuffix, 15.0f, "Bon"));
        return evaluation;
    }

    @Test
    @Order(1)
    @DisplayName("add should insert Evaluation and its ScoreCompetences")
    void testAddEvaluationWithScores() {
        String suffix = UUID.randomUUID().toString();
        Evaluation evaluation = buildTestEvaluation(suffix);

        evaluationDAO.add(evaluation);

        assertTrue(evaluation.getIdEvaluation() > 0, "Generated id should be set on Evaluation");

        Evaluation loaded = evaluationDAO.getOne(evaluation);
        assertNotNull(loaded, "Inserted Evaluation should be retrievable");
        assertEquals(evaluation.getCommentaireGlobal(), loaded.getCommentaireGlobal());
        assertEquals(evaluation.getDecisionPreliminaire(), loaded.getDecisionPreliminaire());

        List<ScoreCompetence> scores = loaded.getScoreCompetences();
        assertNotNull(scores);
        assertFalse(scores.isEmpty(), "Scores should be loaded for Evaluation");
    }

    @Test
    @Order(2)
    @DisplayName("update should modify existing Evaluation")
    void testUpdateEvaluation() {
        String suffix = "Update-" + UUID.randomUUID();
        Evaluation evaluation = buildTestEvaluation(suffix);
        evaluationDAO.add(evaluation);

        evaluation.setCommentaireGlobal("Commentaire mis à jour");
        evaluation.setDecisionPreliminaire(Evaluation.DecisionPreliminaire.A_REVOIR);

        evaluationDAO.update(evaluation);

        Evaluation loaded = evaluationDAO.getOne(evaluation);
        assertNotNull(loaded);
        assertEquals("Commentaire mis à jour", loaded.getCommentaireGlobal());
        assertEquals(Evaluation.DecisionPreliminaire.A_REVOIR, loaded.getDecisionPreliminaire());
    }

    @Test
    @Order(3)
    @DisplayName("delete should remove existing Evaluation")
    void testDeleteEvaluation() {
        String suffix = "Delete-" + UUID.randomUUID();
        Evaluation evaluation = buildTestEvaluation(suffix);
        evaluationDAO.add(evaluation);

        evaluationDAO.delete(evaluation);

        Evaluation deleted = evaluationDAO.getOne(evaluation);
        assertNull(deleted, "Deleted Evaluation should not be found");
    }

    @Test
    @Order(4)
    @DisplayName("getAll should return a non-null list")
    void testGetAll() {
        List<Evaluation> evaluations = evaluationDAO.getAll();
        assertNotNull(evaluations, "getAll should never return null");
    }
}

