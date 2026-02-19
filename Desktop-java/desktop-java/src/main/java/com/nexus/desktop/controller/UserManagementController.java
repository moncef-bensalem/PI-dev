package com.nexus.desktop.controller;

import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ResourceBundle;

/**
 * User Management Controller for CRUD operations on users
 * Handles user listing, creation, editing, and deletion
 */
public class UserManagementController implements Initializable {
    
    @FXML
    private ScrollPane usersScrollPane;
    
    @FXML
    private VBox usersContainer;
    
    // TableView elements removed for CardView implementation
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> roleFilter;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button activateButton;
    
    @FXML
    private Button refreshButton;
    
    private UserDAO userDAO;
    private ObservableList<User> userObservableList;
    private User currentUser; // Admin user performing operations
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        setupFilters();
        loadUsers();
        setupEventHandlers();
    }
    
    /**
     * Set current admin user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Enable/disable buttons based on user permissions
        updateButtonPermissions();
    }
    

    
    /**
     * Setup filters
     */
    private void setupFilters() {
        // Role filter
        roleFilter.getItems().addAll("All Roles", "ADMIN", "RH", "CANDIDAT", "USER");
        roleFilter.getSelectionModel().selectFirst();
        
        // Status filter
        statusFilter.getItems().addAll("All Statuses", "Active", "Inactive");
        statusFilter.getSelectionModel().selectFirst();
    }
    
    /**
     * Load users into cards
     */
    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            userObservableList = FXCollections.observableArrayList(users);
            displayUsersAsCards();
        } catch (SQLException e) {
            showError("Failed to load users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Display users as cards
     */
    private void displayUsersAsCards() {
        usersContainer.getChildren().clear();
        
        if (userObservableList != null) {
            for (User user : userObservableList) {
                try {
                    VBox userCard = new com.nexus.desktop.controller.UserCardController().createUserCard(user, this);
                    usersContainer.getChildren().add(userCard);
                } catch (Exception e) {
                    System.err.println("Failed to create card for user: " + user.getEmail());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Search field with validation
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateSearchInput(newValue);
            filterUsers();
        });
        
        // Filter dropdowns with validation
        roleFilter.setOnAction(event -> {
            validateRoleFilter();
            filterUsers();
        });
        statusFilter.setOnAction(event -> {
            validateStatusFilter();
            filterUsers();
        });
        
        // Button actions
        addButton.setOnAction(event -> showAddUserDialog());
        editButton.setOnAction(event -> showEditUserDialog());
        deleteButton.setOnAction(event -> deleteUser());
        refreshButton.setOnAction(event -> refreshUsers());
    }
    
    /**
     * Validate search input
     */
    private void validateSearchInput(String searchTerm) {
        if (searchTerm != null) {
            // Check for invalid characters
            if (searchTerm.matches(".*[<>\"].*")) {
                showWarning("Search term contains invalid characters (<, >, \"");
                searchField.setText(searchTerm.replaceAll("[<>\"].*", ""));
                return;
            }
            
            // Limit search length
            if (searchTerm.length() > 50) {
                showWarning("Search term is too long (maximum 50 characters)");
                searchField.setText(searchTerm.substring(0, 50));
                return;
            }
        }
    }
    
    /**
     * Validate role filter selection
     */
    private void validateRoleFilter() {
        String selectedRole = roleFilter.getSelectionModel().getSelectedItem();
        if (selectedRole == null || selectedRole.trim().isEmpty()) {
            roleFilter.getSelectionModel().select("All Roles"); // Reset to default
        }
    }
    
    /**
     * Validate status filter selection
     */
    private void validateStatusFilter() {
        String selectedStatus = statusFilter.getSelectionModel().getSelectedItem();
        if (selectedStatus == null || selectedStatus.trim().isEmpty()) {
            statusFilter.getSelectionModel().select("All Statuses"); // Reset to default
        }
    }
    
    /**
     * Filter users based on search and filters
     */
    private void filterUsers() {
        if (userObservableList == null) return;
        
        String searchTerm = searchField.getText().toLowerCase().trim();
        String selectedRole = roleFilter.getSelectionModel().getSelectedItem();
        String selectedStatus = statusFilter.getSelectionModel().getSelectedItem();
        
        // Validate inputs before filtering
        if (!isSearchInputValid(searchTerm)) {
            return;
        }
        
        if (!isRoleFilterValid(selectedRole)) {
            return;
        }
        
        if (!isStatusFilterValid(selectedStatus)) {
            return;
        }
        
        // Apply filters
        ObservableList<User> filteredUsers = userObservableList.filtered(user -> {
            // Search filter
            boolean matchesSearch = searchTerm.isEmpty() || 
                user.getFullName().toLowerCase().contains(searchTerm) ||
                user.getEmail().toLowerCase().contains(searchTerm);
            
            // Role filter
            boolean matchesRole = selectedRole == null || "All Roles".equals(selectedRole) || 
                containsRole(user.getRoles(), selectedRole);
            
            // Status filter
            boolean matchesStatus = selectedStatus == null || "All Statuses".equals(selectedStatus) ||
                (selectedStatus.equals("Active") && user.isActive()) ||
                (selectedStatus.equals("Inactive") && !user.isActive());
            
            return matchesSearch && matchesRole && matchesStatus;
        });
        
        // Display filtered users
        usersContainer.getChildren().clear();
        for (User user : filteredUsers) {
            try {
                VBox userCard = new com.nexus.desktop.controller.UserCardController().createUserCard(user, this);
                usersContainer.getChildren().add(userCard);
            } catch (Exception e) {
                System.err.println("Failed to create card for user: " + user.getEmail());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Check if search input is valid
     */
    private boolean isSearchInputValid(String searchTerm) {
        if (searchTerm == null) return true;
        
        // Check for invalid characters
        if (searchTerm.matches(".*[<>\"].*")) {
            return false;
        }
        
        // Check length
        if (searchTerm.length() > 50) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if role filter is valid
     */
    private boolean isRoleFilterValid(String selectedRole) {
        if (selectedRole == null) return true;
        
        // Check if it's a valid role option
        return Arrays.asList("All Roles", "ADMIN", "RH", "CANDIDAT", "USER").contains(selectedRole);
    }
    
    /**
     * Check if status filter is valid
     */
    private boolean isStatusFilterValid(String selectedStatus) {
        if (selectedStatus == null) return true;
        
        // Check if it's a valid status option
        return Arrays.asList("All Statuses", "Active", "Inactive").contains(selectedStatus);
    }
    
    /**
     * Check if user has the specified role (handles ROLE_ prefix variations)
     */
    private boolean containsRole(String[] userRoles, String filterRole) {
        if (userRoles == null || filterRole == null) return false;
        
        // Handle "All Roles" case
        if ("All Roles".equals(filterRole)) return true;
        
        // Check each user role against the filter role
        for (String userRole : userRoles) {
            if (userRole == null) continue;
            
            // Normalize both roles for comparison
            String normalizedUserRole = userRole.replace("ROLE_", "").toUpperCase();
            String normalizedFilterRole = filterRole.replace("ROLE_", "").toUpperCase();
            
            if (normalizedUserRole.equals(normalizedFilterRole)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Show add user dialog
     */
    private void showAddUserDialog() {
        try {
            openUserForm(null);
        } catch (Exception e) {
            showError("Failed to open user creation form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show edit user dialog
     */
    private void showEditUserDialog() {
        // For CardView, we need to get selected user from context or pass it from card
        User selectedUser = getSelectedUserFromCards();
        if (selectedUser == null) {
            showWarning("Please select a user to edit");
            return;
        }
        
        try {
            openUserForm(selectedUser);
        } catch (Exception e) {
            showError("Failed to open user edit form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Open user form for creation or editing
     */
    private void openUserForm(User user) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL resourceUrl = getClass().getClassLoader().getResource("fxml/user_form.fxml");
        if (resourceUrl == null) {
            throw new Exception("User form FXML file not found");
        }
        
        loader.setLocation(resourceUrl);
        Parent root = loader.load();
        
        // Get controller and configure
        UserFormController formController = loader.getController();
        formController.setUser(user);
        formController.setOnSaveCallback(this::refreshUsers);
        
        // Create dialog window
        Stage stage = new Stage();
        String title = user == null ? "Add New User" : "Edit User";
        stage.setTitle(title + " - NEXUS User Management");
        stage.setScene(new Scene(root, 500, 400));
        stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        stage.initOwner(usersScrollPane.getScene().getWindow());
        stage.setResizable(false);
        stage.showAndWait();
    }
    
    /**
     * Activate user
     */
    private void activateUser() {
        // For CardView, we need to get selected user from context or pass it from card
        User selectedUser = getSelectedUserFromCards();
        if (selectedUser == null) {
            showWarning("Please select a user to activate");
            return;
        }
        
        // Prevent activating yourself if already active (shouldn't happen but just in case)
        if (currentUser != null && currentUser.getId() == selectedUser.getId() && selectedUser.isActive()) {
            showWarning("This user is already active");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Activate User");
        confirmation.setHeaderText("Activate User Confirmation");
        confirmation.setContentText("Are you sure you want to activate user " + selectedUser.getFullName() + "?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selectedUser.setActive(true);
                    userDAO.update(selectedUser);
                    showInfo("User activated successfully");
                    refreshUsers();
                } catch (SQLException e) {
                    showError("Failed to activate user: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Delete user (soft delete - set inactive)
     */
    private void deleteUser() {
        // For CardView, we need to get selected user from context or pass it from card
        User selectedUser = getSelectedUserFromCards();
        if (selectedUser == null) {
            showWarning("Please select a user to delete");
            return;
        }
        
        // Prevent deleting yourself
        if (currentUser != null && currentUser.getId() == selectedUser.getId()) {
            showError("You cannot delete your own account");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Delete User Confirmation");
        confirmation.setContentText("Are you sure you want to delete user " + selectedUser.getFullName() + "?\n\n" +
            "This will deactivate the account.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selectedUser.setActive(false);
                    userDAO.update(selectedUser);
                    showInfo("User deactivated successfully");
                    refreshUsers();
                } catch (SQLException e) {
                    showError("Failed to delete user: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Delete user permanently (hard delete)
     */
    private void deleteUserPermanently() {
        // For CardView, we need to get selected user from context or pass it from card
        User selectedUser = getSelectedUserFromCards();
        if (selectedUser == null) {
            showWarning("Please select a user to delete");
            return;
        }
        
        // Prevent deleting yourself
        if (currentUser != null && currentUser.getId() == selectedUser.getId()) {
            showError("You cannot delete your own account");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User Permanently");
        confirmation.setHeaderText("Permanent Delete Confirmation");
        confirmation.setContentText("Are you sure you want to PERMANENTLY DELETE user " + selectedUser.getFullName() + "?\n\n" +
            "THIS ACTION CANNOT BE UNDONE!");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.delete(selectedUser.getId());
                    showInfo("User deleted permanently");
                    refreshUsers();
                } catch (SQLException e) {
                    showError("Failed to delete user permanently: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Get selected user from cards (temporary implementation)
     */
    private User getSelectedUserFromCards() {
        // Temporary implementation - in a real CardView, you would track selection
        // For now, we'll just return the first user or show a message
        if (userObservableList != null && !userObservableList.isEmpty()) {
            return userObservableList.get(0);
        }
        return null;
    }
    
    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
        // For CardView, buttons are always enabled
        editButton.setDisable(false);
        deleteButton.setDisable(false);
        activateButton.setDisable(false);
    }
    
    /**
     * Refresh user list
     */
    private void refreshUsers() {
        loadUsers();
        searchField.clear();
        roleFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        showInfo("User list refreshed");
    }
    
    /**
     * Update button permissions based on current user role
     */
    private void updateButtonPermissions() {
        if (currentUser == null) {
            addButton.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }
        
        // Check if current user has admin role
        boolean isAdmin = false;
        for (String role : currentUser.getRoles()) {
            if (role.contains("ADMIN")) {
                isAdmin = true;
                break;
            }
        }
        
        addButton.setDisable(!isAdmin);
        editButton.setDisable(!isAdmin);
        deleteButton.setDisable(!isAdmin);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("User Management Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show warning message
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("User Management Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info message
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Close window
     */
    @FXML
    private void onAddUserClick() {
        showAddUserDialog();
    }
    
    @FXML
    private void onEditUserClick() {
        showEditUserDialog();
    }
    
    @FXML
    private void onDeleteUserClick() {
        deleteUser();
    }
    
    @FXML
    private void onActivateUserClick() {
        activateUser();
    }
    
    @FXML
    private void onCloseButtonClick() {
        Stage stage = (Stage) usersScrollPane.getScene().getWindow();
        stage.close();
    }
}