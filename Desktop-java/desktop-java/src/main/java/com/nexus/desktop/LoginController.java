package com.nexus.desktop;

import com.nexus.desktop.model.User;
import com.nexus.desktop.service.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

    private ApiService apiService;
    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = new ApiService();
        
        // Disable login button initially
        loginButton.setDisable(true);
        
        // Enable/disable login button based on input
        emailField.textProperty().addListener((obs, oldText, newText) -> 
            validateInputs());
        passwordField.textProperty().addListener((obs, oldText, newText) -> 
            validateInputs());
    }

    private void validateInputs() {
        boolean emailValid = emailField.getText() != null && !emailField.getText().trim().isEmpty();
        boolean passwordValid = passwordField.getText() != null && !passwordField.getText().trim().isEmpty();
        
        loginButton.setDisable(!emailValid || !passwordValid);
    }

    @FXML
    protected void onLoginClick(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all fields.");
            return;
        }

        // Perform login in a background thread
        Thread loginThread = new Thread(() -> {
            try {
                // For now, simulate login success with mock data
                // In the future, use apiService.login(email, password)
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail(email);
                mockUser.setFirstName("Demo");
                mockUser.setLastName("User");
                mockUser.setActive(true);
                
                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                    closeWindow();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> 
                    showAlert(Alert.AlertType.ERROR, "Login Error", 
                             "Login failed: " + e.getMessage()));
            }
        });

        loginButton.setDisable(true);
        loginThread.start();
    }

    @FXML
    protected void onCancelClick(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}