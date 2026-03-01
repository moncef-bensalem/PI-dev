package databasetestM5;

import databaseModule5.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @BeforeAll
    static void setUpAll() {
    }

    @AfterAll
    static void tearDownAll() {
        DatabaseConnection.closeConnection();
    }

    @Test
    @DisplayName("getConnection should return a non-null, open connection")
    void testGetConnectionNotNullAndOpen() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        assertNotNull(connection, "Connection should not be null");
        assertFalse(connection.isClosed(), "Connection should be open");
    }

    @Test
    @DisplayName("getConnection should return the same instance (singleton)")
    void testGetConnectionSingleton() throws SQLException {
        Connection connection1 = DatabaseConnection.getConnection();
        Connection connection2 = DatabaseConnection.getConnection();

        assertSame(connection1, connection2, "DatabaseConnection should reuse the same Connection instance");
    }
}

