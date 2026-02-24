package com.nexus.desktop.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persistent storage for JWT token (Remember Me feature)
 * Stores token in user home directory: .nexus-desktop/token
 */
public final class TokenStorage {

    private static final String DIR_NAME = ".nexus-desktop";
    private static final String TOKEN_FILE = "token";

    private TokenStorage() {}

    private static Path getTokenPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, DIR_NAME, TOKEN_FILE);
    }

    /**
     * Save token to persistent storage
     */
    public static void save(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }
        try {
            Path path = getTokenPath();
            Files.createDirectories(path.getParent());
            Files.write(path, token.trim().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Failed to save token: " + e.getMessage());
        }
    }

    /**
     * Load token from persistent storage
     * @return token string or null if not found/invalid
     */
    public static String load() {
        try {
            Path path = getTokenPath();
            if (!Files.exists(path)) {
                return null;
            }
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            System.err.println("Failed to load token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete stored token (on logout)
     */
    public static void delete() {
        try {
            Path path = getTokenPath();
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete token: " + e.getMessage());
        }
    }

    /**
     * Check if a token is stored
     */
    public static boolean hasToken() {
        return Files.exists(getTokenPath());
    }
}
