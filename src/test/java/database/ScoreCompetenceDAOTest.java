package database;

import model.ScoreCompetence;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScoreCompetenceDAOTest {

    private static Connection connection;
    private static ScoreCompetenceDAO scoreCompetenceDAO;

    @BeforeAll
    static void setUpAll() {
        try {
            connection = DatabaseConnection.getConnection();
            assumeTrue(connection != null, "Database connection must be available for DAO tests");
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
        // Use transactions to keep tests isolated
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

    @Test
    @Order(1)
    @DisplayName("add should insert a new ScoreCompetence")
    void testAdd() {
        String uniqueName = "Critere-" + UUID.randomUUID();
        ScoreCompetence score = new ScoreCompetence(uniqueName, 12.5f, "Test insertion");

        scoreCompetenceDAO.add(score);

        List<ScoreCompetence> all = scoreCompetenceDAO.getAll();
        boolean exists = all.stream()
                .anyMatch(sc -> uniqueName.equals(sc.getNomCritere())
                        && sc.getNoteAttribuee() == 12.5f
                        && "Test insertion".equals(sc.getAppreciationSpecifique()));

        assertTrue(exists, "Inserted ScoreCompetence should be present in getAll()");
    }

    @Test
    @Order(2)
    @DisplayName("update should modify existing ScoreCompetence")
    void testUpdate() {
        String uniqueName = "CritereUpdate-" + UUID.randomUUID();
        ScoreCompetence score = new ScoreCompetence(uniqueName, 10.0f, "Avant update");
        scoreCompetenceDAO.add(score);

        // Retrieve the inserted row to get its generated id
        ScoreCompetence persisted = scoreCompetenceDAO.getAll().stream()
                .filter(sc -> uniqueName.equals(sc.getNomCritere()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Persisted ScoreCompetence not found"));

        persisted.setNomCritere(uniqueName + "-Updated");
        persisted.setNoteAttribuee(17.0f);
        persisted.setAppreciationSpecifique("Après update");

        scoreCompetenceDAO.update(persisted);

        ScoreCompetence reloaded = scoreCompetenceDAO.getOne(persisted);

        assertNotNull(reloaded, "Updated ScoreCompetence should be retrievable");
        assertEquals(uniqueName + "-Updated", reloaded.getNomCritere());
        assertEquals(17.0f, reloaded.getNoteAttribuee());
        assertEquals("Après update", reloaded.getAppreciationSpecifique());
    }

    @Test
    @Order(3)
    @DisplayName("delete should remove existing ScoreCompetence")
    void testDelete() {
        String uniqueName = "CritereDelete-" + UUID.randomUUID();
        ScoreCompetence score = new ScoreCompetence(uniqueName, 8.0f, "À supprimer");
        scoreCompetenceDAO.add(score);

        ScoreCompetence persisted = scoreCompetenceDAO.getAll().stream()
                .filter(sc -> uniqueName.equals(sc.getNomCritere()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Persisted ScoreCompetence not found"));

        scoreCompetenceDAO.delete(persisted);

        ScoreCompetence deleted = scoreCompetenceDAO.getOne(persisted);
        assertNull(deleted, "Deleted ScoreCompetence should not be found");
    }
}

