package com.nexus.desktop.controller;

import com.nexus.desktop.model.Dashboard;
import com.nexus.desktop.model.User;
import com.nexus.desktop.service.DashboardService;
import com.nexus.desktop.service.AuthenticationService;
import com.nexus.desktop.util.TokenStorage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard Controller for User Management Module
 * Handles admin dashboard with KPIs, statistics, and user overview
 */
public class DashboardController implements Initializable {
    
    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label activeUsersLabel;
    
    @FXML
    private Label newUsersThisMonthLabel;
    
    @FXML
    private PieChart roleDistributionChart;
    
    @FXML
    private ScrollPane recentCreationsScrollPane;
    
    @FXML
    private VBox recentCreationsContainer;
    
    @FXML
    private ScrollPane securityAlertsScrollPane;
    
    @FXML
    private VBox securityAlertsContainer;
    
    @FXML
    private javafx.scene.layout.BorderPane dashboardContainer;

    @FXML
    private StackPane adminLogoContainer;

    @FXML
    private Label adminLogoText;

    @FXML
    private Label welcomeHeaderLabel;
    
    private DashboardService dashboardService;
    private AuthenticationService authService;
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardService = new DashboardService();
        loadAdminLogo();
        if (authService != null) {
            updateWelcomeHeader();
            loadDashboardData();
        }
    }

    private void loadAdminLogo() {
        if (adminLogoContainer == null) return;
        String[] paths = {"logo/logo.jpg", "logo/logo.png", "logo/nexus-logo.png", "logo/nexus.png"};
        for (String path : paths) {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                try {
                    ImageView imgView = new ImageView(new Image(url.toString()));
                    imgView.setFitHeight(48);
                    imgView.setPreserveRatio(true);
                    adminLogoContainer.getChildren().clear();
                    adminLogoContainer.getChildren().add(imgView);
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    private void updateWelcomeHeader() {
        if (welcomeHeaderLabel != null && currentUser != null) {
            welcomeHeaderLabel.setText("Bienvenue, " + currentUser.getFullName() + " 👋");
        }
    }
    
    /**
     * Load all dashboard data
     */
    private void loadDashboardData() {
        try {
            Dashboard dashboard = dashboardService.generateDashboardData();
            
            // Update KPI labels
            totalUsersLabel.setText(String.valueOf(dashboard.getTotalUsers()));
            activeUsersLabel.setText(String.valueOf(dashboard.getActiveUsers()));
            newUsersThisMonthLabel.setText(String.valueOf(dashboard.getNewUsersThisMonth()));
            
            // Update charts
            updateRoleDistributionChart(dashboard);
            
            // Update card displays
            displayRecentCreations(dashboard);
            displaySecurityAlerts(dashboard);
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error to user
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    /**
     * Update role distribution pie chart
     */
    private void updateRoleDistributionChart(Dashboard dashboard) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        dashboard.getUsersByRole().forEach((role, count) -> {
            pieChartData.add(new PieChart.Data(role, count));
        });
        
        roleDistributionChart.setData(pieChartData);
        roleDistributionChart.setTitle("User Roles Distribution");
    }
    
    /**
     * Display recent user creations as cards
     */
    private void displayRecentCreations(Dashboard dashboard) {
        recentCreationsContainer.getChildren().clear();
        
        for (Dashboard.UserCreation creation : dashboard.getRecentCreations()) {
            try {
                VBox userCard = createUserCard(creation);
                recentCreationsContainer.getChildren().add(userCard);
            } catch (Exception e) {
                System.err.println("Failed to create user card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Display security alerts as cards
     */
    private void displaySecurityAlerts(Dashboard dashboard) {
        securityAlertsContainer.getChildren().clear();
        
        for (Dashboard.SecurityAlert alert : dashboard.getSecurityAlerts()) {
            try {
                VBox alertCard = createAlertCard(alert);
                securityAlertsContainer.getChildren().add(alertCard);
            } catch (Exception e) {
                System.err.println("Failed to create alert card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Create user card for dashboard
     */
    private VBox createUserCard(Dashboard.UserCreation creation) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL resourceUrl = DashboardController.class.getClassLoader().getResource("fxml/dashboard_user_card.fxml");
        if (resourceUrl == null) {
            throw new IOException("Dashboard user card FXML file not found");
        }
        
        loader.setLocation(resourceUrl);
        VBox cardRoot = loader.load();
        
        // Get labels from the card
        Label userNameLabel = (Label) cardRoot.lookup("#userNameLabel");
        Label userEmailLabel = (Label) cardRoot.lookup("#userEmailLabel");
        Label userRoleLabel = (Label) cardRoot.lookup("#userRoleLabel");
        Label userDateLabel = (Label) cardRoot.lookup("#userDateLabel");
        
        if (userNameLabel != null) userNameLabel.setText(creation.getUserName());
        if (userEmailLabel != null) userEmailLabel.setText(creation.getUserEmail());
        if (userRoleLabel != null) userRoleLabel.setText(creation.getRole());
        if (userDateLabel != null) userDateLabel.setText(creation.getCreatedAt().toString());
        
        return cardRoot;
    }
    
    /**
     * Create alert card for dashboard
     */
    private VBox createAlertCard(Dashboard.SecurityAlert alert) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL resourceUrl = DashboardController.class.getClassLoader().getResource("fxml/dashboard_alert_card.fxml");
        if (resourceUrl == null) {
            throw new IOException("Dashboard alert card FXML file not found");
        }
        
        loader.setLocation(resourceUrl);
        VBox cardRoot = loader.load();
        
        // Get labels from the card
        Label alertTypeLabel = (Label) cardRoot.lookup("#alertTypeLabel");
        Label alertDescriptionLabel = (Label) cardRoot.lookup("#alertDescriptionLabel");
        Label alertSeverityLabel = (Label) cardRoot.lookup("#alertSeverityLabel");
        Label alertDateLabel = (Label) cardRoot.lookup("#alertDateLabel");
        
        if (alertTypeLabel != null) alertTypeLabel.setText(alert.getType());
        if (alertDescriptionLabel != null) alertDescriptionLabel.setText(alert.getDescription());
        if (alertSeverityLabel != null) alertSeverityLabel.setText(alert.getSeverity().toString());
        if (alertDateLabel != null) alertDateLabel.setText(alert.getTimestamp().toString());
        
        // Set CSS class based on severity for styling
        if (alertSeverityLabel != null) {
            String severity = alert.getSeverity();
            if ("HIGH".equals(severity)) {
                alertSeverityLabel.getStyleClass().add("badge-high");
            } else if ("MEDIUM".equals(severity)) {
                alertSeverityLabel.getStyleClass().add("badge-medium");
            } else if ("LOW".equals(severity)) {
                alertSeverityLabel.getStyleClass().add("badge-low");
            }
        }
        
        return cardRoot;
    }
    
    /**
     * Set current logged in user and load dashboard data
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            updateWelcomeHeader();
            loadDashboardData();
        }
    }
    
    /**
     * Set authentication service and current user
     */
    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
        if (authService != null) {
            this.currentUser = authService.getCurrentUser();
        }
        if (currentUser != null) {
            updateWelcomeHeader();
            loadDashboardData();
        }
    }
    
    /**
     * Refresh dashboard data
     */
    @FXML
    private void onRefreshButtonClick() {
        loadDashboardData();
        showInfo("Dashboard refreshed successfully");
    }
    
    /**
     * Export dashboard data
     */
    @FXML
    private void onExportButtonClick() {
        // Validate export functionality is available
        if (!isExportAvailable()) {
            showError("Export functionality is not available. Please contact system administrator.");
            return;
        }
        
        // Show export options dialog
        showExportOptionsDialog();
    }
    
    /**
     * Check if export functionality is available
     */
    private boolean isExportAvailable() {
        // Check system requirements for export
        try {
            // Check if required libraries are available
            Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Show export options dialog
     */
    private void showExportOptionsDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export Dashboard Data");
        alert.setHeaderText("Select Export Format");
        alert.setContentText("Choose the format for exporting dashboard data:");
        
        ButtonType pdfButton = new ButtonType("PDF");
        ButtonType csvButton = new ButtonType("CSV");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(pdfButton, csvButton, cancelButton);
        
        alert.showAndWait().ifPresent(type -> {
            if (type == pdfButton) {
                exportToPDF();
            } else if (type == csvButton) {
                exportToCSV();
            }
        });
    }
    
    /**
     * Export to PDF format
     */
    private void exportToPDF() {
        if (!validateExportData()) {
            return;
        }
        
        try {
            // Create file chooser to select save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Dashboard Data as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("nexus_admin_dashboard_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            
            File file = fileChooser.showSaveDialog(totalUsersLabel.getScene().getWindow());
            if (file == null) {
                // User cancelled the operation
                return;
            }
            
            // Generate dashboard data to include in PDF
            Dashboard dashboard = dashboardService.generateDashboardData();
            
            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage currentPage = new PDPage();
            document.addPage(currentPage);
            
            // Add content to the first page
            addPdfContent(document, currentPage, dashboard);
            
            // Save the PDF
            document.save(file);
            document.close();
            
            showInfo("Dashboard data exported successfully to " + file.getAbsolutePath());
            
        } catch (IOException e) {
            showError("Error exporting to PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            showError("Error retrieving dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to add content to PDF pages
     */
    private void addPdfContent(PDDocument document, PDPage firstPage, Dashboard dashboard) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, firstPage);
        
        // Title
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("NEXUS Admin Dashboard Report");
        contentStream.endText();
        
        // Subtitle with timestamp
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 730);
        contentStream.showText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        contentStream.endText();
        
        // Current user info
        if (currentUser != null) {
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText("Generated by: " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
            contentStream.endText();
        }
        
        // Stats section
        float yPos = 670;
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Dashboard Statistics");
        contentStream.endText();
        
        yPos -= 20;
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Total Users: " + dashboard.getTotalUsers());
        contentStream.endText();
        
        yPos -= 15;
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Active Users: " + dashboard.getActiveUsers());
        contentStream.endText();
        
        yPos -= 15;
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("New Users This Month: " + dashboard.getNewUsersThisMonth());
        contentStream.endText();
        
        // Role distribution
        yPos -= 25;
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Role Distribution");
        contentStream.endText();
        
        yPos -= 15;
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        for (String role : dashboard.getUsersByRole().keySet()) {
            Long count = dashboard.getUsersByRole().get(role);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("- " + role + ": " + count + " users");
            contentStream.endText();
            yPos -= 15;
            
            // Check if we need a new page
            if (yPos < 100) {
                contentStream.endText();
                contentStream.close();
                
                // Add new page
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                
                yPos = 750;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPos);
                contentStream.showText("Continued: Role Distribution");
                contentStream.endText();
                
                yPos -= 25;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
            }
        }
        
        // Recent user creations
        yPos -= 25;
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Recent User Creations");
        contentStream.endText();
        
        yPos -= 15;
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        for (Dashboard.UserCreation creation : dashboard.getRecentCreations()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("- " + creation.getUserName() + " (" + creation.getUserEmail() + ") - Role: " + creation.getRole() + " - Date: " + creation.getCreatedAt());
            contentStream.endText();
            yPos -= 15;
            
            // Check if we need a new page
            if (yPos < 100) {
                contentStream.endText();
                contentStream.close();
                
                // Add new page
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                
                yPos = 750;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPos);
                contentStream.showText("Continued: Recent User Creations");
                contentStream.endText();
                
                yPos -= 25;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
            }
        }
        
        // Security alerts
        yPos -= 25;
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Security Alerts");
        contentStream.endText();
        
        yPos -= 15;
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        for (Dashboard.SecurityAlert alert : dashboard.getSecurityAlerts()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("- [" + alert.getSeverity() + "] " + alert.getType() + ": " + alert.getDescription() + " - Date: " + alert.getTimestamp());
            contentStream.endText();
            yPos -= 15;
            
            // Check if we need a new page
            if (yPos < 100) {
                contentStream.endText();
                contentStream.close();
                
                // Add new page
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                
                yPos = 750;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPos);
                contentStream.showText("Continued: Security Alerts");
                contentStream.endText();
                
                yPos -= 25;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
            }
        }
        
        contentStream.endText();
        contentStream.close();
    }
    
    /**
     * Export to CSV format
     */
    private void exportToCSV() {
        if (!validateExportData()) {
            return;
        }
        
        // TODO: Implement CSV export functionality
        showInfo("CSV export functionality will be implemented in next sprint");
    }
    
    /**
     * Validate export data
     */
    private boolean validateExportData() {
        // Check if dashboard data is loaded
        if (totalUsersLabel.getText().equals("0")) {
            showError("No data available to export. Please refresh the dashboard first.");
            return false;
        }
        
        // Check if user has export permissions
        if (currentUser == null) {
            showError("User not authenticated. Please log in to export data.");
            return false;
        }
        
        // Check user role for export permissions
        boolean hasExportPermission = false;
        for (String role : currentUser.getRoles()) {
            if (role.contains("ADMIN") || role.contains("RH")) {
                hasExportPermission = true;
                break;
            }
        }
        
        if (!hasExportPermission) {
            showError("You don't have permission to export dashboard data.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dashboard Error");
        alert.setHeaderText("Dashboard Loading Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info message
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Handle logout
     */
    @FXML
    private void onUserManagementButtonClick(javafx.event.ActionEvent event) {
        // Open user management window
        try {
            FXMLLoader loader = new FXMLLoader();
            URL resourceUrl = getClass().getClassLoader().getResource("fxml/user_management.fxml");
            if (resourceUrl == null) {
                throw new IOException("User management FXML file not found");
            }
            
            loader.setLocation(resourceUrl);
            Parent root = loader.load();
            
            // Get controller and set current user
            UserManagementController userMgmtController = loader.getController();
            userMgmtController.setCurrentUser(currentUser);
            
            // Create new window
            Stage stage = new Stage();
            stage.setTitle("User Management - NEXUS");
            stage.setScene(new Scene(root, 1000, 600));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            // Use the current scene instead of dashboardContainer
            stage.initOwner(((Button) event.getSource()).getScene().getWindow());
            stage.show();
            
        } catch (IOException e) {
            showError("Failed to open user management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onLogoutButtonClick() {
        System.out.println("Logout button clicked");
        System.out.println("AuthService: " + (authService != null ? "Available" : "Null"));
        System.out.println("CurrentUser: " + (currentUser != null ? currentUser.getEmail() : "Null"));
        
        if (currentUser == null) {
            showError("No user is currently logged in");
            return;
        }
        
        if (authService == null) {
            TokenStorage.delete();
            showInfo("Returning to login screen.");
            returnToLoginScreen();
            return;
        }
        
        // Confirm logout
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout Confirmation");
        confirmation.setHeaderText("Logout from NEXUS");
        confirmation.setContentText("Are you sure you want to logout?\n\n" + 
            "This will end your current session and return to the login screen.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performLogout();
            }
        });
    }
    
    /**
     * Perform actual logout
     */
    private void performLogout() {
        System.out.println("Performing logout...");
        System.out.println("Current session: " + (authService.getCurrentSession() != null ? "Exists" : "Null"));
        
        try {
            // Logout from authentication service
            boolean loggedOut = authService.logout();
            System.out.println("Logout result: " + loggedOut);
            
            if (loggedOut) {
                TokenStorage.delete();
                showInfo("You have been successfully logged out.");
                returnToLoginScreen();
            } else {
                showError("Logout failed. Please try again.");
            }
        } catch (Exception e) {
            showError("Logout error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Return to login screen
     */
    private void returnToLoginScreen() {
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
            
            // Get current stage - use dashboardContainer or totalUsersLabel as fallback
            javafx.scene.Node sceneNode = dashboardContainer != null ? dashboardContainer : totalUsersLabel;
            if (sceneNode == null || sceneNode.getScene() == null) {
                throw new Exception("Cannot determine current window");
            }
            Stage currentStage = (Stage) sceneNode.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot, 520, 680);
            currentStage.setScene(loginScene);
            currentStage.setTitle("NEXUS - Login");
            currentStage.show();
            
        } catch (Exception e) {
            showError("Failed to return to login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}