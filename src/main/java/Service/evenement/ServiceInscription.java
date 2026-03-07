package Service.evenement;

import Service.IService;


import Entite.InscriptionEvenement;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceInscription implements IService<InscriptionEvenement> {
    private Connection con;

    public ServiceInscription() {
        con = DataSource.getConnection();
    }

    @Override
    public boolean ajouter(InscriptionEvenement inscription) throws SQLException {
        return inscrire(inscription);
    }

    @Override
    public boolean modifier(InscriptionEvenement inscription) throws SQLException {
        // Not implemented in original but required by IService
        String sql = "UPDATE InscriptionEvenement SET nom_participant = ?, email = ?, date_inscription = ? WHERE id_inscription = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, inscription.getNomParticipant());
        ps.setString(2, inscription.getEmail());
        ps.setDate(3, inscription.getDateInscription());
        ps.setInt(4, inscription.getIdInscription());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(InscriptionEvenement inscription) throws SQLException {
        return supprimer(inscription.getIdInscription());
    }

    @Override
    public List<InscriptionEvenement> readAll() throws SQLException {
        return afficherTous();
    }

    @Override
    public InscriptionEvenement findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM InscriptionEvenement WHERE id_inscription = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new InscriptionEvenement(
                    rs.getInt("id_inscription"),
                    rs.getString("nom_participant"),
                    rs.getString("email"),
                    rs.getDate("date_inscription"),
                    rs.getInt("id_evenement"));
        }
        return null;
    }

    /**
     * Inscrit un participant après avoir vérifié la capacité.
     */
    public boolean inscrire(InscriptionEvenement inscription) throws SQLException {
        // Vérifier capacité
        String sqlCap = "SELECT e.capacite_max, COUNT(i.id_inscription) AS nb " +
                "FROM Evenement e LEFT JOIN InscriptionEvenement i ON e.id_evenement = i.id_evenement " +
                "WHERE e.id_evenement = ? GROUP BY e.id_evenement, e.capacite_max";
        PreparedStatement psCap = con.prepareStatement(sqlCap);
        psCap.setInt(1, inscription.getIdEvenement());
        ResultSet rs = psCap.executeQuery();

        if (rs.next()) {
            int capaciteMax = rs.getInt("capacite_max");
            int nbActuel = rs.getInt("nb");
            if (nbActuel >= capaciteMax) {
                return false; // Événement complet
            }
        }

        // Insérer l'inscription
        String sql = "INSERT INTO InscriptionEvenement (nom_participant, email, date_inscription, id_evenement) " +
                "VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, inscription.getNomParticipant());
        ps.setString(2, inscription.getEmail());
        ps.setDate(3, inscription.getDateInscription());
        ps.setInt(4, inscription.getIdEvenement());
        return ps.executeUpdate() > 0;
    }

    public boolean supprimer(int idInscription) throws SQLException {
        String sql = "DELETE FROM InscriptionEvenement WHERE id_inscription = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idInscription);
        return ps.executeUpdate() > 0;
    }

    public List<InscriptionEvenement> afficherParEvenement(int idEvenement) throws SQLException {
        List<InscriptionEvenement> list = new ArrayList<>();
        String sql = "SELECT * FROM InscriptionEvenement WHERE id_evenement = ? ORDER BY date_inscription DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idEvenement);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new InscriptionEvenement(
                    rs.getInt("id_inscription"),
                    rs.getString("nom_participant"),
                    rs.getString("email"),
                    rs.getDate("date_inscription"),
                    rs.getInt("id_evenement")));
        }
        return list;
    }

    public List<InscriptionEvenement> afficherTous() throws SQLException {
        List<InscriptionEvenement> list = new ArrayList<>();
        String sql = "SELECT * FROM InscriptionEvenement ORDER BY date_inscription DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new InscriptionEvenement(
                    rs.getInt("id_inscription"),
                    rs.getString("nom_participant"),
                    rs.getString("email"),
                    rs.getDate("date_inscription"),
                    rs.getInt("id_evenement")));
        }
        return list;
    }

    public boolean emailDejaInscrit(String email, int idEvenement) throws SQLException {
        String sql = "SELECT COUNT(*) FROM InscriptionEvenement WHERE email = ? AND id_evenement = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ps.setInt(2, idEvenement);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getInt(1) > 0;
        return false;
    }
}
