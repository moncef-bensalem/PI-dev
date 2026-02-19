package com.nexus.desktop.model;

import java.time.LocalDateTime;

/**
 * User session model for tracking active user sessions
 * Contains session information and metadata
 */
public class Session {
    private String sessionId;
    private int userId;
    private String email;
    private String[] roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private LocalDateTime expiresAt;
    private String ipAddress;
    private String userAgent;
    private boolean isActive;
    
    // Constructors
    public Session() {}
    
    public Session(String sessionId, int userId, String email, String[] roles, 
                   LocalDateTime createdAt, LocalDateTime expiresAt, 
                   String ipAddress, String userAgent) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.lastActivity = createdAt;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String[] getRoles() {
        return roles;
    }
    
    public void setRoles(String[] roles) {
        this.roles = roles;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Check if session is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if session is valid (active and not expired)
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }
    
    /**
     * Update last activity timestamp
     */
    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        if (roles == null || role == null) return false;
        
        for (String userRole : roles) {
            if (userRole != null && userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... requiredRoles) {
        if (roles == null || requiredRoles == null) return false;
        
        for (String requiredRole : requiredRoles) {
            if (hasRole(requiredRole)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get session timeout in minutes
     */
    public long getTimeoutMinutes() {
        if (expiresAt != null && createdAt != null) {
            return java.time.Duration.between(createdAt, expiresAt).toMinutes();
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", roles=" + java.util.Arrays.toString(roles) +
                ", createdAt=" + createdAt +
                ", lastActivity=" + lastActivity +
                ", expiresAt=" + expiresAt +
                ", isActive=" + isActive +
                '}';
    }
}