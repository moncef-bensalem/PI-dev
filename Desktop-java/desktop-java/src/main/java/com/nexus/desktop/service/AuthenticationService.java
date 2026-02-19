package com.nexus.desktop.service;

import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.model.AuthToken;
import com.nexus.desktop.model.Session;
import com.nexus.desktop.model.User;
import io.jsonwebtoken.JwtException;

import java.sql.SQLException;

/**
 * Main authentication service handling user authentication and authorization
 * Integrates JWT tokens with session management
 */
public class AuthenticationService {
    private final UserDAO userDAO;
    private final TokenService tokenService;
    private final SessionManager sessionManager;
    
    // Current authenticated user and session
    private User currentUser;
    private Session currentSession;
    private AuthToken currentToken;
    
    public AuthenticationService() {
        System.out.println("AuthService created with new instances");
        this.userDAO = new UserDAO();
        this.tokenService = new TokenService();
        this.sessionManager = SessionManager.getInstance();
        System.out.println("SessionManager instance: " + (sessionManager != null ? "Available" : "Null"));
    }
    
    public AuthenticationService(UserDAO userDAO, TokenService tokenService, SessionManager sessionManager) {
        this.userDAO = userDAO;
        this.tokenService = tokenService;
        this.sessionManager = sessionManager;
    }
    
    /**
     * Authenticate user with email and password
     */
    public AuthToken authenticate(String email, String password) throws SQLException {
        System.out.println("AuthService.authenticate called with email: " + email);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Authenticate user against database
        User user = userDAO.authenticate(email, password);
        if (user == null) {
            throw new SecurityException("Invalid credentials");
        }
        
        if (!user.isActive()) {
            throw new SecurityException("User account is deactivated");
        }
        
        // Generate JWT token
        AuthToken token = tokenService.generateToken(user);
        
        // Create session
        String ipAddress = getLocalIpAddress();
        String userAgent = "NEXUS Desktop App";
        Session session = sessionManager.createSession(user, ipAddress, userAgent);
        
        // Update current authentication context
        this.currentUser = user;
        this.currentSession = session;
        this.currentToken = token;
        
        System.out.println("Authentication context updated - Session ID: " + session.getSessionId());
        System.out.println("Current session after setting: " + (this.currentSession != null ? "Available" : "Null"));
        
        return token;
    }
    
