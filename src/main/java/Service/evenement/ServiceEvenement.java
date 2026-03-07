package Service.evenement;

import Service.IService;
import Entite.Evenement;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceEvenement implements IService<Evenement> {
    private Connection con;

    public ServiceEvenement() {
        con = DataSource.getConnection();
    }

    @Override
    public boolean ajouter(Evenement e) throws SQLException {
        String sql = "INSERT INTO Evenement (nom, description, date_evenement, capacite_max, prix, id_club, image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getDescription());
            ps.setDate(3, e.getDateEvenement());
            ps.setInt(4, e.getCapaciteMax());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getIdClub());
            ps.setString(7, e.getImage());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Evenement e) throws SQLException {
        String sql = "UPDATE Evenement SET nom=?, description=?, date_evenement=?, " +
                "capacite_max=?, prix=?, id_club=?, image=? WHERE id_evenement=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getDescription());
            ps.setDate(3, e.getDateEvenement());
            ps.setInt(4, e.getCapaciteMax());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getIdClub());
            ps.setString(7, e.getImage());
            ps.setInt(8, e.getIdEvenement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Evenement e) throws SQLException {
        // Supprimer d'abord les inscriptions liées
        String sqlIns = "DELETE FROM InscriptionEvenement WHERE id_evenement = ?";
        try (PreparedStatement psIns = con.prepareStatement(sqlIns)) {
            psIns.setInt(1, e.getIdEvenement());
            psIns.executeUpdate();
        }

        String sql = "DELETE FROM Evenement WHERE id_evenement = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getIdEvenement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Evenement> readAll() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT e.*, " +
                "(SELECT COUNT(*) FROM InscriptionEvenement i WHERE i.id_evenement = e.id_evenement) AS nb_inscriptions "
                + "FROM Evenement e ORDER BY e.date_evenement DESC";
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Evenement ev = mapResultSet(rs);
                ev.setNbInscriptions(rs.getInt("nb_inscriptions"));
                list.add(ev);
            }
        }
        return list;
    }

    @Override
    public Evenement findbyId(int id) throws SQLException {
        String sql = "SELECT e.*, " +
                "(SELECT COUNT(*) FROM InscriptionEvenement i WHERE i.id_evenement = e.id_evenement) AS nb_inscriptions "
                + "FROM Evenement e WHERE e.id_evenement = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evenement ev = mapResultSet(rs);
                    ev.setNbInscriptions(rs.getInt("nb_inscriptions"));
                    return ev;
                }
            }
        }
        return null;
    }

    public int getNbInscriptions(int idEvenement) throws SQLException {
        String sql = "SELECT COUNT(*) FROM InscriptionEvenement WHERE id_evenement = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return 0;
    }

    public Map<String, Integer> getNbInscriptionsParEvenement() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT e.nom, COUNT(i.id_inscription) AS nb " +
                "FROM Evenement e LEFT JOIN InscriptionEvenement i ON e.id_evenement = i.id_evenement " +
                "GROUP BY e.id_evenement, e.nom";
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("nom"), rs.getInt("nb"));
            }
        }
        return map;
    }

    private Evenement mapResultSet(ResultSet rs) throws SQLException {
        return new Evenement(
                rs.getInt("id_evenement"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getDate("date_evenement"),
                rs.getInt("capacite_max"),
                rs.getDouble("prix"),
                rs.getInt("id_club"),
                rs.getString("image"));
    }
}
