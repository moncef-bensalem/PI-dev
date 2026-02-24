package com.nexus.desktop.controller;

import com.nexus.desktop.LoginController;
import com.nexus.desktop.model.User;
import com.nexus.desktop.service.HttpClientService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RegistrationController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private StackPane logoContainer;

    @FXML
    private Label logoText;

    private HttpClientService httpClientService;

    public RegistrationController() {
        this.httpClientService = new HttpClientService();
    }

    @FXML
    private void initialize() {
        loadLogo();
        registerButton.setDisable(true);
        
        // Add listeners to validate inputs
        firstNameField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        lastNameField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        emailField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        passwordField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
    }

    private void loadLogo() {
        if (logoContainer == null) return;
        String[] logoPaths = {"logo/logo.jpg", "logo/logo.png", "logo/nexus-logo.png", "logo/nexus.png", "logo/nexus.jpg"};
        for (String path : logoPaths) {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                try {
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(url.toString()));
                    imgView.setFitHeight(90);
                    imgView.setPreserveRatio(true);
                    logoContainer.getChildren().clear();
                    logoContainer.getChildren().add(imgView);
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    private void validateInputs() {
        boolean firstNameValid = validateName(firstNameField.getText());
        boolean lastNameValid = validateName(lastNameField.getText());
        boolean emailValid = validateEmailFormat(emailField.getText());
        boolean passwordValid = validatePasswordFormat(passwordField.getText());
        boolean passwordsMatch = passwordField.getText().equals(confirmPasswordField.getText());

        registerButton.setDisable(!firstNameValid || !lastNameValid || !emailValid || !passwordValid || !passwordsMatch);

        // Update field styling
        updateFieldStyling(firstNameField, firstNameValid);
        updateFieldStyling(lastNameField, lastNameValid);
        updateFieldStyling(emailField, emailValid);
        updateFieldStyling(passwordField, passwordValid);
        updateFieldStyling(confirmPasswordField, passwordsMatch);
    }

    private boolean validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.trim().length() >= 2;
    }

    private boolean validateEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean validatePasswordFormat(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.trim().length() >= 6;
    }

    private void updateFieldStyling(javafx.scene.control.TextInputControl field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px;");
        } else {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
        }
    }

    @FXML
    private void onRegisterClick() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate form inputs
        if (!validateForm(firstName, lastName, email, password)) {
            return;
        }

        // Disable register button during registration
        registerButton.setDisable(true);

        try {
            // Prepare registration data
            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("firstName", firstName);
            registrationData.put("lastName", lastName);
            registrationData.put("email", email);
            registrationData.put("password", password);
            registrationData.put("role", "ROLE_CANDIDAT"); // Register as candidate by default

            // Send registration request to backend
            String response = httpClientService.post("/api/auth/register", registrationData);

            // Parse response
            if (response != null && response.contains("\"success\":true")) {
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                         "Your account has been created successfully. You can now log in.");
                switchToLoginScreen();
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", 
                         "Unable to create your account. Please try again.");
                registerButton.setDisable(false);
            }
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Error", 
                     "Unable to connect to the server. Please make sure the backend server is running at http://localhost:8000\n\nTo start the backend server:\n1. Navigate to the Backend-symfony folder\n2. Run: symfony server:start\n   Or alternatively: php -S localhost:8000 -t public/");
            registerButton.setDisable(false);
        } catch (java.net.http.HttpTimeoutException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Timeout", 
                     "Connection to the server timed out. Please check your network connection and try again.");
            registerButton.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            // Check if it's a connection-related exception
            if (e instanceof java.net.UnknownHostException || 
                e instanceof java.net.NoRouteToHostException ||
                e instanceof java.nio.channels.ClosedChannelException) {
                showAlert(Alert.AlertType.ERROR, "Connection Error", 
                         "Unable to connect to the server. Please make sure the backend server is running at http://localhost:8000\n\nTo start the backend server:\n1. Navigate to the Backend-symfony folder\n2. Run: symfony server:start\n   Or alternatively: php -S localhost:8000 -t public/");
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Error", 
                         "An error occurred during registration: " + e.getMessage());
            }
            registerButton.setDisable(false);
        }
    }

    private boolean validateForm(String firstName, String lastName, String email, String password) {
        // Check if fields are empty
        if (firstName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "First name field cannot be empty.");
            firstNameField.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Last name field cannot be empty.");
            lastNameField.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Email field cannot be empty.");
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Password field cannot be empty.");
            passwordField.requestFocus();
            return false;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a valid email address.");
            emailField.requestFocus();
            emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return false;
        }

        // Validate password length
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Password must be at least 6 characters long.");
            passwordField.requestFocus();
            passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Passwords do not match.");
            confirmPasswordField.requestFocus();
            confirmPasswordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return false;
        }

        return true;
    }

    @FXML
    private void onBackToLoginClick() {
        switchToLoginScreen();
    }

    private void switchToLoginScreen() {
        try {
            // Load login FXML
            FXMLLoader loader = new FXMLLoader();
            URL resourceUrl = getClass().getClassLoader().getResource("login.fxml");
            if (resourceUrl == null) {
                resourceUrl = getClass().getClassLoader().getResource("primary.fxml");
            }
            if (resourceUrl == null) {
                throw new Exception("Login FXML file not found");
            }

            loader.setLocation(resourceUrl);
            Parent loginRoot = loader.load();

            // Get current stage
            Stage currentStage = (Stage) backToLoginButton.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot, 520, 680);
            currentStage.setScene(loginScene);
            currentStage.setTitle("NEXUS - Login");
            currentStage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Failed to return to login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}