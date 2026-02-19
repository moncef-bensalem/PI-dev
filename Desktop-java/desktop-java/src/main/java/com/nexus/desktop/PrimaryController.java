package com.nexus.desktop;

import com.nexus.desktop.model.User;
import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.service.AuthenticationService;
import com.nexus.desktop.util.DatabaseManager;
import com.nexus.desktop.util.DashboardRouter;
import com.nexus.desktop.util.TokenStorage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class PrimaryController implements Initializable {

    @FXML
    private Label welcomeText;

    @FXML
    private Button loginButton;

    @FXML
    private Button exitButton;

    private UserDAO userDAO;
    private AuthenticationService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!DatabaseManager.isDatabaseAvailable()) {
            welcomeText.setText("Database connection failed!");
            loginButton.setDisable(true);
            return;
        }
        userDAO = new UserDAO();
        authService = new AuthenticationService();
        welcomeText.setText("Welcome to NEXUS - Recruitment Platform");

        // Remember Me: restore session from saved token (defer until scene is attached)
        Platform.runLater(this::tryRestoreSession);
    }

    private void tryRestoreSession() {
        String savedToken = TokenStorage.load();
        if (savedToken == null || savedToken.isEmpty()) return;
        try {
            authService.restoreFromToken(savedToken);
            User user = authService.getCurrentUser();
            if (user != null && welcomeText.getScene() != null) {
                redirectToDashboard(user, authService);
            }
        } catch (Exception e) {
            TokenStorage.delete();
        }
    }
    
    /**
     * Redirect to role-appropriate dashboard after successful login
     */
    private void redirectToDashboard(User user, AuthenticationService authService) throws IOException {
        String dashboardPath = DashboardRouter.getDashboardForUser(user);
        URL resourceUrl = getClass().getClassLoader().getResource(dashboardPath);
        if (resourceUrl == null) {
            throw new IOException("Dashboard FXML file not found: " + dashboardPath);
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

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(dashboardRoot);
        Stage stage = (Stage) currentScene.getWindow();
        stage.setTitle("NEXUS - " + (DashboardRouter.isCandidate(user) ? "Candidate" : "Admin") + " Dashboard - " + user.getFullName());
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        // Create a simple login dialog
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Enter your credentials");

        // Set the button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create the login form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable OK button depending on whether a minimum number of fields have valid values
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax)
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || passwordField.getText().trim().isEmpty());
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || emailField.getText().trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new String[]{emailField.getText(), passwordField.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                String email = result[0];
                String password = result[1];

                // Authenticate with AuthenticationService (creates JWT for Remember Me)
                javafx.concurrent.Task<javafx.util.Pair<User, AuthenticationService>> loginTask = new javafx.concurrent.Task<javafx.util.Pair<User, AuthenticationService>>() {
                    @Override
                    protected javafx.util.Pair<User, AuthenticationService> call() throws Exception {
                        try {
                            authService.authenticate(email, password);
                            User user = authService.getCurrentUser();
                            if (user != null) {
                                return new javafx.util.Pair<>(user, authService);
                            }
                            throw new RuntimeException("Invalid credentials");
                        } catch (Exception e) {
                            throw new RuntimeException("Database error: " + e.getMessage(), e);
                        }
                    }
                };

                loginTask.setOnSucceeded(ev -> {
                    javafx.util.Pair<User, AuthenticationService> loginResult = loginTask.getValue();
                    User user = loginResult.getKey();
                    AuthenticationService service = loginResult.getValue();
                    try {
                        // Save token for Remember Me (persistent session)
                        if (service.getCurrentToken() != null && service.getCurrentToken().getToken() != null) {
                            TokenStorage.save(service.getCurrentToken().getToken());
                        }
                        redirectToDashboard(user, service);
                    } catch (IOException ex) {
                        showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load dashboard: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });

                loginTask.setOnFailed(e -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setHeaderText("Login Error");
                    alert.setContentText("Could not log in. Please check your credentials.");
                    alert.showAndWait();
                });

                new Thread(loginTask).start();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Unexpected Error");
                alert.setContentText("An error occurred: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    @FXML
    protected void onExitButtonClick(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
}