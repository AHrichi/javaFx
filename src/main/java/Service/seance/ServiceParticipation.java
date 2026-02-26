package Service.seance;

import Utils.DataSource;
import java.sql.*;
import Entite.Membre;
import java.util.ArrayList;
import java.util.List;

public class ServiceParticipation {

    private Connection getConnect() { return DataSource.getConnection(); }

    // Method to join a session
    public String participer(int idSeance, int idMembre) {
        try {
            // 1. Check if already registered
            String checkReq = "SELECT count(*) FROM participation WHERE id_seance = ? AND id_membre = ?";
            PreparedStatement psCheck = getConnect().prepareStatement(checkReq);
            psCheck.setInt(1, idSeance);
            psCheck.setInt(2, idMembre);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                return "Déjà inscrit !";
            }

            // 2. Check Capacity (Current participants vs Max Capacity)
            String capReq = "SELECT s.capacite_max, (SELECT COUNT(*) FROM participation p WHERE p.id_seance = s.id_seance) as total " +
                    "FROM Seance s WHERE s.id_seance = ?";
            PreparedStatement psCap = getConnect().prepareStatement(capReq);
            psCap.setInt(1, idSeance);
            ResultSet rsCap = psCap.executeQuery();

            if (rsCap.next()) {
                int max = rsCap.getInt("capacite_max");
                int current = rsCap.getInt("total");

                if (current >= max) {
                    return "Séance complète !";
                }
            }

            // 3. Add Reservation
            String req = "INSERT INTO participation (id_seance, id_membre) VALUES (?, ?)";
            PreparedStatement ps = getConnect().prepareStatement(req);
            ps.setInt(1, idSeance);
            ps.setInt(2, idMembre);

            ps.executeUpdate();
            return "Inscription réussie !";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur : " + e.getMessage();
        }
    }

    // Method to cancel
    public boolean annulerParticipation(int idSeance, int idMembre) {
        try {
            String req = "DELETE FROM participation WHERE id_seance=? AND id_membre=?";
            PreparedStatement ps = getConnect().prepareStatement(req);
            ps.setInt(1, idSeance);
            ps.setInt(2, idMembre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Method to count current participants
    public int countParticipants(int idSeance) {
        int count = 0;
        try {
            String req = "SELECT COUNT(*) FROM participation WHERE id_seance = ?";
            PreparedStatement ps = getConnect().prepareStatement(req);
            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public List<Membre> getParticipants(int idSeance) {
        List<Membre> list = new ArrayList<>();
        try {
            // Join User, Membre, and Participation tables
            String req = "SELECT u.*, m.* FROM User u " +
                    "JOIN Membre m ON u.id_user = m.id_membre " +
                    "JOIN participation p ON m.id_membre = p.id_membre " +
                    "WHERE p.id_seance = ?";

            PreparedStatement ps = getConnect().prepareStatement(req);
            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Membre m = new Membre();
                // Map User fields
                m.setIdUser(rs.getInt("id_user"));
                m.setNom(rs.getString("nom"));
                m.setPrenom(rs.getString("prenom"));
                m.setEmail(rs.getString("email"));
                m.setTelephone(rs.getString("telephone"));

                // Map Membre fields
                m.setTailleCm(rs.getInt("taille_cm"));
                m.setPoidsKg(rs.getDouble("poids_kg"));

                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}