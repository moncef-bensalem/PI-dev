package com.nexus.desktop.service;

import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.model.Dashboard;
import com.nexus.desktop.model.User;
import com.nexus.desktop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Service for calculating dashboard statistics and KPIs
 * Handles all data aggregation for user management dashboard
 */
public class DashboardService {
    private UserDAO userDAO;
    
    public DashboardService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Generate complete dashboard data with all KPIs
     */
    public Dashboard generateDashboardData() throws SQLException {
        Dashboard dashboard = new Dashboard();
        
        // Calculate basic statistics
        dashboard.setTotalUsers(getTotalUsers());
        dashboard.setActiveUsers(getActiveUsers());
        dashboard.setInactiveUsers(getInactiveUsers());
        dashboard.setNewUsersThisMonth(getNewUsersThisMonth());
        
        // Calculate role distribution
        Map<String, Long> usersByRole = getUsersByRole();
        dashboard.setUsersByRole(usersByRole);
        dashboard.setRoleDistributionList(calculateRolePercentages(usersByRole, dashboard.getTotalUsers()));
        
        // Get recent activity
        dashboard.setRecentLogins(getRecentLogins(5));
        dashboard.setRecentCreations(getRecentCreations(5));
        
        // Security data
        dashboard.setFailedLoginAttempts(getFailedLoginAttempts());
        dashboard.setSecurityAlerts(getRecentSecurityAlerts(10));
        
        // Monthly evolution
        dashboard.setMonthlyEvolution(getMonthlyEvolution(6));
        
        return dashboard;
    }
    
