package model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreCompetenceTest {

    private ScoreCompetence score;

    @BeforeAll
    static void beforeAll() {
        // One-time setup for this test class if needed
    }

    @AfterAll
    static void afterAll() {
        // One-time cleanup for this test class if needed
    }

    @BeforeEach
    void setUp() {
        score = new ScoreCompetence();
    }

    @AfterEach
    void tearDown() {
        score = null;
    }

    @Test
    @DisplayName("Constructor with arguments should correctly set fields")
    void testConstructorWithArguments() {
        String critere = "Communication";
        float note = 18.5f;
        String appreciation = "Excellent";

        ScoreCompetence sc = new ScoreCompetence(critere, note, appreciation);

        assertEquals(critere, sc.getNomCritere());
        assertEquals(note, sc.getNoteAttribuee());
        assertEquals(appreciation, sc.getAppreciationSpecifique());
    }

    @Test
    @DisplayName("Getters and setters should work as expected")
    void testGettersAndSetters() {
        score.setIdDetail(42);
        score.setNomCritere("Technique");
        score.setNoteAttribuee(14.0f);
        score.setAppreciationSpecifique("Bon niveau général");

        assertEquals(42, score.getIdDetail());
        assertEquals("Technique", score.getNomCritere());
        assertEquals(14.0f, score.getNoteAttribuee());
        assertEquals("Bon niveau général", score.getAppreciationSpecifique());
    }

    @Test
    @DisplayName("toString should contain key fields")
    void testToString() {
        score.setIdDetail(5);
        score.setNomCritere("Communication");
        score.setNoteAttribuee(16.0f);
        score.setAppreciationSpecifique("Très bon");

        String result = score.toString();

        assertNotNull(result);
        assertTrue(result.contains("idDetail=5"));
        assertTrue(result.contains("Communication"));
        assertTrue(result.contains("16.0"));
        assertTrue(result.contains("Très bon"));
    }
}

