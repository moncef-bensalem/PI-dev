package com.nexus.desktop.service;

import com.nexus.desktop.model.AuthToken;
import com.nexus.desktop.model.Session;
import com.nexus.desktop.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing user sessions
 * Handles session creation, validation, and cleanup
 */
public class SessionManager {
    // Active sessions storage (in production, use persistent storage)
    private static final Map<String, Session> ACTIVE_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Integer, List<String>> USER_SESSIONS = new ConcurrentHashMap<>();
    
    // Session cleanup timer
    private static final Timer CLEANUP_TIMER = new Timer("SessionCleanupTimer", true);
    private static final long CLEANUP_INTERVAL = 60000; // 1 minute
    
    static {
        // Start cleanup task
        startCleanupTask();
    }
    
    private final TokenService tokenService;
    
    public SessionManager() {
        this.tokenService = new TokenService();
    }
    
    public SessionManager(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    /**
     * Create new session for authenticated user
     */
    public Session createSession(User user, String ipAddress, String userAgent) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Invalid user for session creation");
        }
        
        // Generate session ID
        String sessionId = UUID.randomUUID().toString();
        
        // Create session
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = calculateSessionExpiration(user.getRoles());
        
        Session session = new Session(sessionId, user.getId(), user.getEmail(), user.getRoles(),
                                    now, expiresAt, ipAddress, userAgent);
        
        // Store session
        ACTIVE_SESSIONS.put(sessionId, session);
        
        // Track user sessions
        USER_SESSIONS.computeIfAbsent(user.getId(), k -> new ArrayList<>()).add(sessionId);
        
        return session;
    }
    
    /**
     * Validate session by ID
     */
    public Session validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        
        Session session = ACTIVE_SESSIONS.get(sessionId);
        if (session == null) {
            return null;
        }
        
        // Check if session is valid
        if (!session.isValid()) {
            invalidateSession(sessionId);
            return null;
        }
        
        // Update last activity
        session.updateLastActivity();
        
        return session;
    }
    
    /**
     * Validate session using JWT token
     */
    public Session validateSessionByToken(String token) {
        try {
            AuthToken authToken = tokenService.validateToken(token);
            return validateSession(authToken.getSessionId());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Invalidate specific session
     */
    public boolean invalidateSession(String sessionId) {
        if (sessionId == null) {
            return false;
        }
        
        Session session = ACTIVE_SESSIONS.remove(sessionId);
        if (session != null) {
            session.setActive(false);
            
            // Remove from user sessions tracking
            List<String> userSessions = USER_SESSIONS.get(session.getUserId());
            if (userSessions != null) {
                userSessions.remove(sessionId);
                if (userSessions.isEmpty()) {
                    USER_SESSIONS.remove(session.getUserId());
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Invalidate all sessions for a user
     */
    public int invalidateUserSessions(int userId) {
        List<String> sessionIds = USER_SESSIONS.get(userId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return 0;
        }
        
        int invalidatedCount = 0;
        for (String sessionId : new ArrayList<>(sessionIds)) {
            if (invalidateSession(sessionId)) {
                invalidatedCount++;
            }
        }
        
        return invalidatedCount;
    }
    
    /**
     * Get active sessions for user
     */
    public List<Session> getUserSessions(int userId) {
        List<String> sessionIds = USER_SESSIONS.get(userId);
        if (sessionIds == null) {
            return new ArrayList<>();
        }
        
        return sessionIds.stream()
                .map(ACTIVE_SESSIONS::get)
                .filter(Objects::nonNull)
                .filter(Session::isValid)
                .collect(Collectors.toList());
    }
    
    /**
     * Get total active session count
     */
    public int getActiveSessionCount() {
        return (int) ACTIVE_SESSIONS.values().stream()
                .filter(Session::isValid)
                .count();
    }
    
    /**
     * Get all active sessions
     */
    public List<Session> getAllActiveSessions() {
        return ACTIVE_SESSIONS.values().stream()
                .filter(Session::isValid)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if user has active sessions
     */
    public boolean hasActiveSessions(int userId) {
        List<String> sessionIds = USER_SESSIONS.get(userId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return false;
        }
        
        return sessionIds.stream()
                .map(ACTIVE_SESSIONS::get)
                .filter(Objects::nonNull)
                .anyMatch(Session::isValid);
    }
    
    /**
     * Cleanup expired sessions
     */
    public int cleanupExpiredSessions() {
        int cleanedCount = 0;
        
        Iterator<Map.Entry<String, Session>> iterator = ACTIVE_SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Session> entry = iterator.next();
            Session session = entry.getValue();
            
            if (session.isExpired() || !session.isActive()) {
                iterator.remove();
                
                // Remove from user sessions
                List<String> userSessions = USER_SESSIONS.get(session.getUserId());
                if (userSessions != null) {
                    userSessions.remove(entry.getKey());
                    if (userSessions.isEmpty()) {
                        USER_SESSIONS.remove(session.getUserId());
                    }
                }
                
                cleanedCount++;
            }
        }
        
        return cleanedCount;
    }
    
    /**
     * Calculate session expiration based on user roles
     */
    private LocalDateTime calculateSessionExpiration(String[] userRoles) {
        if (userRoles == null || userRoles.length == 0) {
            return LocalDateTime.now().plusHours(1);
        }
        
        int maxTimeout = 1;
        for (String role : userRoles) {
            if (role != null) {
                int timeout = tokenService.getRoleTimeoutHours(role);
                if (timeout > maxTimeout) {
                    maxTimeout = timeout;
                }
            }
        }
        
        return LocalDateTime.now().plusHours(maxTimeout);
    }
    
    /**
     * Start automatic cleanup task
     */
    private static void startCleanupTask() {
        CLEANUP_TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    SessionManager.getInstance().cleanupExpiredSessions();
                } catch (Exception e) {
                    System.err.println("Session cleanup error: " + e.getMessage());
                }
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
    }
    
    /**
     * Shutdown cleanup timer
     */
    public static void shutdown() {
        CLEANUP_TIMER.cancel();
    }
    
    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        return SessionManagerHolder.INSTANCE;
    }
    
    // Singleton holder pattern
    private static class SessionManagerHolder {
        private static final SessionManager INSTANCE = new SessionManager();
    }
    
    /**
     * Get session statistics
     */
    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveSessions", getActiveSessionCount());
        stats.put("totalSessions", ACTIVE_SESSIONS.size());
        stats.put("usersWithSessions", USER_SESSIONS.size());
        
        // Count sessions by role
        Map<String, Integer> roleStats = new HashMap<>();
        ACTIVE_SESSIONS.values().stream()
                .filter(Session::isValid)
                .forEach(session -> {
                    for (String role : session.getRoles()) {
                        roleStats.put(role, roleStats.getOrDefault(role, 0) + 1);
                    }
                });
        stats.put("sessionsByRole", roleStats);
        
        return stats;
    }
}