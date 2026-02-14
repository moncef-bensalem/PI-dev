package conttrollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Planification;
import services.ServicePlanification;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherPlanification implements Initializable {

    @FXML
    private VBox cardsBox;

    private final ServicePlanification service = new ServicePlanification();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCards();
    }

    @FXML
    public void onRefresh() {
        loadCards();
    }

    private void loadCards() {
        cardsBox.getChildren().clear();

        List<Planification> list = service.getAll();

        if (list.isEmpty()) {
            Label empty = new Label("No planifications found.");
            empty.setStyle("-fx-font-size: 14px; -fx-opacity: 0.7;");
            cardsBox.getChildren().add(empty);
            return;
        }

        for (Planification p : list) {
            cardsBox.getChildren().add(createCard(p));
        }
    }

    private Pane createCard(Planification p) {

        // Card container
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 2);"
        );

        // Header (ID + Type)
        Label title = new Label("ID: " + p.getIdEvent() + "  |  " + safe(p.getTypeEvent()));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Details block
        Label details = new Label(
                "Date: " + safe(p.getDate()) + "\n" +
                        "Debut: " + safe(p.getHeureDebut()) + "   Fin: " + safe(p.getHeureFin()) + "\n" +
                        "Mode: " + safe(p.getMode()) + "   Statut: " + safe(p.getStatut()) + "\n" +
                        "Description: " + safe(p.getDescription()) + "\n" +
                        "Lien: " + safe(p.getLienMeeting())
        );
        details.setWrapText(true);
        details.setStyle("-fx-font-size: 13px;");

        // Buttons row
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");

        btnUpdate.setOnAction(e -> onUpdateCard(p));
        btnDelete.setOnAction(e -> onDeleteCard(p));

        HBox actions = new HBox(10, btnUpdate, btnDelete);
        actions.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(title, details, actions);
        return card;
    }

    private void onDeleteCard(Planification p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete planification ID " + p.getIdEvent() + " ?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            service.delete(p.getIdEvent());
            loadCards();
        }
    }

    private void onUpdateCard(Planification p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdatePlanification.fxml"));
            Parent root = loader.load();

            UpdatePlanificationController ctrl = loader.getController();
            ctrl.setData(p, this::loadCards); // prefill + refresh after save

            Stage stage = new Stage();
            stage.setTitle("Update Planification - ID " + p.getIdEvent());
            stage.setScene(new Scene(root, 650, 520));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String safe(Object o) {
        return (o == null) ? "" : o.toString();
    }
}
