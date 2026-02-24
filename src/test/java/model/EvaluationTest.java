package model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EvaluationTest {

    private Evaluation evaluation;

    @BeforeAll
    static void beforeAll() {
    }

    @AfterAll
    static void afterAll() {
    }

    @BeforeEach
    void setUp() {
        evaluation = new Evaluation();
    }

    @AfterEach
    void tearDown() {
        evaluation = null;
    }

    @Test
    @Order(1)
    @DisplayName("Constructor with arguments should correctly set fields")
    void testConstructorWithArguments() {
        LocalDateTime now = LocalDateTime.now();
        String comment = "Bon profil";
        Evaluation.DecisionPreliminaire decision = Evaluation.DecisionPreliminaire.FAVORABLE;
        int entretienId = 1;
        int recruteurId = 2;

        Evaluation e = new Evaluation(now, comment, decision, entretienId, recruteurId);

        assertEquals(now, e.getDateCreation());
        assertEquals(comment, e.getCommentaireGlobal());
        assertEquals(decision, e.getDecisionPreliminaire());
        assertEquals(entretienId, e.getFkEntretienId());
        assertEquals(recruteurId, e.getFkRecruteurId());
        assertNotNull(e.getScoreCompetences());
        assertTrue(e.getScoreCompetences().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("addScoreCompetence should add item to internal list")
    void testAddScoreCompetence() {
        ScoreCompetence score = new ScoreCompetence("Communication", 15f, "Très bon échange");

        evaluation.addScoreCompetence(score);

        assertEquals(1, evaluation.getScoreCompetences().size());
        assertSame(score, evaluation.getScoreCompetences().get(0));
    }

    @Test
    @Order(3)
    @DisplayName("removeScoreCompetence should remove existing item from list")
    void testRemoveScoreCompetence() {
        ScoreCompetence score1 = new ScoreCompetence("Communication", 15f, "Très bon échange");
        ScoreCompetence score2 = new ScoreCompetence("Technique", 14f, "Bon niveau");

        evaluation.addScoreCompetence(score1);
        evaluation.addScoreCompetence(score2);

        evaluation.removeScoreCompetence(score1);

        assertEquals(1, evaluation.getScoreCompetences().size());
        assertFalse(evaluation.getScoreCompetences().contains(score1));
        assertTrue(evaluation.getScoreCompetences().contains(score2));
    }

    @Test
    @Order(4)
    @DisplayName("toString should contain key fields")
    void testToString() {
        evaluation.setIdEvaluation(10);
        evaluation.setDateCreation(LocalDateTime.of(2024, 1, 1, 10, 0));
        evaluation.setDecisionPreliminaire(Evaluation.DecisionPreliminaire.A_REVOIR);
        evaluation.setFkEntretienId(5);
        evaluation.setFkRecruteurId(6);

        String result = evaluation.toString();

        assertNotNull(result);
        assertTrue(result.contains("idEvaluation=10"));
        assertTrue(result.contains("A_REVOIR"));
        assertTrue(result.contains("fkEntretienId=5"));
        assertTrue(result.contains("fkRecruteurId=6"));
    }
}

