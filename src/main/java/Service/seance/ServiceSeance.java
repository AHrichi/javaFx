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

    private boolean isCoachAvailable(int idCoach, Timestamp debut, Timestamp fin, int idSeanceAExclure) throws SQLException {
        String req = "SELECT count(*) FROM Seance WHERE id_coach = ? AND id_seance != ? AND date_debut < ? AND date_fin > ?";
        PreparedStatement ps = getConnect().prepareStatement(req);
        ps.setInt(1, idCoach);
        ps.setInt(2, idSeanceAExclure);
        ps.setTimestamp(3, fin);
        ps.setTimestamp(4, debut);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) == 0;
        }
        return true;
    }

    private void validerDonneesSeance(Seance seance) throws SQLException {
        Timestamp debut = Timestamp.valueOf(seance.getDateDebut());
        Timestamp fin = Timestamp.valueOf(seance.getDateFin());

        if (fin.before(debut) || fin.equals(debut)) {
            throw new SQLException("Erreur : L'heure de fin doit être strictement postérieure à l'heure de début.");
        }

        if (seance.getCapaciteMax() <= 0) {
            throw new SQLException("Erreur : La capacité maximale doit être d'au moins 1 participant.");
        }
    }

    @Override
    public boolean ajouter(Seance seance) throws SQLException {
        validerDonneesSeance(seance);

        Timestamp debut = Timestamp.valueOf(seance.getDateDebut());
        Timestamp fin = Timestamp.valueOf(seance.getDateFin());

        if (!isCoachAvailable(seance.getIdCoach(), debut, fin, -1)) {
            throw new SQLException("Conflit d'horaire : Le coach sélectionné a déjà une séance prévue sur cette plage horaire.");
        }

        String req = "INSERT INTO Seance " +
                "(titre, description, date_debut, date_fin, id_club, id_coach, type_seance, niveau, capacite_max, statut, photo_seance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = getConnect().prepareStatement(req);

        ps.setString(1, seance.getTitre());
        ps.setString(2, seance.getDescription());
        ps.setTimestamp(3, debut);
        ps.setTimestamp(4, fin);
        ps.setInt(5, seance.getIdClub());
        ps.setInt(6, seance.getIdCoach());
        ps.setString(7, seance.getTypeSeance());
        ps.setString(8, seance.getNiveau());
        ps.setInt(9, seance.getCapaciteMax());
        ps.setString(10, seance.getStatut());
        ps.setString(11, seance.getPhotoSeance());

        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Seance seance) throws SQLException {
        validerDonneesSeance(seance);

        Timestamp debut = Timestamp.valueOf(seance.getDateDebut());
        Timestamp fin = Timestamp.valueOf(seance.getDateFin());

        if (!isCoachAvailable(seance.getIdCoach(), debut, fin, seance.getIdSeance())) {
            throw new SQLException("Conflit d'horaire : Le coach a été assigné à une autre séance sur cette plage horaire.");
        }

        // AJOUT DE id_club=? ICI
        String req = "UPDATE Seance SET titre=?, description=?, date_debut=?, date_fin=?, capacite_max=?, statut=?, id_coach=?, id_club=? " +
                "WHERE id_seance=?";

        PreparedStatement ps = getConnect().prepareStatement(req);

        ps.setString(1, seance.getTitre());
        ps.setString(2, seance.getDescription());
        ps.setTimestamp(3, debut);
        ps.setTimestamp(4, fin);
        ps.setInt(5, seance.getCapaciteMax());
        ps.setString(6, seance.getStatut());
        ps.setInt(7, seance.getIdCoach());
        ps.setInt(8, seance.getIdClub()); // Mise à jour du club
        ps.setInt(9, seance.getIdSeance());

        boolean isUpdated = ps.executeUpdate() > 0;

        if (isUpdated) {
            notifyParticipants(seance.getIdSeance(),
                    "Séance modifiée : " + seance.getTitre(),
                    "Bonjour,\n\nLes détails de la séance '" + seance.getTitre() + "' prévue le " + seance.getDateDebut().toLocalDate() + " ont été mis à jour.\n\nL'équipe SportLink."
            );
        }

        return isUpdated;
    }

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

    @Override
    public boolean supprimer(Seance seance) throws SQLException {
        notifyParticipants(seance.getIdSeance(),
                "Séance annulée : " + seance.getTitre(),
                "Bonjour,\n\nLa séance '" + seance.getTitre() + "' prévue le " + seance.getDateDebut() + " a été annulée par le coach.\n\nL'équipe SportLink."
        );

        String req = "DELETE FROM Seance WHERE id_seance = ?";
        PreparedStatement ps = getConnect().prepareStatement(req);
        ps.setInt(1, seance.getIdSeance());

        return ps.executeUpdate() > 0;
    }

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
            seance.setIdClub(rs.getInt("id_club"));
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