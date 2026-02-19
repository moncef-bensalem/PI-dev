package com.nexus.desktop.model;

/**
 * Admin user model extending base User
 */
public class Admin extends User {
    
    public Admin() {
        super();
        // Set default role for Admin
        setRoles(new String[]{"ROLE_ADMIN"});
    }
    
    public Admin(int id, String email, String firstName, String lastName, String passwordHash, boolean active) {
        super(id, email, firstName, lastName, passwordHash, new String[]{"ROLE_ADMIN"}, active);
    }
    
    public Admin(int id, String email, String firstName, String lastName, boolean active) {
        super(id, email, firstName, lastName, new String[]{"ROLE_ADMIN"}, active);
    }
    
    // Admin-specific methods can be added here
}