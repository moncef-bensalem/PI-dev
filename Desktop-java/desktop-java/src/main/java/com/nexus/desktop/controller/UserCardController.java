package com.nexus.desktop.controller;

import com.nexus.desktop.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class UserCardController {
    
    private User user;
    private UserManagementController parentController;
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setParentController(UserManagementController parentController) {
        this.parentController = parentController;
    }
    
    public VBox createUserCard(User user, UserManagementController parentController) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL resourceUrl = UserCardController.class.getClassLoader().getResource("fxml/user_card.fxml");
        if (resourceUrl == null) {
            throw new IOException("User card FXML file not found");
        }
        
        loader.setLocation(resourceUrl);
        VBox cardRoot = loader.load();
        
        UserCardController cardController = loader.getController();
        cardController.setUser(user);
        cardController.setParentController(parentController);
        
        // Update card content
        cardController.updateCardContent(cardRoot, user);
        
        return cardRoot;
    }
    
    private void updateCardContent(VBox cardRoot, User user) {
        // Update labels in the card
        javafx.scene.control.Label userNameLabel = (javafx.scene.control.Label) cardRoot.lookup("#userNameLabel");
        javafx.scene.control.Label userEmailLabel = (javafx.scene.control.Label) cardRoot.lookup("#userEmailLabel");
        javafx.scene.control.Label userRoleLabel = (javafx.scene.control.Label) cardRoot.lookup("#userRoleLabel");
        javafx.scene.control.Label userStatusLabel = (javafx.scene.control.Label) cardRoot.lookup("#userStatusLabel");
        
        if (userNameLabel != null) userNameLabel.setText(user.getFullName());
        if (userEmailLabel != null) userEmailLabel.setText(user.getEmail());
        if (userRoleLabel != null) userRoleLabel.setText(user.getRoles()[0]);
        if (userStatusLabel != null) {
            userStatusLabel.setText(user.isActive() ? "ACTIVE" : "INACTIVE");
            userStatusLabel.setStyle(user.isActive() ? 
                "-fx-background-color: #bbf7d0; -fx-text-fill: #1a1a2e; -fx-padding: 2 8 2 8; -fx-background-radius: 10;" :
                "-fx-background-color: #fecaca; -fx-text-fill: #1a1a2e; -fx-padding: 2 8 2 8; -fx-background-radius: 10;");
        }
        
        // Setup button actions
        javafx.scene.control.Button editCardButton = (javafx.scene.control.Button) cardRoot.lookup("#editCardButton");
        javafx.scene.control.Button deleteCardButton = (javafx.scene.control.Button) cardRoot.lookup("#deleteCardButton");
        javafx.scene.control.Button activateCardButton = (javafx.scene.control.Button) cardRoot.lookup("#activateCardButton");
        javafx.scene.control.Button deleteUserButton = (javafx.scene.control.Button) cardRoot.lookup("#deleteUserButton");
        
        if (editCardButton != null) {
            editCardButton.setOnAction(e -> {
                if (validateUserAction("edit", user)) {
                    if (parentController != null) {
                        // Call parent controller method to edit user
                        try {
                            java.lang.reflect.Method method = parentController.getClass().getDeclaredMethod("showEditUserDialog");
                            method.setAccessible(true);
                            method.invoke(parentController);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
        
        if (deleteCardButton != null) {
            deleteCardButton.setOnAction(e -> {
                if (validateUserAction("delete", user)) {
                    if (parentController != null) {
                        // Call parent controller method to delete user
                        try {
                            java.lang.reflect.Method method = parentController.getClass().getDeclaredMethod("deleteUser");
                            method.setAccessible(true);
                            method.invoke(parentController);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
        
        if (activateCardButton != null) {
            activateCardButton.setOnAction(e -> {
                if (validateUserAction("activate", user)) {
                    if (parentController != null) {
                        // Call parent controller method to activate user
                        try {
                            java.lang.reflect.Method method = parentController.getClass().getDeclaredMethod("activateUser");
                            method.setAccessible(true);
                            method.invoke(parentController);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
        
        if (deleteUserButton != null) {
            deleteUserButton.setOnAction(e -> {
                if (validateUserAction("delete_permanent", user)) {
                    if (parentController != null) {
                        // Call parent controller method to delete user permanently
                        try {
                            java.lang.reflect.Method method = parentController.getClass().getDeclaredMethod("deleteUserPermanently");
                            method.setAccessible(true);
                            method.invoke(parentController);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Validate user action permissions and conditions
     */
    private boolean validateUserAction(String action, User user) {
        // Check if user object is valid
        if (user == null) {
            showError("Invalid user data. Please refresh the user list.");
            return false;
        }
        
        // Validate action-specific conditions
        switch (action) {
            case "edit":
                return validateEditAction(user);
            case "delete":
                return validateDeleteAction(user);
            case "activate":
                return validateActivateAction(user);
            case "delete_permanent":
                return validateDeletePermanentAction(user);
            default:
                showError("Invalid action requested.");
                return false;
        }
    }
    
    /**
     * Validate edit action
     */
    private boolean validateEditAction(User user) {
        // Check if user has required data
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            showError("Cannot edit user: Missing email address.");
            return false;
        }
        
        if (user.getRoles() == null || user.getRoles().length == 0) {
            showError("Cannot edit user: Missing role information.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate delete action
     */
    private boolean validateDeleteAction(User user) {
        // Prevent deleting system users
        if (user.getEmail() != null && user.getEmail().toLowerCase().contains("admin")) {
            showError("Cannot deactivate system administrator account.");
            return false;
        }
        
        // Check if user is already inactive
        if (!user.isActive()) {
            showError("User is already deactivated.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate activate action
     */
    private boolean validateActivateAction(User user) {
        // Check if user is already active
        if (user.isActive()) {
            showError("User is already active.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate permanent delete action
     */
    private boolean validateDeletePermanentAction(User user) {
        // Confirm permanent deletion
        javafx.scene.control.Alert confirmation = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Permanent Delete Confirmation");
        confirmation.setHeaderText("Delete User Permanently");
        confirmation.setContentText("Are you sure you want to PERMANENTLY DELETE user " + user.getFullName() + "?\n\n" +
            "THIS ACTION CANNOT BE UNDONE!");
        
        javafx.scene.control.ButtonType result = confirmation.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL);
        return result == javafx.scene.control.ButtonType.OK;
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("User Action Error");
        alert.setHeaderText("Action Validation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}