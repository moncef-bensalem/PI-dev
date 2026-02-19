package com.nexus.desktop.util;

import com.nexus.desktop.model.User;

/**
 * Utility for role-based dashboard routing
 */
public final class DashboardRouter {

    private static final String ADMIN_DASHBOARD = "fxml/dashboard.fxml";
    private static final String CANDIDATE_DASHBOARD = "fxml/candidate_dashboard.fxml";

    private DashboardRouter() {}

    /**
     * Get the FXML path for the appropriate dashboard based on user roles.
     * - ROLE_CANDIDAT -> Candidate dashboard
     * - ROLE_ADMIN, ROLE_RH, ROLE_MANAGER -> Admin dashboard
     * - ROLE_USER (default) -> Candidate dashboard
     */
    public static String getDashboardForUser(User user) {
        if (user == null || user.getRoles() == null) {
            return ADMIN_DASHBOARD;
        }
        for (String role : user.getRoles()) {
            if (role != null) {
                String r = role.toUpperCase();
                if (r.contains("CANDIDAT") || "ROLE_USER".equals(r)) {
                    return CANDIDATE_DASHBOARD;
                }
            }
        }
        return ADMIN_DASHBOARD;
    }

    /**
     * Check if user is a candidate (should see candidate dashboard)
     */
    public static boolean isCandidate(User user) {
        if (user == null || user.getRoles() == null) return false;
        for (String role : user.getRoles()) {
            if (role != null && (role.toUpperCase().contains("CANDIDAT") || "ROLE_USER".equals(role.toUpperCase()))) {
                return true;
            }
        }
        return false;
    }
}
