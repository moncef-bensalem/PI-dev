package conttrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import models.Planification;
import services.ServicePlanification;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;

public class AjouterPlanification {

    @FXML private TextField TFTypeEvent;
    @FXML private TextField TFDate;        // yyyy-mm-dd
    @FXML private TextField TFHeureDebut;  // HH:mm
    @FXML private TextField TFHeureFin;    // HH:mm
    @FXML private TextField TFMode;
    @FXML private TextField TFStatut;
    @FXML private TextField TFDescription;
    @FXML private TextField TFLienMeeting;

    private final ServicePlanification servicePlanification = new ServicePlanification();

    @FXML
    void ajouterPlanification(ActionEvent event) {

        String typeEvent     = TFTypeEvent.getText().trim();
        String dateStr       = TFDate.getText().trim();
        String heureDebutStr = TFHeureDebut.getText().trim();
        String heureFinStr   = TFHeureFin.getText().trim();

        String mode        = TFMode.getText().trim();
        String statut      = TFStatut.getText().trim();
        String description = TFDescription.getText().trim();
        String lienMeeting = TFLienMeeting.getText().trim();

        // Required fields (STRINGS)
        if (typeEvent.isEmpty() || dateStr.isEmpty() || heureDebutStr.isEmpty() || heureFinStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing fields",
                    "TypeEvent, Date, Heure Debut, Heure Fin are required.");
            return;
        }

        // Date (yyyy-mm-dd)
        Date dateSql;
        try {
            dateSql = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date Format",
                    "Use yyyy-mm-dd example: 2026-02-14");
            return;
        }

        // Time (HH:mm)
        Time heureDebut;
        Time heureFin;
        try {
            heureDebut = Time.valueOf(heureDebutStr + ":00");
            heureFin   = Time.valueOf(heureFinStr + ":00");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time Format",
                    "Use HH:mm example: 08:30");
            return;
        }

        Planification p = new Planification(
                typeEvent,
                dateSql,
                heureDebut,
                heureFin,
                mode,
                statut,
                description,
                lienMeeting
        );

        try {
            servicePlanification.add(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Planification added ✅");
            clearFields();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Insert Failed", ex.getMessage());
        }
    }

    @FXML
    void gotToAfficher(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    AjouterPlanification.class.getResource("/AfficherPlanification.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) TFTypeEvent.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();   // IMPORTANT
            showAlert(Alert.AlertType.ERROR, "Navigation error", e.getMessage());
        }
    }


    private void clearFields() {
        TFTypeEvent.clear();
        TFDate.clear();
        TFHeureDebut.clear();
        TFHeureFin.clear();
        TFMode.clear();
        TFStatut.clear();
        TFDescription.clear();
        TFLienMeeting.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
