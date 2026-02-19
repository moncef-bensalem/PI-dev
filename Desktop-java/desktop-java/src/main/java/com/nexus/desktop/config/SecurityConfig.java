package com.nexus.desktop.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Security configuration class for managing authentication and authorization settings
 * Contains security policies, timeouts, and configuration values
 */
public class SecurityConfig {
    // Application security settings
    private static final String APPLICATION_NAME = "NEXUS Desktop Application";
    private static final String APPLICATION_VERSION = "1.0.0";
    
    // Session and token configuration
    private static final Duration DEFAULT_SESSION_TIMEOUT = Duration.ofHours(2);
    private static final Duration MIN_SESSION_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration MAX_SESSION_TIMEOUT = Duration.ofHours(24);
    
    // Password security settings
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_LOCKOUT_DURATION = Duration.ofMinutes(15);
    
    // Role-based session timeouts (hours)
    private static final Map<String, Duration> ROLE_SESSION_TIMEOUTS = new HashMap<>();
    
    static {
        ROLE_SESSION_TIMEOUTS.put("ROLE_ADMIN", Duration.ofHours(8));
        ROLE_SESSION_TIMEOUTS.put("ROLE_RH", Duration.ofHours(4));
        ROLE_SESSION_TIMEOUTS.put("ROLE_MANAGER", Duration.ofHours(2));
        ROLE_SESSION_TIMEOUTS.put("ROLE_CANDIDAT", Duration.ofHours(1));
        ROLE_SESSION_TIMEOUTS.put("ROLE_USER", Duration.ofHours(1));
    }
    
    // Security features flags
    private static final boolean ENABLE_TOKEN_REFRESH = true;
    private static final boolean ENABLE_SESSION_TIMEOUT_WARNING = true;
    private static final boolean ENABLE_AUTO_LOGOUT = true;
    private static final boolean ENABLE_CONCURRENT_SESSIONS = false;
    
    // JWT configuration
    private static final String JWT_ISSUER = "NEXUS-DESKTOP";
    private static final String JWT_AUDIENCE = "NEXUS-CLIENTS";
    private static final Duration JWT_DEFAULT_EXPIRATION = Duration.ofHours(2);
    
    // UI security settings
    private static final int AUTO_LOGOUT_WARNING_MINUTES = 5;
    private static final boolean SHOW_SESSION_TIMER = true;
    private static final boolean ENABLE_SCREEN_LOCK = true;
    
    // Private constructor to prevent instantiation
    private SecurityConfig() {}
    
    // Getters for configuration values
    public static String getApplicationName() {
        return APPLICATION_NAME;
    }
    
    public static String getApplicationVersion() {
        return APPLICATION_VERSION;
    }
    
    public static Duration getDefaultSessionTimeout() {
        return DEFAULT_SESSION_TIMEOUT;
    }
    
    public static Duration getMinSessionTimeout() {
        return MIN_SESSION_TIMEOUT;
    }
    
    public static Duration getMaxSessionTimeout() {
        return MAX_SESSION_TIMEOUT;
    }
    
    public static int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
    
    public static int getMaxLoginAttempts() {
        return MAX_LOGIN_ATTEMPTS;
    }
    
    public static Duration getLoginLockoutDuration() {
        return LOGIN_LOCKOUT_DURATION;
    }
    
    public static Duration getRoleSessionTimeout(String role) {
        if (role == null) {
            return DEFAULT_SESSION_TIMEOUT;
        }
        return ROLE_SESSION_TIMEOUTS.getOrDefault(role.toUpperCase(), DEFAULT_SESSION_TIMEOUT);
    }
    
    public static boolean isTokenRefreshEnabled() {
        return ENABLE_TOKEN_REFRESH;
    }
    
    public static boolean isSessionTimeoutWarningEnabled() {
        return ENABLE_SESSION_TIMEOUT_WARNING;
    }
    
    public static boolean isAutoLogoutEnabled() {
        return ENABLE_AUTO_LOGOUT;
    }
    
    public static boolean isConcurrentSessionsEnabled() {
        return ENABLE_CONCURRENT_SESSIONS;
    }
    
    public static String getJwtIssuer() {
        return JWT_ISSUER;
    }
    
    public static String getJwtAudience() {
        return JWT_AUDIENCE;
    }
    
    public static Duration getJwtDefaultExpiration() {
        return JWT_DEFAULT_EXPIRATION;
    }
    
    public static int getAutoLogoutWarningMinutes() {
        return AUTO_LOGOUT_WARNING_MINUTES;
    }
    
    public static boolean isShowSessionTimer() {
        return SHOW_SESSION_TIMER;
    }
    
    public static boolean isEnableScreenLock() {
        return ENABLE_SCREEN_LOCK;
    }
    
    /**
     * Get all role session timeouts
     */
    public static Map<String, Duration> getAllRoleTimeouts() {
        return new HashMap<>(ROLE_SESSION_TIMEOUTS);
    }
    
    /**
     * Validate session timeout duration
     */
    public static boolean isValidSessionTimeout(Duration timeout) {
        return timeout != null && 
               !timeout.isNegative() && 
               timeout.compareTo(MIN_SESSION_TIMEOUT) >= 0 && 
               timeout.compareTo(MAX_SESSION_TIMEOUT) <= 0;
    }
    
    /**
     * Get session timeout in minutes for a role
     */
    public static long getRoleSessionTimeoutMinutes(String role) {
        return getRoleSessionTimeout(role).toMinutes();
    }
    
    /**
     * Get formatted security information
     */
    public static String getSecurityInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== Security Configuration ===\n");
        info.append("Application: ").append(APPLICATION_NAME).append(" v").append(APPLICATION_VERSION).append("\n");
        info.append("Default Session Timeout: ").append(DEFAULT_SESSION_TIMEOUT.toHours()).append(" hours\n");
        info.append("Token Refresh: ").append(ENABLE_TOKEN_REFRESH ? "Enabled" : "Disabled").append("\n");
        info.append("Auto Logout: ").append(ENABLE_AUTO_LOGOUT ? "Enabled" : "Disabled").append("\n");
        info.append("Concurrent Sessions: ").append(ENABLE_CONCURRENT_SESSIONS ? "Allowed" : "Not Allowed").append("\n");
        info.append("\nRole Timeouts:\n");
        
        ROLE_SESSION_TIMEOUTS.forEach((role, timeout) -> 
            info.append("  ").append(role).append(": ").append(timeout.toHours()).append(" hours\n")
        );
        
        return info.toString();
    }
    
    /**
     * Check if role has elevated privileges
     */
    public static boolean isElevatedRole(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_RH".equals(role);
    }
    
    /**
     * Get security level for role (1-5, 5 being highest)
     */
    public static int getRoleSecurityLevel(String role) {
        switch (role != null ? role.toUpperCase() : "") {
            case "ROLE_ADMIN": return 5;
            case "ROLE_RH": return 4;
            case "ROLE_MANAGER": return 3;
            case "ROLE_CANDIDAT": return 2;
            default: return 1;
        }
    }
}