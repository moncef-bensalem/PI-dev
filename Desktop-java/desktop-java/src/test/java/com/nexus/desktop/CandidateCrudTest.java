package com.nexus.desktop;

import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.model.User;
import com.nexus.desktop.service.AuthenticationService;
import com.nexus.desktop.util.DatabaseManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Comprehensive test class for Candidate CRUD operations
 * Tests adding, editing, and deleting candidates, then performs cleanup
 */
public class CandidateCrudTest {
    
    private UserDAO userDAO;
    private AuthenticationService authService;
    private User testCandidate;
    private int createdUserId = -1;
    
    public static void main(String[] args) {
        CandidateCrudTest test = new CandidateCrudTest();
        test.runAllTests();
    }
    
    public CandidateCrudTest() {
        // Initialize database connection and services
        try {
            // DatabaseManager initializes automatically through static block
            // Just verify connection is available
            if (!DatabaseManager.isDatabaseAvailable()) {
                throw new RuntimeException("Database connection not available");
            }
            userDAO = new UserDAO();
            authService = new AuthenticationService();
            System.out.println("Test environment initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize test environment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run all CRUD tests in sequence
     */
    public void runAllTests() {
        System.out.println("=== Starting Candidate CRUD Test Suite ===\n");
        
        try {
            // Test 1: Create/Add Candidate
            testCreateCandidate();
            
            // Test 2: Read/Find Candidate
            testReadCandidate();
            
            // Test 3: Update/Edit Candidate
            testUpdateCandidate();
            
            // Test 4: Delete Candidate
            testDeleteCandidate();
            
            // Test 5: Cleanup
            performCleanup();
            
            System.out.println("\n=== All tests completed successfully ===");
            
        } catch (Exception e) {
            System.err.println("Test suite failed: " + e.getMessage());
            e.printStackTrace();
            
            // Attempt cleanup even if tests fail
            try {
                performCleanup();
            } catch (Exception cleanupEx) {
                System.err.println("Cleanup also failed: " + cleanupEx.getMessage());
            }
        }
    }
    
    /**
     * Test creating a new candidate
     */
    private void testCreateCandidate() throws SQLException {
        System.out.println("1. Testing Candidate Creation...");
        
        // Create test candidate data
        User candidate = new User();
        candidate.setEmail("test.candidate@example.com");
        candidate.setFirstName("Test");
        candidate.setLastName("Candidate");
        candidate.setPasswordHash("testpassword123"); // In real scenario, this would be hashed
        candidate.setActive(true);
        candidate.setRoles(new String[]{"ROLE_CANDIDAT"});
        
        // Save to database
        userDAO.save(candidate);
        
        // Verify creation
        User savedCandidate = userDAO.findByEmail("test.candidate@example.com");
        if (savedCandidate != null) {
            createdUserId = savedCandidate.getId();
            testCandidate = savedCandidate;
            System.out.println("✓ Candidate created successfully with ID: " + createdUserId);
            System.out.println("  Name: " + savedCandidate.getFullName());
            System.out.println("  Email: " + savedCandidate.getEmail());
            System.out.println("  Role: " + String.join(", ", savedCandidate.getRoles()));
        } else {
            throw new RuntimeException("Failed to create candidate - not found in database");
        }
    }
    
    /**
     * Test reading/fetching candidate data
     */
    private void testReadCandidate() throws SQLException {
        System.out.println("\n2. Testing Candidate Read Operations...");
        
        if (createdUserId == -1) {
            throw new RuntimeException("No candidate created to read");
        }
        
        // Test findById
        User foundById = userDAO.findById(createdUserId);
        if (foundById != null) {
            System.out.println("✓ Find by ID successful");
            System.out.println("  Found: " + foundById.getFullName() + " (" + foundById.getEmail() + ")");
        } else {
            throw new RuntimeException("Failed to find candidate by ID: " + createdUserId);
        }
        
        // Test findByEmail
        User foundByEmail = userDAO.findByEmail("test.candidate@example.com");
        if (foundByEmail != null) {
            System.out.println("✓ Find by email successful");
        } else {
            throw new RuntimeException("Failed to find candidate by email");
        }
        
        // Test findAll
        List<User> allUsers = userDAO.findAll();
        System.out.println("✓ Found " + allUsers.size() + " total users in database");
        
        // Test search functionality
        List<User> searchResults = userDAO.searchUsers("Test");
        System.out.println("✓ Search found " + searchResults.size() + " users matching 'Test'");
    }
    
    /**
     * Test updating/editing candidate data
     */
    private void testUpdateCandidate() throws SQLException {
        System.out.println("\n3. Testing Candidate Update Operations...");
        
        if (testCandidate == null) {
            throw new RuntimeException("No candidate available to update");
        }
        
        // Modify candidate data
        testCandidate.setFirstName("Updated");
        testCandidate.setLastName("Candidate-Test");
        testCandidate.setEmail("updated.candidate@example.com");
        
        // Update in database
        userDAO.update(testCandidate);
        
        // Verify update
        User updatedCandidate = userDAO.findById(createdUserId);
        if (updatedCandidate != null && 
            "Updated".equals(updatedCandidate.getFirstName()) &&
            "Candidate-Test".equals(updatedCandidate.getLastName()) &&
            "updated.candidate@example.com".equals(updatedCandidate.getEmail())) {
            System.out.println("✓ Candidate updated successfully");
            System.out.println("  New name: " + updatedCandidate.getFullName());
            System.out.println("  New email: " + updatedCandidate.getEmail());
            testCandidate = updatedCandidate; // Update reference
        } else {
            throw new RuntimeException("Failed to verify candidate update");
        }
        
        // Test profile update (partial update)
        testCandidate.setFirstName("Profile");
        testCandidate.setLastName("Update");
        userDAO.updateProfile(testCandidate);
        
        User profileUpdated = userDAO.findById(createdUserId);
        if (profileUpdated != null && 
            "Profile".equals(profileUpdated.getFirstName()) &&
            "Update".equals(profileUpdated.getLastName())) {
            System.out.println("✓ Profile update successful");
            testCandidate = profileUpdated;
        }
    }
    
    /**
     * Test deleting candidate
     */
    private void testDeleteCandidate() throws SQLException {
        System.out.println("\n4. Testing Candidate Deletion...");
        
        if (createdUserId == -1) {
            throw new RuntimeException("No candidate created to delete");
        }
        
        // Verify candidate exists before deletion
        User candidateToDelete = userDAO.findById(createdUserId);
        if (candidateToDelete == null) {
            throw new RuntimeException("Candidate not found before deletion");
        }
        
        System.out.println("  Candidate to delete: " + candidateToDelete.getFullName());
        
        // Delete candidate
        userDAO.delete(createdUserId);
        
        // Verify deletion
        User deletedCandidate = userDAO.findById(createdUserId);
        if (deletedCandidate == null) {
            System.out.println("✓ Candidate deleted successfully");
            createdUserId = -1; // Reset ID since candidate is deleted
            testCandidate = null;
        } else {
            throw new RuntimeException("Failed to delete candidate - still exists in database");
        }
    }
    
    /**
     * Perform cleanup of any remaining test data
     */
    private void performCleanup() throws SQLException {
        System.out.println("\n5. Performing Cleanup...");
        
        // Clean up any remaining test candidates
        cleanupTestCandidates();
        
        // Verify database integrity
        verifyDatabaseState();
        
        System.out.println("✓ Cleanup completed successfully");
    }
    
    /**
     * Remove any test candidates that might remain
     */
    private void cleanupTestCandidates() throws SQLException {
        System.out.println("  Cleaning up test candidates...");
        
        // Find and delete test candidates by email pattern
        List<User> allUsers = userDAO.findAll();
        int cleanupCount = 0;
        
        for (User user : allUsers) {
            if (user.getEmail() != null && 
                (user.getEmail().contains("test.candidate") || 
                 user.getEmail().contains("updated.candidate"))) {
                System.out.println("    Deleting test user: " + user.getEmail());
                userDAO.delete(user.getId());
                cleanupCount++;
            }
        }
        
        System.out.println("  ✓ Cleaned up " + cleanupCount + " test candidates");
    }
    
    /**
     * Verify database is in a clean state
     */
    private void verifyDatabaseState() throws SQLException {
        System.out.println("  Verifying database state...");
        
        // Check that our test candidates are gone
        User testUser1 = userDAO.findByEmail("test.candidate@example.com");
        User testUser2 = userDAO.findByEmail("updated.candidate@example.com");
        
        if (testUser1 == null && testUser2 == null) {
            System.out.println("  ✓ No test candidates remaining in database");
        } else {
            System.out.println("  ⚠ Warning: Some test candidates still exist");
        }
        
        // Count total users
        List<User> allUsers = userDAO.findAll();
        System.out.println("  ✓ Database contains " + allUsers.size() + " total users");
        
        // Verify database connection is still working
        boolean connectionOk = DatabaseManager.isDatabaseAvailable();
        if (connectionOk) {
            System.out.println("  ✓ Database connection verified");
        } else {
            System.out.println("  ⚠ Database connection issue detected");
        }
    }
    
    /**
     * Utility method to create a test candidate for manual testing
     */
    public User createTestCandidate(String firstName, String lastName, String email) throws SQLException {
        User candidate = new User();
        candidate.setEmail(email);
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setPasswordHash("testpassword123");
        candidate.setActive(true);
        candidate.setRoles(new String[]{"ROLE_CANDIDAT"});
        
        userDAO.save(candidate);
        System.out.println("Created test candidate: " + candidate.getFullName() + " (" + email + ")");
        return candidate;
    }
    
    /**
     * Utility method to list all candidates
     */
    public void listAllCandidates() throws SQLException {
        List<User> candidates = userDAO.getUsersByRole("CANDIDAT");
        System.out.println("Found " + candidates.size() + " candidates:");
        for (User candidate : candidates) {
            System.out.println("  - " + candidate.getFullName() + " (" + candidate.getEmail() + ")");
        }
    }
}