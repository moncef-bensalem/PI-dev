package com.nexus.desktop.model;

import java.time.LocalDateTime;

/**
 * Authentication token model for JWT-based authentication
 * Contains token information and metadata
 */
public class AuthToken {
    private String token;
    private int userId;
    private String email;
    private String[] roles;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String sessionId;
    
    // Constructors
    public AuthToken() {}
    
    public AuthToken(String token, int userId, String email, String[] roles, 
                     LocalDateTime issuedAt, LocalDateTime expiresAt, String sessionId) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
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
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
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
    
    @Override
    public String toString() {
        return "AuthToken{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", roles=" + java.util.Arrays.toString(roles) +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}