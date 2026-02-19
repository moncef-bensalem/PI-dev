package com.nexus.desktop.model;

/**
 * RH (Human Resources) user model extending base User
 */
public class RH extends User {
    
    public RH() {
        super();
        // Set default role for RH
        setRoles(new String[]{"ROLE_RH"});
    }
    
    public RH(int id, String email, String firstName, String lastName, String passwordHash, boolean active) {
        super(id, email, firstName, lastName, passwordHash, new String[]{"ROLE_RH"}, active);
    }
    
    public RH(int id, String email, String firstName, String lastName, boolean active) {
        super(id, email, firstName, lastName, new String[]{"ROLE_RH"}, active);
    }
    
    // RH-specific methods can be added here
}