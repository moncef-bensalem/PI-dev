package com.nexus.desktop.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager using HikariCP connection pooling
 */
public class DatabaseManager {
    private static HikariDataSource dataSource;
    private static final Properties dbProperties = new Properties();

    static {
        initialize();
    }

    private static void initialize() {
        try {
            // Load database configuration
            InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties");
            if (input == null) {
                System.err.println("WARNING: Unable to find database.properties file");
                return;
            }
            
            dbProperties.load(input);

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            // Construire l'URL dynamiquement à partir des propriétés individuelles
            String host = dbProperties.getProperty("db.host");
            String port = dbProperties.getProperty("db.port");
            String dbName = dbProperties.getProperty("db.name");
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", host, port, dbName));
            config.setUsername(dbProperties.getProperty("db.username"));
            config.setPassword(dbProperties.getProperty("db.password"));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Connection pool settings
            config.setMaximumPoolSize(Integer.parseInt(dbProperties.getProperty("db.pool.maximumPoolSize")));
            config.setMinimumIdle(Integer.parseInt(dbProperties.getProperty("db.pool.minimumIdle")));
            config.setConnectionTimeout(Long.parseLong(dbProperties.getProperty("db.pool.connectionTimeout")));
            config.setIdleTimeout(Long.parseLong(dbProperties.getProperty("db.pool.idleTimeout")));
            config.setMaxLifetime(Long.parseLong(dbProperties.getProperty("db.pool.maxLifetime")));
            
            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            dataSource = new HikariDataSource(config);
            
            System.out.println("Database connection pool initialized successfully");
        } catch (IOException e) {
            System.err.println("WARNING: Failed to load database configuration: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("WARNING: Database connection failed - running in offline mode: " + e.getMessage());
            // Don't throw exception, allow application to run without database
            dataSource = null;
        }
    }

    /**
     * Get a database connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }

    /**
     * Check if database connection is available
     */
    public static boolean isDatabaseAvailable() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get database properties
     */
    public static Properties getProperties() {
        return dbProperties;
    }
}