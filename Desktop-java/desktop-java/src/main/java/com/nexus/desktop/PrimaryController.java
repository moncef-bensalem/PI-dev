package com.nexus.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexus.desktop.model.User;
import com.nexus.desktop.service.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class PrimaryController {

    @FXML
    private Label welcomeText;

    @FXML
    private Button loginButton;

    @FXML
    private Button exitButton;

    private ApiService apiService;

    public void initialize() {
        apiService = new ApiService();
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

                // Attempt to log in using the actual API service
                javafx.concurrent.Task<User> loginTask = new javafx.concurrent.Task<User>() {
                    @Override
                    protected User call() throws Exception {
                        try {
                            Map<String, Object> response = apiService.login(email, password);
                            
                            if (Boolean.TRUE.equals(response.get("success"))) {
                                // Extract user information from the API response
                                JsonNode userNode = (JsonNode) response.get("user");
                                if (userNode != null) {
                                    User user = new User();
                                    user.setId(userNode.get("id").asLong());
                                    user.setEmail(userNode.get("email").asText());
                                    user.setFirstName(userNode.get("fullName").asText());
                                    user.setLastName("");
                                    user.setActive(true);
                                    return user;
                                } else {
                                    throw new RuntimeException("No user data in response");
                                }
                            } else {
                                // Login failed
                                String message = (String) response.get("message");
                                throw new RuntimeException(message != null ? message : "Login failed");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Network error: " + e.getMessage(), e);
                        }
                    }
                };

                loginTask.setOnSucceeded(e -> {
                    User user = loginTask.getValue();
                    welcomeText.setText("Welcome, " + user.getFullName() + "!");
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