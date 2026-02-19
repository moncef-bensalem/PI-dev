package com.nexus.desktop.service;

import com.nexus.desktop.model.AuthToken;
import com.nexus.desktop.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling JWT token operations
 * Manages token generation, validation, and security
 */
public class TokenService {
    // Secret key for JWT signing - HS512 requires minimum 64 bytes (512 bits) per RFC 7518
    private static final String SECRET_KEY = "nexus-desktop-app-secret-key-for-jwt-token-generation-2026-secure-minimum-64bytes!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    
    // Token expiration times by role (in hours)
    private static final Map<String, Integer> ROLE_TIMEOUTS = new ConcurrentHashMap<>();
    
    static {
        ROLE_TIMEOUTS.put("ROLE_ADMIN", 8);      // 8 hours
        ROLE_TIMEOUTS.put("ROLE_RH", 4);         // 4 hours
        ROLE_TIMEOUTS.put("ROLE_MANAGER", 2);    // 2 hours
        ROLE_TIMEOUTS.put("ROLE_CANDIDAT", 1);   // 1 hour
        ROLE_TIMEOUTS.put("ROLE_USER", 1);       // 1 hour (default)
    }
    
    /**
     * Generate JWT token for authenticated user
     */
    public AuthToken generateToken(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Invalid user for token generation");
        }
        
        // Generate unique session ID
        String sessionId = UUID.randomUUID().toString();
        
        // Determine expiration time based on user roles
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = calculateExpirationTime(user.getRoles());
        
        // Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles());
        claims.put("sessionId", sessionId);
        
        // Generate JWT token
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(issuedAt.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(KEY, SignatureAlgorithm.HS512)
                .compact();
        
        // Create and return AuthToken
        return new AuthToken(token, user.getId(), user.getEmail(), user.getRoles(), 
                           issuedAt, expiresAt, sessionId);
    }
    
    /**
     * Validate JWT token and extract user information
     */
    public AuthToken validateToken(String token) throws JwtException {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("Token is null or empty");
        }
        
        try {
            // Parse and validate token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Extract claims
            Integer userId = claims.get("userId", Integer.class);
            String email = claims.getSubject();
            String sessionId = claims.get("sessionId", String.class);
            
            // Extract roles array
            List<String> rolesList = claims.get("roles", List.class);
            String[] roles = rolesList != null ? 
                rolesList.toArray(new String[0]) : new String[]{"ROLE_USER"};
            
            // Extract timestamps
            Date issuedAtDate = claims.getIssuedAt();
            Date expirationDate = claims.getExpiration();
            
            LocalDateTime issuedAt = issuedAtDate != null ? 
                issuedAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now();
            LocalDateTime expiresAt = expirationDate != null ? 
                expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now().plusHours(1);
            
            // Create AuthToken
            AuthToken authToken = new AuthToken(token, userId, email, roles, issuedAt, expiresAt, sessionId);
            
            // Check if token is expired
            if (authToken.isExpired()) {
                throw new ExpiredJwtException(null, claims, "Token has expired");
            }
            
            return authToken;
            
        } catch (JwtException e) {
            throw new JwtException("Invalid token: " + e.getMessage());
        }
    }
    
    /**
     * Refresh existing token (extend expiration)
     */
    public AuthToken refreshToken(AuthToken oldToken) {
        if (oldToken == null || oldToken.isExpired()) {
            throw new IllegalArgumentException("Cannot refresh expired or null token");
        }
        
        // Create user object from token data
        User user = new User();
        user.setId(oldToken.getUserId());
        user.setEmail(oldToken.getEmail());
        user.setRoles(oldToken.getRoles());
        
        // Generate new token
        return generateToken(user);
    }
    
    /**
     * Check if token has specific role
     */
    public boolean hasRole(String token, String requiredRole) {
        try {
            AuthToken authToken = validateToken(token);
            return authToken.hasRole(requiredRole);
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * Check if token has any of the required roles
     */
    public boolean hasAnyRole(String token, String... requiredRoles) {
        try {
            AuthToken authToken = validateToken(token);
            return authToken.hasAnyRole(requiredRoles);
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * Calculate expiration time based on user roles
     */
    private LocalDateTime calculateExpirationTime(String[] userRoles) {
        if (userRoles == null || userRoles.length == 0) {
            return LocalDateTime.now().plusHours(1); // Default 1 hour
        }
        
        // Find the maximum timeout among user roles
        int maxTimeout = 1; // Default minimum 1 hour
        for (String role : userRoles) {
            if (role != null) {
                Integer timeout = ROLE_TIMEOUTS.get(role.toUpperCase());
                if (timeout != null && timeout > maxTimeout) {
                    maxTimeout = timeout;
                }
            }
        }
        
        return LocalDateTime.now().plusHours(maxTimeout);
    }
    
    /**
     * Get token expiration time in minutes
     */
    public long getTokenExpirationMinutes(String token) {
        try {
            AuthToken authToken = validateToken(token);
            return java.time.Duration.between(
                authToken.getIssuedAt(), 
                authToken.getExpiresAt()
            ).toMinutes();
        } catch (JwtException e) {
            return 0;
        }
    }
    
    /**
     * Check if token is about to expire (within 10 minutes)
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            AuthToken authToken = validateToken(token);
            LocalDateTime soon = LocalDateTime.now().plusMinutes(10);
            return authToken.getExpiresAt().isBefore(soon);
        } catch (JwtException e) {
            return true; // Treat invalid tokens as expiring
        }
    }
    
    /**
     * Get role-specific timeout in hours
     */
    public int getRoleTimeoutHours(String role) {
        if (role == null) return 1;
        return ROLE_TIMEOUTS.getOrDefault(role.toUpperCase(), 1);
    }
    
    /**
     * Update secret key (for security rotation)
     */
    public void updateSecretKey(String newSecretKey) {
        if (newSecretKey == null || newSecretKey.length() < 32) {
            throw new IllegalArgumentException("Secret key must be at least 32 characters long");
        }
        // Note: In production, this should involve more sophisticated key rotation
    }
}