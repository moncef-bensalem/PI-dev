package com.nexus.desktop.dao;

import com.nexus.desktop.model.User;
import com.nexus.desktop.util.DatabaseManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

/**
 * DAO implementation for User entity
 */
public class UserDAO implements GenericDAO<User, Integer> {
    
    @Override
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO user (email, first_name, last_name, password, roles, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, convertRolesToJson(user.getRoles()));
            statement.setBoolean(6, user.isActive());
            statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Convert roles array to JSON string format
     */
    private String convertRolesToJson(String[] roles) {
        if (roles == null || roles.length == 0) {
            return "[\"ROLE_USER\"]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(roles[i]).append("\"");
        }
        json.append("]");
        return json.toString();
    }
    
    /**
     * Parse roles from JSON string format
     */
    private String[] parseRolesFromJson(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty() || !rolesJson.startsWith("[") || !rolesJson.endsWith("]")) {
            return new String[]{"ROLE_USER"};
        }
        
        // Remove brackets
        String content = rolesJson.substring(1, rolesJson.length() - 1);
        
        if (content.trim().isEmpty()) {
            return new String[]{"ROLE_USER"};
        }
        
        // Split by comma and clean up quotes
        String[] roles = content.split(",");
        for (int i = 0; i < roles.length; i++) {
            roles[i] = roles[i].trim().replaceAll("^\"|\"$", ""); // Remove leading/trailing quotes
        }
        
        return roles;
    }
    
    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET email = ?, first_name = ?, last_name = ?, password = ?, roles = ?, is_active = ? WHERE id = ?";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, convertRolesToJson(user.getRoles()));
            statement.setBoolean(6, user.isActive());
            statement.setInt(7, user.getId());
            
            statement.executeUpdate();
        }
    }
    
    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
    
    @Override
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        }
        return null;
    }
    
    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }
        
        return users;
    }
    
    @Override
    public boolean exists(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE id = ?";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    /**
     * Update profile (first_name, last_name, email only - password unchanged)
     */
    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE user SET first_name = ?, last_name = ?, email = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setInt(4, user.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Check if email exists for another user (exclude userId)
     */
    public boolean emailExistsForOtherUser(String email, int excludeUserId) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email = ? AND id != ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, excludeUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        }
        return null;
    }
    
    /**
     * Authenticate user by email and password
     */
    public User authenticate(String email, String password) throws SQLException {
        System.out.println("UserDAO.authenticate called with email: " + email);
        User user = findByEmail(email);
        System.out.println("User found: " + (user != null ? "Yes" : "No"));
        if (user != null) {
            System.out.println("User active: " + user.isActive());
            System.out.println("Password check result: " + checkPassword(password, user.getPasswordHash()));
        }
        if (user != null && user.isActive() && checkPassword(password, user.getPasswordHash())) {
            System.out.println("Authentication successful for: " + email);
            return user;
        }
        System.out.println("Authentication failed for: " + email);
        return null;
    }
    
    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email = ?";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    /**
     * Search users by name or email
     */
    public List<User> searchUsers(String searchTerm) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet));
                }
            }
        }
        
        return users;
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE roles LIKE ? ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, "%" + role + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet));
                }
            }
        }
        
        return users;
    }
    
    /**
     * Get active/inactive users
     */
    public List<User> getUsersByStatus(boolean active) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE is_active = ? ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setBoolean(1, active);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet));
                }
            }
        }
        
        return users;
    }
    
    /**
     * Count total users
     */
    public int getUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM user";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        }
        
        return 0;
    }
    
    /**
     * Count active users
     */
    public int getActiveUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM user WHERE is_active = 1";
        
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        }
        
        return 0;
    }
    
    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setEmail(resultSet.getString("email"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setPasswordHash(resultSet.getString("password"));
        user.setActive(resultSet.getBoolean("is_active"));
        
        // Parse roles from JSON string
        String rolesJson = resultSet.getString("roles");
        if (rolesJson != null && !rolesJson.isEmpty()) {
            user.setRoles(parseRolesFromJson(rolesJson));
        } else {
            user.setRoles(new String[]{"ROLE_USER"});
        }
        
        return user;
    }
    
    /**
     * Password checking using BCrypt (compatible with Symfony/PHP)
     */
    private boolean checkPassword(String plainPassword, String hashedPassword) {
        // Check if the password is in bcrypt format
        if (hashedPassword.startsWith("$2y$") || hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$")) {
            // Use jBCrypt to verify the password
            return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
        }
        // For backward compatibility with plain text passwords
        return plainPassword.equals(hashedPassword);
    }
}