    /**
     * Restore session from persistent token (Remember Me - app restart).
     * Validates JWT, loads user from DB, creates new session (old session is lost on restart).
     * @throws SecurityException if token invalid or user no longer active
     */
    public AuthToken restoreFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Token is empty");
        }
        try {
            AuthToken authToken = tokenService.validateToken(token);
            User user = userDAO.findById(authToken.getUserId());
            if (user == null || !user.isActive()) {
                throw new SecurityException("User account no longer valid");
            }
            // Create new session (previous session was lost on app restart)
            String ipAddress = getLocalIpAddress();
            String userAgent = "NEXUS Desktop App (restored)";
            Session session = sessionManager.createSession(user, ipAddress, userAgent);
            this.currentUser = user;
            this.currentSession = session;
            this.currentToken = authToken;
            return authToken;
        } catch (JwtException e) {
            throw new SecurityException("Invalid or expired token: " + e.getMessage());
        } catch (SQLException e) {
            throw new SecurityException("Database error: " + e.getMessage());
        }
    }

    /**
     * Validate existing token and refresh session
     */
    public AuthToken validateAndRefreshToken(String token) {
        try {
            // Validate token
            AuthToken authToken = tokenService.validateToken(token);
            
            // Validate corresponding session
            Session session = sessionManager.validateSession(authToken.getSessionId());
            if (session == null) {
                throw new SecurityException("Session not found or invalid");
            }
            
            // Check if user is still active
            try {
                User user = userDAO.findById(authToken.getUserId());
                if (user == null || !user.isActive()) {
                    throw new SecurityException("User account no longer valid");
                }
            } catch (SQLException e) {
                throw new SecurityException("Database error during validation: " + e.getMessage());
            }
            
            // Refresh token if it's expiring soon
            if (tokenService.isTokenExpiringSoon(token)) {
                authToken = tokenService.refreshToken(authToken);
            }
            
            // Update current context
            this.currentToken = authToken;
            this.currentSession = session;
            
            try {
                this.currentUser = userDAO.findById(authToken.getUserId());
            } catch (SQLException e) {
                throw new SecurityException("Failed to load user data: " + e.getMessage());
            }
            
            return authToken;
            
        } catch (JwtException e) {
            throw new SecurityException("Invalid token: " + e.getMessage());
        }
    }
    
    /**
     * Logout current user
     */
    public boolean logout() {
        System.out.println("Logout called - Current session: " + (currentSession != null ? currentSession.getSessionId() : "null"));
        
        boolean loggedOut = false;
        
        // Try to invalidate current session
        if (currentSession != null) {
            boolean invalidated = sessionManager.invalidateSession(currentSession.getSessionId());
            System.out.println("Session invalidation result: " + invalidated);
            if (invalidated) {
                loggedOut = true;
            }
        }
        
        // If current user exists, invalidate all their sessions
        if (currentUser != null) {
            int invalidatedCount = sessionManager.invalidateUserSessions(currentUser.getId());
            System.out.println("Invalidated " + invalidatedCount + " user sessions");
            if (invalidatedCount > 0) {
                loggedOut = true;
            }
        }
        
        // Clear current context regardless
        clearCurrentContext();
        
        System.out.println("Logout completed - Success: " + loggedOut);
        return loggedOut;
    }
    
    /**
     * Logout all sessions for current user
     */
    public int logoutAllSessions() {
        if (currentUser != null) {
            int invalidatedCount = sessionManager.invalidateUserSessions(currentUser.getId());
            if (invalidatedCount > 0) {
                clearCurrentContext();
            }
            return invalidatedCount;
        }
        return 0;
    }
    
    /**
     * Check if user has required role
     */
    public boolean hasRole(String requiredRole) {
        if (currentToken == null || requiredRole == null) {
            return false;
        }
        return currentToken.hasRole(requiredRole);
    }
    
    /**
     * Check if user has any of the required roles
     */
    public boolean hasAnyRole(String... requiredRoles) {
        if (currentToken == null || requiredRoles == null) {
            return false;
        }
        return currentToken.hasAnyRole(requiredRoles);
    }
    
    /**
     * Check if current user can access specific resource
     */
    public boolean canAccessResource(String resource, String action) {
        if (currentToken == null) {
            return false;
        }
        
        // Admin can access everything
        if (currentToken.hasRole("ROLE_ADMIN")) {
            return true;
        }
        
        // RH can access user management
        if (currentToken.hasRole("ROLE_RH") && 
            ("user_management".equals(resource) || "dashboard".equals(resource))) {
            return true;
        }
        
        // Manager can access dashboard
        if (currentToken.hasRole("ROLE_MANAGER") && "dashboard".equals(resource)) {
            return true;
        }
        
        // Candidate can access basic features
        if (currentToken.hasRole("ROLE_CANDIDAT") && 
            ("profile".equals(resource) || "applications".equals(resource))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get current session
     */
    public Session getCurrentSession() {
        System.out.println("getCurrentSession called - currentSession: " + (currentSession != null ? "Available" : "Null"));
        return currentSession;
    }
    
    /**
     * Get current token
     */
    public AuthToken getCurrentToken() {
        return currentToken;
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null && 
               currentSession != null && 
               currentToken != null &&
               currentSession.isValid() &&
               !currentToken.isExpired();
    }
    
    /**
     * Get authentication status message
     */
    public String getAuthStatus() {
        if (!isAuthenticated()) {
            return "Not authenticated";
        }
        
        StringBuilder status = new StringBuilder();
        status.append("Authenticated as: ").append(currentUser.getFullName());
        status.append(" (").append(currentUser.getEmail()).append(")");
        status.append(" | Roles: ").append(String.join(", ", currentUser.getRoles()));
        status.append(" | Session expires: ").append(currentSession.getExpiresAt());
        
        return status.toString();
    }
    
    /**
     * Clear current authentication context
     */
    private void clearCurrentContext() {
        this.currentUser = null;
        this.currentSession = null;
        this.currentToken = null;
    }
    
    /**
     * Get local IP address (simplified implementation)
     */
    private String getLocalIpAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * Get token service instance
     */
    public TokenService getTokenService() {
        return tokenService;
    }
    
    /**
     * Get session manager instance
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    /**
     * Get user DAO instance
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }
}