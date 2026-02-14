package conttrollers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Planification;
import services.ServicePlanification;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UpdatePlanificationController {

    @FXML private TextField tfId;
    @FXML private TextField tfType;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfDebut;
    @FXML private TextField tfFin;
    @FXML private TextField tfMode;
    @FXML private TextField tfStatut;
    @FXML private TextArea taDesc;
    @FXML private TextField tfLien;
    @FXML private Label lbError;

    private final ServicePlanification service = new ServicePlanification();
    private Planification current;
    private Runnable onSavedCallback; // to refresh cards

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public void setData(Planification p, Runnable onSavedCallback) {
        this.current = p;
        this.onSavedCallback = onSavedCallback;

        tfId.setText(String.valueOf(p.getIdEvent()));
        tfType.setText(nullSafe(p.getTypeEvent()));

        if (p.getDate() != null) dpDate.setValue(p.getDate().toLocalDate());

        if (p.getHeureDebut() != null) tfDebut.setText(p.getHeureDebut().toLocalTime().format(HHMM));
        if (p.getHeureFin() != null) tfFin.setText(p.getHeureFin().toLocalTime().format(HHMM));

        tfMode.setText(nullSafe(p.getMode()));
        tfStatut.setText(nullSafe(p.getStatut()));
        taDesc.setText(nullSafe(p.getDescription()));
        tfLien.setText(nullSafe(p.getLienMeeting()));
    }

    @FXML
    private void onSave() {
        lbError.setText("");

        try {
            String type = tfType.getText().trim();
            String mode = tfMode.getText().trim();
            String statut = tfStatut.getText().trim();
            String desc = taDesc.getText().trim();
            String lien = tfLien.getText().trim();

            LocalDate ld = dpDate.getValue();
            if (ld == null) {
                lbError.setText("Date is required.");
                return;
            }

            LocalTime tDebut = parseTime(tfDebut.getText());
            LocalTime tFin = parseTime(tfFin.getText());
            if (tDebut == null || tFin == null) {
                lbError.setText("Time must be in HH:mm (example 08:30).");
                return;
            }
            if (!tFin.isAfter(tDebut)) {
                lbError.setText("Heure fin must be after heure début.");
                return;
            }

            if (type.isEmpty()) { lbError.setText("Type is required."); return; }
            if (mode.isEmpty()) { lbError.setText("Mode is required."); return; }
            if (statut.isEmpty()) { lbError.setText("Statut is required."); return; }

            // Update object
            current.setTypeEvent(type);
            current.setDate(Date.valueOf(ld));
            current.setHeureDebut(Time.valueOf(tDebut));
            current.setHeureFin(Time.valueOf(tFin));
            current.setMode(mode);
            current.setStatut(statut);
            current.setDescription(desc);
            current.setLienMeeting(lien);

            service.update(current);

            if (onSavedCallback != null) onSavedCallback.run();

            closeWindow();

        } catch (Exception e) {
            lbError.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) tfId.getScene().getWindow();
        stage.close();
    }

    private LocalTime parseTime(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            // accepts "8:30" too
            LocalTime lt = LocalTime.parse(t.length() == 4 ? "0" + t : t, HHMM);
            return lt;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
