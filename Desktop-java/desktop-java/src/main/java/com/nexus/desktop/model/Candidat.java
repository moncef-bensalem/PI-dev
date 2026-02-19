package com.nexus.desktop.model;

/**
 * Candidat user model extending base User
 */
public class Candidat extends User {
    
    public Candidat() {
        super();
        // Set default role for Candidat
        setRoles(new String[]{"ROLE_CANDIDAT"});
    }
    
    public Candidat(int id, String email, String firstName, String lastName, String passwordHash, boolean active) {
        super(id, email, firstName, lastName, passwordHash, new String[]{"ROLE_CANDIDAT"}, active);
    }
    
    public Candidat(int id, String email, String firstName, String lastName, boolean active) {
        super(id, email, firstName, lastName, new String[]{"ROLE_CANDIDAT"}, active);
    }
    
    // Candidat-specific methods can be added here
}