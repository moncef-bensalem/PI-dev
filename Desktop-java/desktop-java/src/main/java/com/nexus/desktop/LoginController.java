package com.nexus.desktop;

import com.nexus.desktop.model.User;
import com.nexus.desktop.model.AuthToken;
import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.service.AuthenticationService;
import com.nexus.desktop.util.DatabaseManager;
import com.nexus.desktop.util.DashboardRouter;
import com.nexus.desktop.util.TokenStorage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button signupButton;

    @FXML
    private StackPane logoContainer;

    @FXML
    private Label logoText;

    private UserDAO userDAO;
    private AuthenticationService authService;
    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadLogo();
        if (!DatabaseManager.isDatabaseAvailable()) {
            System.err.println("Database not available - running in offline mode");
            // Setup basic UI without database
            setupOfflineMode();
            return;
        }
        userDAO = new UserDAO();
        authService = new AuthenticationService();
        loginButton.setDisable(true);
        emailField.textProperty().addListener((obs, oldText, newText) -> validateInputs());
        passwordField.textProperty().addListener((obs, oldText, newText) -> validateInputs());

        // Try automatic login from persistent token (Remember Me)
        tryAutoLoginFromToken();
    }
    
    private void setupOfflineMode() {
        loginButton.setText("Database Unavailable");
        loginButton.setDisable(true);
        loginButton.setStyle("-fx-background-color: #dc3545;");
        // Setup basic event handlers for UI elements
        loginButton.setOnAction(e -> showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database. Please check your database configuration and restart the application."));
        cancelButton.setOnAction(e -> onCancelClick(e));
    }

    private void loadLogo() {
        if (logoContainer == null) return;
        String[] logoPaths = {"logo/logo.jpg", "logo/logo.png", "logo/nexus-logo.png", "logo/nexus.png", "logo/nexus.jpg"};
        for (String path : logoPaths) {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                try {
                    ImageView imgView = new ImageView(new Image(url.toString()));
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
        boolean emailValid = validateEmailFormat(emailField.getText());
        boolean passwordValid = validatePasswordFormat(passwordField.getText());
        
        loginButton.setDisable(!emailValid || !passwordValid);
        
        // Update field styling
        updateFieldStyling(emailField, emailValid);
        updateFieldStyling(passwordField, passwordValid);
    }
    
    /**
     * Validate email format
     */
  
    
    /**
     * Validate password format
     */
    private boolean validatePasswordFormat(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Check minimum length
        return password.trim().length() >= 4;
    }
    
    /**
     * Update field styling based on validation
     */
    private void updateFieldStyling(javafx.scene.control.TextInputControl field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px;");
        } else {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
        }
    }

    /**
     * Attempt automatic login using a previously saved JWT token.
     * If the token is valid, the user is redirected directly to the appropriate dashboard.
     * If invalid or expired, the token file is deleted and the normal login screen is shown.
     *
     * Important: this is called from initialize(), before the controls are attached to a Scene.
     * We therefore defer the redirect to the JavaFX application thread with Platform.runLater
     * so that cancelButton.getScene() is not null when we try to access the Stage.
     */
    private void tryAutoLoginFromToken() {
        String savedToken = TokenStorage.load();
        if (savedToken == null || savedToken.trim().isEmpty()) {
            System.out.println("No saved token found for auto-login.");
            return;
        }

        System.out.println("Attempting auto-login from saved token...");
        try {
            authService.restoreFromToken(savedToken);
            User user = authService.getCurrentUser();
            if (user != null) {
                System.out.println("Auto-login successful for: " + user.getEmail());

                // Defer redirect until the scene is fully attached
                Platform.runLater(() -> {
                    try {
                        if (cancelButton != null && cancelButton.getScene() != null) {
                            redirectToDashboard(user);
                        } else {
                            System.out.println("Auto-login: scene not yet ready, skipping redirect.");
                        }
                    } catch (Exception ex) {
                        System.out.println("Exception during auto-login redirect: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            } else {
                System.out.println("Auto-login failed: user is null after restoreFromToken.");
            }
        } catch (Exception e) {
            System.out.println("Auto-login from token failed: " + e.getMessage());
            e.printStackTrace();
            // Token is invalid/expired or DB error: clear it so we don't loop on next start
            TokenStorage.delete();
        }
    }

    @FXML
    protected void onLoginClick(ActionEvent event) {
        System.out.println("onLoginClick method called");
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("Validating login form - Email: " + email + ", Password: " + (password != null ? "Provided" : "Null"));
        // Comprehensive input validation
        if (!validateLoginForm(email, password)) {
            System.out.println("Login form validation failed");
            return;
        }
        System.out.println("Login form validation passed");

        try {
            System.out.println("Attempting authentication for: " + email);
            // Authenticate user with JWT token generation
            System.out.println("About to authenticate");
            AuthToken authToken = authService.authenticate(email, password);
            System.out.println("Auth token result: " + (authToken != null ? "Success" : "Null"));
            User user = authService.getCurrentUser();
            System.out.println("Authentication result - User: " + (user != null ? user.getEmail() : "null"));
            System.out.println("Auth token: " + (authToken != null ? "Generated" : "Null"));
            System.out.println("Current session after auth: " + (authService.getCurrentSession() != null ? "Available" : "Null"));
            
            if (user != null) {
                // Save token for Remember Me (persistent session)
                if (authToken != null && authToken.getToken() != null) {
                    TokenStorage.save(authToken.getToken());
                }
                try {
                    redirectToDashboard(user);
                } catch (Exception e) {
                    System.out.println("Exception in redirectToDashboard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Login failed
                showAlert(Alert.AlertType.ERROR, "Login Failed", 
                         "Invalid email or password. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Exception in login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Database connection error: " + e.getMessage());
        }

        loginButton.setDisable(true);
    }

    @FXML
    protected void onCancelClick(ActionEvent event) {
        closeWindow();
    }

    @FXML
    protected void onSignupClick(ActionEvent event) {
        switchToRegistrationScreen();
    }

    private void switchToRegistrationScreen() {
        try {
            // Load registration FXML
            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getClassLoader().getResource("fxml/registration.fxml");
            if (resourceUrl == null) {
                throw new Exception("Registration FXML file not found");
            }

            loader.setLocation(resourceUrl);
            Parent registrationRoot = loader.load();

            // Get current stage
            Stage currentStage = (Stage) signupButton.getScene().getWindow();
            Scene registrationScene = new Scene(registrationRoot, 520, 720);
            currentStage.setScene(registrationScene);
            currentStage.setTitle("NEXUS - Registration");
            currentStage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Failed to navigate to registration screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Redirect to role-appropriate dashboard after successful login
     */
    private void redirectToDashboard(User user) throws Exception {
        String dashboardPath = DashboardRouter.getDashboardForUser(user);
        java.net.URL resourceUrl = getClass().getClassLoader().getResource(dashboardPath);
        if (resourceUrl == null) {
            throw new Exception("Dashboard FXML file not found: " + dashboardPath);
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resourceUrl);
        Parent dashboardRoot = loader.load();

        // Set user and auth based on dashboard type
        if (DashboardRouter.isCandidate(user)) {
            com.nexus.desktop.controller.CandidateDashboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(authService);
        } else {
            com.nexus.desktop.controller.DashboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(authService);
        }

        Scene dashboardScene = new Scene(dashboardRoot, 1200, 800);
        Stage currentStage = (Stage) cancelButton.getScene().getWindow();
        currentStage.setScene(dashboardScene);
        currentStage.setTitle("NEXUS - " + (DashboardRouter.isCandidate(user) ? "Candidate" : "Admin") + " Dashboard - " + user.getFullName());
        currentStage.show();
    }
    
    /**
     * Validate login form inputs
     */
    private boolean validateLoginForm(String email, String password) {
        // Check if fields are empty
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
        if (password.length() < 4) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Password must be at least 4 characters long.");
            passwordField.requestFocus();
            passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return false;
        }
        
        // Check for suspicious characters
        if (password.contains(" ") || password.contains("	")) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Password cannot contain spaces or tabs.");
            passwordField.requestFocus();
            return false;
        }
        
        // Reset field styling on successful validation
        emailField.setStyle("");
        passwordField.setStyle("");
        
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info message
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}