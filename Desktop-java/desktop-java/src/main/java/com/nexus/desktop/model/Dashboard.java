package com.nexus.desktop.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dashboard data model for User Management Module
 * Contains all KPIs and statistics for admin dashboard
 */
public class Dashboard {
    // User Statistics
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long newUsersThisMonth;
    
    // Role Distribution
    private Map<String, Long> usersByRole;
    private List<RoleCount> roleDistributionList;
    
    // Recent Activity
    private List<RecentUserLogin> recentLogins;
    private List<UserCreation> recentCreations;
    
    // Security Alerts
    private long failedLoginAttempts;
    private List<SecurityAlert> securityAlerts;
    
    // Monthly Evolution
    private List<MonthlyUserCount> monthlyEvolution;
    
    // Getters and Setters
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    
    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    
    public long getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
    
    public long getNewUsersThisMonth() { return newUsersThisMonth; }
    public void setNewUsersThisMonth(long newUsersThisMonth) { this.newUsersThisMonth = newUsersThisMonth; }
    
    public Map<String, Long> getUsersByRole() { return usersByRole; }
    public void setUsersByRole(Map<String, Long> usersByRole) { this.usersByRole = usersByRole; }
    
    public List<RoleCount> getRoleDistributionList() { return roleDistributionList; }
    public void setRoleDistributionList(List<RoleCount> roleDistributionList) { this.roleDistributionList = roleDistributionList; }
    
    public List<RecentUserLogin> getRecentLogins() { return recentLogins; }
    public void setRecentLogins(List<RecentUserLogin> recentLogins) { this.recentLogins = recentLogins; }
    
    public List<UserCreation> getRecentCreations() { return recentCreations; }
    public void setRecentCreations(List<UserCreation> recentCreations) { this.recentCreations = recentCreations; }
    
    public long getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(long failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public List<SecurityAlert> getSecurityAlerts() { return securityAlerts; }
    public void setSecurityAlerts(List<SecurityAlert> securityAlerts) { this.securityAlerts = securityAlerts; }
    
    public List<MonthlyUserCount> getMonthlyEvolution() { return monthlyEvolution; }
    public void setMonthlyEvolution(List<MonthlyUserCount> monthlyEvolution) { this.monthlyEvolution = monthlyEvolution; }
    
    // Helper classes for dashboard data
    public static class RoleCount {
        private String roleName;
        private long count;
        private double percentage;
        
        public RoleCount(String roleName, long count, double percentage) {
            this.roleName = roleName;
            this.count = count;
            this.percentage = percentage;
        }
        
        // Getters and setters
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
    
    public static class RecentUserLogin {
        private long userId;
        private String userEmail;
        private String userName;
        private LocalDateTime loginTime;
        private String ipAddress;
        
        public RecentUserLogin(long userId, String userEmail, String userName, LocalDateTime loginTime, String ipAddress) {
            this.userId = userId;
            this.userEmail = userEmail;
            this.userName = userName;
            this.loginTime = loginTime;
            this.ipAddress = ipAddress;
        }
        
        // Getters and setters
        public long getUserId() { return userId; }
        public void setUserId(long userId) { this.userId = userId; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    }
    
    public static class UserCreation {
        private long userId;
        private String userEmail;
        private String userName;
        private String role;
        private LocalDateTime createdAt;
        
        public UserCreation(long userId, String userEmail, String userName, String role, LocalDateTime createdAt) {
            this.userId = userId;
            this.userEmail = userEmail;
            this.userName = userName;
            this.role = role;
            this.createdAt = createdAt;
        }
        
        // Getters and setters
        public long getUserId() { return userId; }
        public void setUserId(long userId) { this.userId = userId; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    public static class SecurityAlert {
        private String type;
        private String description;
        private LocalDateTime timestamp;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        
        public SecurityAlert(String type, String description, LocalDateTime timestamp, String severity) {
            this.type = type;
            this.description = description;
            this.timestamp = timestamp;
            this.severity = severity;
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
    
    public static class MonthlyUserCount {
        private String monthYear; // "Jan 2024"
        private long userCount;
        private long activeCount;
        
        public MonthlyUserCount(String monthYear, long userCount, long activeCount) {
            this.monthYear = monthYear;
            this.userCount = userCount;
            this.activeCount = activeCount;
        }
        
        // Getters and setters
        public String getMonthYear() { return monthYear; }
        public void setMonthYear(String monthYear) { this.monthYear = monthYear; }
        
        public long getUserCount() { return userCount; }
        public void setUserCount(long userCount) { this.userCount = userCount; }
        
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }
    }
}