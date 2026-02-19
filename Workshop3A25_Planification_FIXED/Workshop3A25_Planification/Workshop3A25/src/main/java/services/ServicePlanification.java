package services;

import models.Planification;
import utils.MyDataBase;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServicePlanification {

    private final Connection cnx;

    public ServicePlanification() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Planification p) {
        if (cnx == null) {
            throw new RuntimeException("DB connection is null (check MySQL driver / Laragon / URL).");
        }

        String sql = "INSERT INTO planification " +
                "(typeEvent, date, heureDebut, heureFin, mode, statut, description, lienMeeting) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getTypeEvent());
            ps.setDate(2, p.getDate());
            ps.setTime(3, p.getHeureDebut());
            ps.setTime(4, p.getHeureFin());
            ps.setString(5, p.getMode());
            ps.setString(6, p.getStatut());
            ps.setString(7, p.getDescription());
            ps.setString(8, p.getLienMeeting());

            ps.executeUpdate();
            System.out.println("Planification inserted ✅");
        } catch (SQLException e) {
            throw new RuntimeException("SQL error: " + e.getMessage(), e);
        }
    }
    public List<Planification> getAll() {

        if (cnx == null) {
            throw new RuntimeException("DB connection is null (check MySQL driver / Laragon / URL).");
        }

        List<Planification> list = new ArrayList<>();
        String sql = "SELECT * FROM planification";

        try {
            ResultSet rs = cnx.createStatement().executeQuery(sql);

            while (rs.next()) {
                Planification p = new Planification(
                        rs.getInt("idEvent"),
                        rs.getString("typeEvent"),
                        rs.getDate("date"),
                        rs.getTime("heureDebut"),
                        rs.getTime("heureFin"),
                        rs.getString("mode"),
                        rs.getString("statut"),
                        rs.getString("description"),
                        rs.getString("lienMeeting")
                );
                list.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching planifications: " + e.getMessage(), e);
        }

        return list;
    }

    public void delete(int idEvent) {
        if (cnx == null) {
            throw new RuntimeException("DB connection is null (check MySQL driver / Laragon / URL).");
        }

        String sql = "DELETE FROM planification WHERE idEvent = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEvent);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL error: " + e.getMessage(), e);
        }
    }

    public void update(Planification p) {
        if (cnx == null) {
            throw new RuntimeException("DB connection is null (check MySQL driver / Laragon / URL).");
        }

        String sql = "UPDATE planification SET " +
                "typeEvent = ?, date = ?, heureDebut = ?, heureFin = ?, mode = ?, statut = ?, description = ?, lienMeeting = ? " +
                "WHERE idEvent = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getTypeEvent());
            ps.setDate(2, p.getDate());
            ps.setTime(3, p.getHeureDebut());
            ps.setTime(4, p.getHeureFin());
            ps.setString(5, p.getMode());
            ps.setString(6, p.getStatut());
            ps.setString(7, p.getDescription());
            ps.setString(8, p.getLienMeeting());
            ps.setInt(9, p.getIdEvent());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL error: " + e.getMessage(), e);
        }
    }

}
