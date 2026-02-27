package Service.seance;

import Service.IService;
import Entite.Seance;
import Entite.Membre;
import Utils.DataSource;
import Utils.EmailService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceSeance implements IService<Seance> {

    private Connection getConnect() { return DataSource.getConnection(); }
    private Statement st;

    public ServiceSeance() {
        try {
            st = getConnect().createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // ✅ Ajouter une séance
    @Override
    public boolean ajouter(Seance seance) throws SQLException {

        String req = "INSERT INTO Seance " +
                "(titre, description, date_debut, date_fin, id_club, id_coach, type_seance, niveau, capacite_max, statut, photo_seance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = getConnect().prepareStatement(req);

        ps.setString(1, seance.getTitre());
        ps.setString(2, seance.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(seance.getDateDebut()));
        ps.setTimestamp(4, Timestamp.valueOf(seance.getDateFin()));
        ps.setInt(5, seance.getIdClub());
        ps.setInt(6, seance.getIdCoach());
        ps.setString(7, seance.getTypeSeance());
        ps.setString(8, seance.getNiveau());
        ps.setInt(9, seance.getCapaciteMax());
        ps.setString(10, seance.getStatut());
        ps.setString(11, seance.getPhotoSeance());

        return ps.executeUpdate() > 0;
    }

    // ✅ Afficher toutes les séances
    @Override
    public List<Seance> readAll() throws SQLException {

        List<Seance> list = new ArrayList<>();

        String query = "SELECT * FROM Seance";
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {

            Seance seance = new Seance();

            seance.setIdSeance(rs.getInt("id_seance"));
            seance.setTitre(rs.getString("titre"));
            seance.setDescription(rs.getString("description"));

            seance.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
            seance.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());

            seance.setIdClub(rs.getInt("id_club"));
            seance.setIdCoach(rs.getInt("id_coach"));

            seance.setTypeSeance(rs.getString("type_seance"));
            seance.setNiveau(rs.getString("niveau"));

            seance.setCapaciteMax(rs.getInt("capacite_max"));
            seance.setStatut(rs.getString("statut"));
            seance.setPhotoSeance(rs.getString("photo_seance"));

            list.add(seance);
        }

        return list;
    }

    // ✅ Supprimer une séance
    @Override
    public boolean supprimer(Seance seance) throws SQLException {
        // 1. Notify participants before deleting the session
        notifyParticipants(seance.getIdSeance(),
                "Séance annulée : " + seance.getTitre(),
                "Bonjour,\n\nLa séance '" + seance.getTitre() + "' prévue le " + seance.getDateDebut() + " a été annulée par le coach.\n\nL'équipe SportLink."
        );

        // 2. Delete the session
        String req = "DELETE FROM Seance WHERE id_seance = ?";
        PreparedStatement ps = getConnect().prepareStatement(req);
        ps.setInt(1, seance.getIdSeance());

        return ps.executeUpdate() > 0;
    }

    // ✅ Modifier une séance
    @Override
    public boolean modifier(Seance seance) throws SQLException {

        String req = "UPDATE Seance SET titre=?, description=?, date_debut=?, date_fin=?, capacite_max=?, statut=? " +
                "WHERE id_seance=?";

        PreparedStatement ps = getConnect().prepareStatement(req);

        ps.setString(1, seance.getTitre());
        ps.setString(2, seance.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(seance.getDateDebut()));
        ps.setTimestamp(4, Timestamp.valueOf(seance.getDateFin()));
        ps.setInt(5, seance.getCapaciteMax());
        ps.setString(6, seance.getStatut());
        ps.setInt(7, seance.getIdSeance());

        boolean isUpdated = ps.executeUpdate() > 0;

        // 3. Notify participants of the update
        if (isUpdated) {
            notifyParticipants(seance.getIdSeance(),
                    "Séance modifiée : " + seance.getTitre(),
                    "Bonjour,\n\nLes détails de la séance '" + seance.getTitre() + "' prévue le " + seance.getDateDebut().toLocalDate() + " ont été mis à jour.\n\nL'équipe SportLink."
            );
        }

        return isUpdated;
    }

    // ✅ Find by ID
    @Override
    public Seance findbyId(int id) throws SQLException {

        String req = "SELECT * FROM Seance WHERE id_seance=?";
        PreparedStatement ps = getConnect().prepareStatement(req);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Seance seance = new Seance();

            seance.setIdSeance(rs.getInt("id_seance"));
            seance.setTitre(rs.getString("titre"));
            seance.setDescription(rs.getString("description"));
            seance.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
            seance.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());

            return seance;
        }

        return null;
    }

    private void notifyParticipants(int idSeance, String subject, String body) {
        ServiceParticipation sp = new ServiceParticipation();
        List<Membre> participants = sp.getParticipants(idSeance);

        for (Membre m : participants) {
            if (m.getEmail() != null && !m.getEmail().isEmpty()) {
                EmailService.envoyerEmail(m.getEmail(), subject, body);
            }
        }
    }
}