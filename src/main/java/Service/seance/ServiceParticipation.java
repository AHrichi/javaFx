package Service.seance;

import Service.IService;
import Utils.DataSource;
import Utils.EmailService;
import Service.notification.AutoNotificationService;
import java.sql.*;
import Entite.Membre;
import Entite.Seance;
import net.fortuna.ical4j.model.component.Participant;

import java.util.ArrayList;
import java.util.List;

public class ServiceParticipation implements IService<Participant> {

    private Connection getConnect() {
        return DataSource.getConnection();
    }

    private ServiceSeance serviceSeance = new ServiceSeance();

    // Method to join a session
    public String participer(int idSeance, int idMembre) {
        try {
            // 1. Fetch target seance details to check time and status
            String seanceReq = "SELECT date_debut, date_fin, statut FROM seance WHERE id_seance = ?";
            PreparedStatement psTarget = getConnect().prepareStatement(seanceReq);
            psTarget.setInt(1, idSeance);
            ResultSet rsTarget = psTarget.executeQuery();

            if (!rsTarget.next()) {
                return "Séance introuvable !";
            }

            Timestamp targetDebut = rsTarget.getTimestamp("date_debut");
            Timestamp targetFin = rsTarget.getTimestamp("date_fin");
            String statut = rsTarget.getString("statut");

            // EDGE CASE: Prevent joining past or cancelled sessions
            if (targetDebut.before(new Timestamp(System.currentTimeMillis()))) {
                return "Action refusée : Vous ne pouvez pas rejoindre une séance déjà commencée ou passée.";
            }
            if (!"planifiée".equals(statut)) {
                return "Action refusée : Cette séance est " + statut + ".";
            }

            // 2. Check if already registered
            String checkReq = "SELECT count(*) FROM participation WHERE id_seance = ? AND id_membre = ?";
            PreparedStatement psCheck = getConnect().prepareStatement(checkReq);
            psCheck.setInt(1, idSeance);
            psCheck.setInt(2, idMembre);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                return "Déjà inscrit !";
            }

            // 3. EDGE CASE: Check overlapping sessions for this Member
            String overlapReq = "SELECT count(*) FROM participation p " +
                    "JOIN seance s ON p.id_seance = s.id_seance " +
                    "WHERE p.id_membre = ? AND p.id_seance != ? " +
                    "AND s.date_debut < ? AND s.date_fin > ?";
            PreparedStatement psOverlap = getConnect().prepareStatement(overlapReq);
            psOverlap.setInt(1, idMembre);
            psOverlap.setInt(2, idSeance);
            psOverlap.setTimestamp(3, targetFin);
            psOverlap.setTimestamp(4, targetDebut);
            ResultSet rsOverlap = psOverlap.executeQuery();
            if (rsOverlap.next() && rsOverlap.getInt(1) > 0) {
                return "Conflit d'horaire : Le membre a déjà une autre séance prévue à ce moment-là !";
            }

            // 4. Check Capacity (Current participants vs Max Capacity)
            String capReq = "SELECT s.capacite_max, s.titre, s.id_coach, (SELECT COUNT(*) FROM participation p WHERE p.id_seance = s.id_seance) as total "
                    +
                    "FROM seance s WHERE s.id_seance = ?";
            PreparedStatement psCap = getConnect().prepareStatement(capReq);
            psCap.setInt(1, idSeance);
            ResultSet rsCap = psCap.executeQuery();

            int max = 0;
            int current = 0;
            String seanceTitre = "";
            int coachId = 0;

            if (rsCap.next()) {
                max = rsCap.getInt("capacite_max");
                current = rsCap.getInt("total");
                seanceTitre = rsCap.getString("titre");
                coachId = rsCap.getInt("id_coach");

                if (current >= max) {
                    return "Séance complète !";
                }
            }

            if (idMembre <= 0) {
                return "Erreur : ID Membre invalide (" + idMembre + "). Vérifiez le mappage dans ServiceMembre.";
            }

            String checkMembreReq = "SELECT id_membre FROM membre WHERE id_membre = ?";
            PreparedStatement psCheckMembre = getConnect().prepareStatement(checkMembreReq);
            psCheckMembre.setInt(1, idMembre);
            ResultSet rsCheckMembre = psCheckMembre.executeQuery();

            if (!rsCheckMembre.next()) {
                // Explicitly insert into membre table
                String insertMembreReq = "INSERT INTO membre (id_membre) VALUES (?)";
                PreparedStatement psInsertMembre = getConnect().prepareStatement(insertMembreReq);
                psInsertMembre.setInt(1, idMembre);
                psInsertMembre.executeUpdate();
            }

            // 5. Add Reservation
            String req = "INSERT INTO participation (id_seance, id_membre) VALUES (?, ?)";
            PreparedStatement ps = getConnect().prepareStatement(req);
            ps.setInt(1, idSeance);
            ps.setInt(2, idMembre);
            ps.executeUpdate();

            // 6. Auto-notifications + Email
            // We run DB operations on the main thread to prevent ResultSet concurrency crashes.
            // The actual email sending over the network will still be asynchronous via EmailService.
            try {
                // #2 — Confirm to member
                AutoNotificationService.notifyParticipationConfirmed(idMembre, idSeance, seanceTitre);

                // #5 — Notify coach of new participant
                String membreName = getMembreName(idMembre);
                AutoNotificationService.notifyCoachNewParticipant(coachId, seanceTitre, membreName);

                // #8 — If session is now full, notify admin
                if (current + 1 >= max) {
                    AutoNotificationService.notifySessionFull(seanceTitre);
                }

                // Send Email
                sendParticipationEmail(idSeance, idMembre, "Ajout à une séance",
                        "Bonjour,\n\nVous avez été ajouté avec succès à la séance : ");
            } catch (Exception ex) {
                System.err.println("Notification error: " + ex.getMessage());
            }

            return "Inscription réussie !";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur : " + e.getMessage();
        }
    }

    // Method to cancel
    public boolean annulerParticipation(int idSeance, int idMembre) {
        try {
            // Send email before deleting to ensure we still have the session data
            sendParticipationEmail(idSeance, idMembre, "Annulation de participation",
                    "Bonjour,\n\nVotre participation a été annulée pour la séance : ");

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

    // Helper method to fetch details and send email
    private void sendParticipationEmail(int idSeance, int idMembre, String subject, String messagePrefix) {
        try {
            // Fetch Member Email
            String emailReq = "SELECT email, prenom FROM user WHERE id_user = ?";
            PreparedStatement psUser = getConnect().prepareStatement(emailReq);
            psUser.setInt(1, idMembre);
            ResultSet rsUser = psUser.executeQuery();

            if (rsUser.next()) {
                String email = rsUser.getString("email");

                // Fetch Seance Details
                Seance seance = serviceSeance.findbyId(idSeance);
                if (seance != null) {
                    String body = messagePrefix + seance.getTitre() +
                            "\nDate: " + seance.getDateDebut().toLocalDate() +
                            "\nHeure: " + seance.getDateDebut().toLocalTime() +
                            "\n\nL'équipe SportLink.";
                    EmailService.envoyerEmail(email, subject, body);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            String req = "SELECT u.*, m.* FROM user u " +
                    "JOIN membre m ON u.id_user = m.id_membre " +
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

    @Override
    public boolean ajouter(Participant participant) throws SQLException {
        return false;
    }

    @Override
    public boolean supprimer(Participant participant) throws SQLException {
        return false;
    }

    @Override
    public boolean modifier(Participant participant) throws SQLException {
        return false;
    }

    @Override
    public Participant findbyId(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Participant> readAll() throws SQLException {
        return new ArrayList<>();
    }

    /** Looks up a Member's full name from the User table. */
    private String getMembreName(int membreId) {
        try {
            String sql = "SELECT CONCAT(prenom, ' ', nom) AS full_name FROM user WHERE id_user=?";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setInt(1, membreId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("full_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Un membre";
    }
}