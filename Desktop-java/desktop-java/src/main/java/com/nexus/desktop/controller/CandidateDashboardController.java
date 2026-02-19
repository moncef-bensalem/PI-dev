package com.nexus.desktop.controller;

import com.nexus.desktop.dao.UserDAO;
import com.nexus.desktop.model.User;
import com.nexus.desktop.service.AuthenticationService;
import com.nexus.desktop.util.TokenStorage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Modern candidate dashboard with profile editing
 */
public class CandidateDashboardController implements Initializable {

    @FXML private BorderPane dashboardContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label avatarLabel;
    @FXML private Label viewFirstName;
    @FXML private Label viewLastName;
    @FXML private Label viewEmail;
    @FXML private Button editProfileBtn;
    @FXML private Button profileNavBtn;
    @FXML private Button applicationsNavBtn;
    @FXML private GridPane profileViewMode;
    @FXML private VBox profileEditMode;
    @FXML private VBox profileView;
    @FXML private VBox applicationsView;
    @FXML private TextField editFirstName;
    @FXML private TextField editLastName;
    @FXML private TextField editEmail;

    private AuthenticationService authService;
    private User currentUser;
    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        refreshProfileDisplay();
    }

    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
        if (authService != null) {
            this.currentUser = authService.getCurrentUser();
            refreshProfileDisplay();
        }
    }

    private void refreshProfileDisplay() {
        if (currentUser == null) return;
        userNameLabel.setText(currentUser.getFullName());
        emailLabel.setText(currentUser.getEmail());
        welcomeLabel.setText("Bienvenue,");
        avatarLabel.setText(getInitials(currentUser));
        viewFirstName.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "-");
        viewLastName.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "-");
        viewEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "-");
    }

    private String getInitials(User user) {
        if (user.getFirstName() != null && !user.getFirstName().isEmpty() && 
            user.getLastName() != null && !user.getLastName().isEmpty()) {
            return String.valueOf(user.getFirstName().charAt(0)) + user.getLastName().charAt(0);
        }
        if (user.getEmail() != null && user.getEmail().length() >= 2) {
            return user.getEmail().substring(0, 2).toUpperCase();
        }
        return "??";
    }

    @FXML
    private void showProfileView() {
        profileView.setVisible(true);
        profileView.setManaged(true);
        applicationsView.setVisible(false);
        applicationsView.setManaged(false);
        setActiveNavButton(profileNavBtn);
    }

    @FXML
    private void showApplicationsView() {
        profileView.setVisible(false);
        profileView.setManaged(false);
        applicationsView.setVisible(true);
        applicationsView.setManaged(true);
        setActiveNavButton(applicationsNavBtn);
    }

    private void setActiveNavButton(Button activeBtn) {
        profileNavBtn.getStyleClass().remove("active");
        applicationsNavBtn.getStyleClass().remove("active");
        activeBtn.getStyleClass().add("active");
    }

    @FXML
    private void onEditProfileClick() {
        if (currentUser == null) return;
        editFirstName.setText(currentUser.getFirstName());
        editLastName.setText(currentUser.getLastName());
        editEmail.setText(currentUser.getEmail());
        profileViewMode.setVisible(false);
        profileViewMode.setManaged(false);
        profileEditMode.setVisible(true);
        profileEditMode.setManaged(true);
        editProfileBtn.setVisible(false);
        editProfileBtn.setManaged(false);
    }

    @FXML
    private void onCancelEditClick() {
        profileEditMode.setVisible(false);
        profileEditMode.setManaged(false);
        profileViewMode.setVisible(true);
        profileViewMode.setManaged(true);
        editProfileBtn.setVisible(true);
        editProfileBtn.setManaged(true);
    }

    @FXML
    private void onSaveProfileClick() {
        if (currentUser == null) return;

        String firstName = editFirstName.getText() != null ? editFirstName.getText().trim() : "";
        String lastName = editLastName.getText() != null ? editLastName.getText().trim() : "";
        String email = editEmail.getText() != null ? editEmail.getText().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showError("Le prénom et le nom sont requis.");
            return;
        }
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Veuillez entrer une adresse email valide.");
            return;
        }

        try {
            if (!email.equals(currentUser.getEmail()) && userDAO.emailExistsForOtherUser(email, currentUser.getId())) {
                showError("Cette adresse email est déjà utilisée.");
                return;
            }

            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);
            userDAO.updateProfile(currentUser);

            if (authService != null && authService.getCurrentUser() != null) {
                authService.getCurrentUser().setFirstName(firstName);
                authService.getCurrentUser().setLastName(lastName);
                authService.getCurrentUser().setEmail(email);
            }

            refreshProfileDisplay();
            onCancelEditClick();
            showInfo("Profil mis à jour avec succès.");
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutButtonClick() {
        if (currentUser == null) {
            showError("Aucun utilisateur connecté.");
            return;
        }

        if (authService == null) {
            TokenStorage.delete();
            showInfo("Retour à l'écran de connexion.");
            returnToLoginScreen();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Déconnexion de NEXUS");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    authService.logout();
                    TokenStorage.delete();
                    showInfo("Déconnexion réussie.");
                    returnToLoginScreen();
                } catch (Exception e) {
                    showError("Erreur: " + e.getMessage());
                }
            }
        });
    }

    private void returnToLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL resourceUrl = getClass().getClassLoader().getResource("login.fxml");
            if (resourceUrl == null) {
                resourceUrl = getClass().getClassLoader().getResource("primary.fxml");
            }
            if (resourceUrl == null) {
                throw new Exception("Fichier login introuvable");
            }
            loader.setLocation(resourceUrl);
            Parent loginRoot = loader.load();

            javafx.scene.Node sceneNode = dashboardContainer != null ? dashboardContainer : profileView;
            if (sceneNode == null || sceneNode.getScene() == null) {
                throw new Exception("Fenêtre non trouvée");
            }
            Stage stage = (Stage) sceneNode.getScene().getWindow();
            Scene scene = new Scene(loginRoot, 520, 680);
            stage.setScene(scene);
            stage.setTitle("NEXUS - Connexion");
            stage.show();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }
}