    /**
     * Get total number of users
     */
    private long getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM user";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong("total");
            }
        }
        return 0;
    }
    
    /**
     * Get active users count
     */
    private long getActiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) as active FROM user WHERE is_active = 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong("active");
            }
        }
        return 0;
    }
    
    /**
     * Get inactive users count
     */
    private long getInactiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) as inactive FROM user WHERE is_active = 0";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong("inactive");
            }
        }
        return 0;
    }
    
    /**
     * Get users created this month
     */
    private long getNewUsersThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) as new_users FROM user WHERE MONTH(created_at) = MONTH(CURRENT_DATE()) AND YEAR(created_at) = YEAR(CURRENT_DATE())";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong("new_users");
            }
        }
        return 0;
    }
    
    /**
     * Get user distribution by role
     */
    private Map<String, Long> getUsersByRole() throws SQLException {
        Map<String, Long> roleCounts = new HashMap<>();
        
        String sql = "SELECT roles, COUNT(*) as count FROM user GROUP BY roles";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                String rolesJson = resultSet.getString("roles");
                long count = resultSet.getLong("count");
                
                // Parse role from JSON (simplified - in real app, use proper JSON parsing)
                String role = parseRoleFromJson(rolesJson);
                roleCounts.put(role, count);
            }
        }
        
        return roleCounts;
    }
    
    /**
     * Calculate role percentages for pie chart
     */
    private List<Dashboard.RoleCount> calculateRolePercentages(Map<String, Long> roleCounts, long totalUsers) {
        List<Dashboard.RoleCount> roleList = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : roleCounts.entrySet()) {
            String roleName = entry.getKey();
            long count = entry.getValue();
            double percentage = totalUsers > 0 ? (count * 100.0 / totalUsers) : 0;
            
            roleList.add(new Dashboard.RoleCount(roleName, count, percentage));
        }
        
        return roleList;
    }
    
    /**
     * Get recent user logins
     */
    private List<Dashboard.RecentUserLogin> getRecentLogins(int limit) throws SQLException {
        List<Dashboard.RecentUserLogin> recentLogins = new ArrayList<>();
        
        // Note: This assumes you have a login_log table or similar
        // For now, we'll simulate with recent user data
        String sql = "SELECT id, email, first_name, last_name, created_at FROM user ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, limit);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long userId = resultSet.getLong("id");
                    String email = resultSet.getString("email");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                    
                    String fullName = firstName + " " + lastName;
                    Dashboard.RecentUserLogin login = new Dashboard.RecentUserLogin(
                        userId, email, fullName, createdAt, "127.0.0.1" // Simulated IP
                    );
                    recentLogins.add(login);
                }
            }
        }
        
        return recentLogins;
    }
    
    /**
     * Get recently created users
     */
    private List<Dashboard.UserCreation> getRecentCreations(int limit) throws SQLException {
        List<Dashboard.UserCreation> recentCreations = new ArrayList<>();
        
        String sql = "SELECT id, email, first_name, last_name, roles, created_at FROM user ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, limit);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long userId = resultSet.getLong("id");
                    String email = resultSet.getString("email");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String rolesJson = resultSet.getString("roles");
                    LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                    
                    String fullName = firstName + " " + lastName;
                    String role = parseRoleFromJson(rolesJson);
                    
                    Dashboard.UserCreation creation = new Dashboard.UserCreation(
                        userId, email, fullName, role, createdAt
                    );
                    recentCreations.add(creation);
                }
            }
        }
        
        return recentCreations;
    }
    
    /**
     * Get failed login attempts (simulated)
     */
    private long getFailedLoginAttempts() throws SQLException {
        // In a real implementation, you would have a login_attempts table
        // For now, returning simulated data
        return 3; // Simulated failed attempts
    }
    
    /**
     * Get recent security alerts
     */
    private List<Dashboard.SecurityAlert> getRecentSecurityAlerts(int limit) {
        List<Dashboard.SecurityAlert> alerts = new ArrayList<>();
        
        // Simulated security alerts
        alerts.add(new Dashboard.SecurityAlert(
            "FAILED_LOGIN", 
            "Multiple failed login attempts detected for user admin@nexus.com",
            LocalDateTime.now().minusHours(2),
            "MEDIUM"
        ));
        
        alerts.add(new Dashboard.SecurityAlert(
            "ACCOUNT_LOCKED",
            "User account rh@nexus.com temporarily locked due to security policy",
            LocalDateTime.now().minusDays(1),
            "LOW"
        ));
        
        return alerts;
    }
    
    /**
     * Get monthly user evolution
     */
    private List<Dashboard.MonthlyUserCount> getMonthlyEvolution(int months) throws SQLException {
        List<Dashboard.MonthlyUserCount> monthlyData = new ArrayList<>();
        
        // Get current month and previous months
        YearMonth currentMonth = YearMonth.now();
        
        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            
            String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active " +
                        "FROM user WHERE YEAR(created_at) = ? AND MONTH(created_at) = ?";
            
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                statement.setInt(1, month.getYear());
                statement.setInt(2, month.getMonthValue());
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        long total = resultSet.getLong("total");
                        long active = resultSet.getLong("active");
                        
                        String monthYear = month.toString(); // Format: 2024-01
                        monthlyData.add(new Dashboard.MonthlyUserCount(monthYear, total, active));
                    }
                }
            }
        }
        
        return monthlyData;
    }
    
    /**
     * Parse role from JSON string (simplified)
     * In production, use proper JSON parsing library
     */
    private String parseRoleFromJson(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty()) {
            return "USER";
        }
        
        // Simple parsing - extract role name from JSON array
        // Example: "[\"ROLE_ADMIN\"]" -> "ADMIN"
        if (rolesJson.contains("ADMIN")) {
            return "ADMIN";
        } else if (rolesJson.contains("RH")) {
            return "RH";
        } else if (rolesJson.contains("MANAGER")) {
            return "MANAGER";
        } else if (rolesJson.contains("CANDIDAT")) {
            return "CANDIDAT";
        }
        
        return "USER";
    }
    
    /**
     * Get dashboard data for specific user role
     */
    public Dashboard generateDashboardForRole(String userRole) throws SQLException {
        Dashboard dashboard = generateDashboardData();
        
        // Filter data based on user role permissions
        if (!"ADMIN".equals(userRole)) {
            // Non-admin users see limited dashboard
            dashboard.setSecurityAlerts(new ArrayList<>()); // No security alerts
            // Add other role-specific filtering here
        }
        
        return dashboard;
    }
}