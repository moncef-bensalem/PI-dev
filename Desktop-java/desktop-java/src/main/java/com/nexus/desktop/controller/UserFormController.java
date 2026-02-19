package com.nexus.desktop.controller;

import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * User Form Controller for creating and editing users
 * Handles user input validation and database operations
 */
public class UserFormController implements Initializable {
    
    @FXML
    private Label formTitle;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private ComboBox<String> roleComboBox;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button generatePasswordButton;
    
    @FXML
    private ComboBox<String> statusComboBox;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button saveButton;
    
    private UserDAO userDAO;
    private User currentUser; // User being edited (null for new user)
    private Runnable onSaveCallback; // Callback to refresh parent list
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        setupForm();
    }
    
    /**
     * Setup form components
     */
    private void setupForm() {
        // Setup role dropdown
        roleComboBox.setItems(FXCollections.observableArrayList(
            "ROLE_ADMIN", "ROLE_RH", "ROLE_MANAGER", "ROLE_CANDIDAT"
        ));
        roleComboBox.getSelectionModel().selectFirst();
        
        // Setup status dropdown
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Active", "Inactive"
        ));
        statusComboBox.getSelectionModel().select("Active");
        
        // Add input validation listeners
        addValidationListeners();
    }
    
    /**
     * Add real-time validation to form fields
     */
    private void addValidationListeners() {
        // Email validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail();
            updateSaveButtonState();
        });
        
        // First name validation
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateFirstName();
            updateSaveButtonState();
        });
        
        // Last name validation
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateLastName();
            updateSaveButtonState();
        });
        
        // Password validation
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword();
            updateSaveButtonState();
        });
        
        // Role validation
        roleComboBox.setOnAction(event -> {
            validateRole();
            updateSaveButtonState();
        });
        
        // Status validation
        statusComboBox.setOnAction(event -> {
            updateSaveButtonState();
        });
        
        // Update save button state
        updateSaveButtonState();
    }
    
    /**
     * Set user for editing (null for new user creation)
     */
    public void setUser(User user) {
        this.currentUser = user;
        
        if (user != null) {
            // Edit mode
            formTitle.setText("Edit User");
            saveButton.setText("Update User");
            generatePasswordButton.setVisible(false);
            
            // Populate fields
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            passwordField.setPromptText("Leave empty to keep current password");
            
            // Set role
            if (user.getRoles() != null && user.getRoles().length > 0) {
                String role = user.getRoles()[0];
                roleComboBox.getSelectionModel().select(role);
            }
            
            // Set status
            statusComboBox.getSelectionModel().select(user.isActive() ? "Active" : "Inactive");
            
            // Update button state after populating fields
            updateSaveButtonState();
            
        } else {
            // Create mode
            formTitle.setText("Add New User");
            saveButton.setText("Save User");
            generatePasswordButton.setVisible(true);
            passwordField.setPromptText("Enter password");
        }
    }
    
    /**
     * Set callback to refresh user list after save
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    /**
     * Generate random password
     */
    @FXML
    private void onGeneratePasswordClick() {
        String randomPassword = generateRandomPassword(12);
        passwordField.setText(randomPassword);
        showInfo("Random password generated: " + randomPassword);
    }
    
    /**
     * Handle save button click
     */
    @FXML
    private void onSaveClick() {
        if (!validateForm()) {
            return;
        }
        
        try {
            if (currentUser == null) {
                createUser();
            } else {
                updateUser();
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void onCancelClick() {
        closeForm();
    }
    
    /**
     * Create new user
     */
    private void createUser() throws SQLException {
        // Check if email already exists
        if (userDAO.emailExists(emailField.getText().trim())) {
            showError("Email already exists. Please use a different email address.");
            return;
        }
        
        User newUser = new User();
        populateUserFromForm(newUser);
        
        // Hash password
        String hashedPassword = hashPassword(passwordField.getText());
        newUser.setPasswordHash(hashedPassword);
        
        userDAO.save(newUser);
        showInfo("User created successfully!");
        
        // Trigger callback to refresh list
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        closeForm();
    }
    
    /**
     * Update existing user
     */
    private void updateUser() throws SQLException {
        // Check if email is changed and if new email exists
        if (!currentUser.getEmail().equals(emailField.getText().trim())) {
            if (userDAO.emailExists(emailField.getText().trim())) {
                showError("Email already exists. Please use a different email address.");
                return;
            }
        }
        
        populateUserFromForm(currentUser);
        
        // Update password only if provided
        if (!passwordField.getText().isEmpty()) {
            String hashedPassword = hashPassword(passwordField.getText());
            currentUser.setPasswordHash(hashedPassword);
        }
        
        userDAO.update(currentUser);
        showInfo("User updated successfully!");
        
        // Trigger callback to refresh list
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        closeForm();
    }
    
    /**
     * Populate User object from form fields
     */
    private void populateUserFromForm(User user) {
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        
        // Set role
        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
        user.setRoles(new String[]{selectedRole});
        
        // Set status
        boolean isActive = "Active".equals(statusComboBox.getSelectionModel().getSelectedItem());
        user.setActive(isActive);
    }
    
    /**
     * Validate entire form
     */
    private boolean validateForm() {
        return validateFirstName() && 
               validateLastName() && 
               validateEmail() && 
               validatePassword() &&
               validateRole();
    }
    
    /**
     * Validate first name
     */
    private boolean validateFirstName() {
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            setFieldError(firstNameField, "First name is required");
            return false;
        } else if (firstName.length() < 2) {
            setFieldError(firstNameField, "First name must be at least 2 characters");
            return false;
        } else {
            clearFieldError(firstNameField);
            return true;
        }
    }
    
    /**
     * Validate last name
     */
    private boolean validateLastName() {
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            setFieldError(lastNameField, "Last name is required");
            return false;
        } else if (lastName.length() < 2) {
            setFieldError(lastNameField, "Last name must be at least 2 characters");
            return false;
        } else {
            clearFieldError(lastNameField);
            return true;
        }
    }
    
    /**
     * Validate email
     */
    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            setFieldError(emailField, "Email is required");
            return false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            setFieldError(emailField, "Please enter a valid email address");
            return false;
        } else {
            clearFieldError(emailField);
            return true;
        }
    }
    
    /**
     * Validate password
     */
    private boolean validatePassword() {
        // For new users, password is required
        if (currentUser == null) {
            String password = passwordField.getText();
            if (password.isEmpty()) {
                setFieldError(passwordField, "Password is required for new users");
                return false;
            } else if (password.length() < 6) {
                setFieldError(passwordField, "Password must be at least 6 characters");
                return false;
            } else {
                clearFieldError(passwordField);
                return true;
            }
        } else {
            // For editing, password is optional
            clearFieldError(passwordField);
            return true;
        }
    }
    
    /**
     * Validate role selection
     */
    private boolean validateRole() {
        if (roleComboBox.getSelectionModel().getSelectedItem() == null) {
            setFieldError(roleComboBox, "Please select a role");
            return false;
        } else {
            clearFieldError(roleComboBox);
            return true;
        }
    }
    
    /**
     * Set error styling on field
     */
    private void setFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: red;");
        field.setTooltip(new Tooltip(message));
    }
    
    /**
     * Clear error styling from field
     */
    private void clearFieldError(Control field) {
        field.setStyle("");
        field.setTooltip(null);
    }
    
    /**
     * Update save button enabled state
     */
    private void updateSaveButtonState() {
        boolean isValid = isFormValid();
        saveButton.setDisable(!isValid);
    }
    
    /**
     * Check if form is valid without showing errors
     */
    private boolean isFormValid() {
        return isFirstNameValid() && 
               isLastNameValid() && 
               isEmailValid() && 
               isPasswordValid() &&
               isRoleValid();
    }
    
    /**
     * Check first name validity without showing error
     */
    private boolean isFirstNameValid() {
        String firstName = firstNameField.getText().trim();
        return !firstName.isEmpty() && firstName.length() >= 2;
    }
    
    /**
     * Check last name validity without showing error
     */
    private boolean isLastNameValid() {
        String lastName = lastNameField.getText().trim();
        return !lastName.isEmpty() && lastName.length() >= 2;
    }
    
    /**
     * Check email validity without showing error
     */
    private boolean isEmailValid() {
        String email = emailField.getText().trim();
        return !email.isEmpty() && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Check password validity without showing error
     */
    private boolean isPasswordValid() {
        if (currentUser == null) {
            // For new users, password is required
            String password = passwordField.getText();
            return !password.isEmpty() && password.length() >= 6;
        } else {
            // For editing, password is optional
            return true;
        }
    }
    
    /**
     * Check role validity without showing error
     */
    private boolean isRoleValid() {
        return roleComboBox.getSelectionModel().getSelectedItem() != null;
    }
    
    /**
     * Generate random password
     */
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
    
    /**
     * Hash password using BCrypt (compatible with Symfony/PHP)
     */
    private String hashPassword(String plainPassword) {
        // Use jBCrypt to hash the password in Symfony-compatible format
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
    }
    
    /**
     * Close form window
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Form Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info message
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}