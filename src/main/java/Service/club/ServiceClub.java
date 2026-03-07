package Service.club;

import Service.IService;

import Entite.Club;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceClub implements IService<Club> {
    private Connection con;

    public ServiceClub() {
        con = DataSource.getConnection();
    }

    @Override
    public boolean ajouter(Club c) throws SQLException {
        String sql = "INSERT INTO club (nom, adresse, ville, telephone, email, date_creation, description, evenements, photo_club, photo_salle, capacite, places_restantes, id_admin_responsable) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getAdresse());
        ps.setString(3, c.getVille());
        ps.setString(4, c.getTelephone());
        ps.setString(5, c.getEmail());
        ps.setDate(6, c.getDateCreation());
        ps.setString(7, c.getDescription());
        ps.setString(8, c.getEvenements());
        ps.setString(9, c.getPhotoClub());
        ps.setString(10, c.getPhotoSalle()); // stocke le NOM de la salle (texte)
        ps.setInt(11, c.getCapacite());
        ps.setInt(12, c.getPlacesRestantes());
        ps.setInt(13, c.getIdAdminResponsable());

        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(Club c) throws SQLException {
        String sql = "DELETE FROM club WHERE id_club = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, c.getIdClub());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Club c) throws SQLException {
        String sql = "UPDATE club SET nom = ?, adresse = ?, ville = ?, telephone = ?, email = ?, " +
                "description = ?, evenements = ?, photo_club = ?, photo_salle = ?, capacite = ?, places_restantes = ?, id_admin_responsable = ? "
                + "WHERE id_club = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getAdresse());
        ps.setString(3, c.getVille());
        ps.setString(4, c.getTelephone());
        ps.setString(5, c.getEmail());
        ps.setString(6, c.getDescription());
        ps.setString(7, c.getEvenements());
        ps.setString(8, c.getPhotoClub());
        ps.setString(9, c.getPhotoSalle()); // stocke le NOM de la salle (texte)
        ps.setInt(10, c.getCapacite());
        ps.setInt(11, c.getPlacesRestantes());
        ps.setInt(12, c.getIdAdminResponsable());
        ps.setInt(13, c.getIdClub());

        return ps.executeUpdate() > 0;
    }

    @Override
    public List<Club> readAll() throws SQLException {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT * FROM club";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            clubs.add(mapResultSetToClub(rs));
        }
        return clubs;
    }

    @Override
    public Club findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM club WHERE id_club = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToClub(rs);
        }
        return null;
    }

    private Club mapResultSetToClub(ResultSet rs) throws SQLException {
        return new Club(
                rs.getInt("id_club"),
                rs.getString("nom"),
                rs.getString("adresse"),
                rs.getString("ville"),
                rs.getString("telephone"),
                rs.getString("email"),
                rs.getDate("date_creation"),
                rs.getString("description"),
                rs.getString("evenements"),
                rs.getString("photo_club"),
                rs.getString("photo_salle"), // nom de la salle (champ texte)
                rs.getInt("capacite"),
                rs.getInt("places_restantes"),
                rs.getInt("id_admin_responsable"));
    }

    public boolean clubExiste(String nom, String description) throws SQLException {
        String sql = "SELECT COUNT(*) FROM club WHERE nom = ? OR description = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nom);
        ps.setString(2, description);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    public List<Club> getAllClubs() throws SQLException {
        return readAll();
    }

    // --- KPI Methods for Home ---
    public int countClubs() {
        try {
            String sql = "SELECT COUNT(*) FROM club";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countMembres() {
        try {
            String sql = "SELECT COUNT(*) FROM user WHERE type_user = 'Membre'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countSeances() {
        try {
            String sql = "SELECT COUNT(*) FROM seance";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double avgSatisfaction() {
        return 4.5; // Placeholder for now
    }

    public List<Club> getTopClubs(int limit) {
        try {
            List<Club> clubs = new ArrayList<>();
            String sql = "SELECT * FROM club LIMIT ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clubs.add(mapResultSetToClub(rs));
            }
            return clubs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getMemberCount(int idClub) {
        return 0;
    }

    public int getCoachCount(int idClub) {
        return 0;
    }

    public double getAvgSatisfaction(int idClub) {
        return 5.0;
    }
}
