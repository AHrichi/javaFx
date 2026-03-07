package Service.club;

import Entite.Club;
import Service.IService;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClubService implements IService<Club> {

    private Connection getConnect() {
        return DataSource.getConnection();
    }

    // ── CRUD ──────────────────────────────────────────────

    @Override
    public boolean ajouter(Club c) throws SQLException {
        String sql = "INSERT INTO club (nom, adresse, ville, telephone, email, date_creation, description, photo_club, photo_salle, id_admin_responsable) VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = getConnect().prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getAdresse());
        ps.setString(3, c.getVille());
        ps.setString(4, c.getTelephone());
        ps.setString(5, c.getEmail());
        ps.setDate(6, c.getDateCreation());
        ps.setString(7, c.getDescription());
        ps.setString(8, c.getPhotoClub());
        ps.setString(9, c.getPhotoSalle());
        ps.setInt(10, c.getIdAdminResponsable());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(Club c) throws SQLException {
        PreparedStatement ps = getConnect().prepareStatement("DELETE FROM club WHERE id_club = ?");
        ps.setInt(1, c.getIdClub());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Club c) throws SQLException {
        String sql = "UPDATE club SET nom=?, adresse=?, ville=?, telephone=?, email=?, description=?, photo_club=?, photo_salle=? WHERE id_club=?";
        PreparedStatement ps = getConnect().prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getAdresse());
        ps.setString(3, c.getVille());
        ps.setString(4, c.getTelephone());
        ps.setString(5, c.getEmail());
        ps.setString(6, c.getDescription());
        ps.setString(7, c.getPhotoClub());
        ps.setString(8, c.getPhotoSalle());
        ps.setInt(9, c.getIdClub());
        return ps.executeUpdate() > 0;
    }

    @Override
    public Club findbyId(int id) throws SQLException {
        PreparedStatement ps = getConnect().prepareStatement("SELECT * FROM club WHERE id_club = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return mapRow(rs);
        return null;
    }

    @Override
    public List<Club> readAll() throws SQLException {
        List<Club> list = new ArrayList<>();
        ResultSet rs = getConnect().createStatement().executeQuery("SELECT * FROM club ORDER BY nom");
        while (rs.next())
            list.add(mapRow(rs));
        return list;
    }

    // ── KPI / Stats queries ──────────────────────────────

    public int countClubs() {
        try {
            ResultSet rs = getConnect().createStatement().executeQuery("SELECT COUNT(*) FROM club");
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countMembres() {
        try {
            ResultSet rs = getConnect().createStatement()
                    .executeQuery("SELECT COUNT(DISTINCT id_membre) FROM club_membre");
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countSeances() {
        try {
            ResultSet rs = getConnect().createStatement().executeQuery("SELECT COUNT(*) FROM seance");
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double avgSatisfaction() {
        try {
            ResultSet rs = getConnect().createStatement().executeQuery(
                    "SELECT AVG(satisfaction) FROM participation WHERE satisfaction IS NOT NULL");
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Per-club stats ───────────────────────────────────

    public int getMemberCount(int clubId) {
        try {
            PreparedStatement ps = getConnect().prepareStatement("SELECT COUNT(*) FROM club_membre WHERE id_club = ?");
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCoachCount(int clubId) {
        try {
            PreparedStatement ps = getConnect().prepareStatement("SELECT COUNT(*) FROM club_coach WHERE id_club = ?");
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSeanceCount(int clubId) {
        try {
            PreparedStatement ps = getConnect().prepareStatement("SELECT COUNT(*) FROM seance WHERE id_club = ?");
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAvgSatisfaction(int clubId) {
        try {
            PreparedStatement ps = getConnect().prepareStatement(
                    "SELECT AVG(p.satisfaction) FROM participation p " +
                            "JOIN seance s ON p.id_seance = s.id_seance " +
                            "WHERE s.id_club = ? AND p.satisfaction IS NOT NULL");
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double val = rs.getDouble(1);
                if (!rs.wasNull())
                    return val;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Top clubs by Member count ────────────────────────

    public List<Club> getTopClubs(int limit) {
        List<Club> list = new ArrayList<>();
        try {
            PreparedStatement ps = getConnect().prepareStatement(
                    "SELECT c.* FROM club c " +
                            "LEFT JOIN club_membre cm ON c.id_club = cm.id_club " +
                            "GROUP BY c.id_club ORDER BY COUNT(cm.id_membre) DESC LIMIT ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Search / Filter ──────────────────────────────────

    public List<Club> searchClubs(String keyword, String villeFilter) {
        List<Club> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM club WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND nom LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        if (villeFilter != null && !villeFilter.isBlank() && !villeFilter.equalsIgnoreCase("Toutes")) {
            sql.append(" AND ville = ?");
            params.add(villeFilter);
        }
        sql.append(" ORDER BY nom");

        try {
            PreparedStatement ps = getConnect().prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getDistinctVilles() {
        List<String> villes = new ArrayList<>();
        try {
            ResultSet rs = getConnect().createStatement().executeQuery(
                    "SELECT DISTINCT ville FROM club WHERE ville IS NOT NULL ORDER BY ville");
            while (rs.next())
                villes.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return villes;
    }
    // ── Seances by club ──────────────────────────────────

    public List<Entite.Seance> getSeancesByClub(int clubId) {
        List<Entite.Seance> list = new ArrayList<>();
        try {
            PreparedStatement ps = getConnect().prepareStatement(
                    "SELECT * FROM seance WHERE id_club = ? ORDER BY date_debut DESC");
            ps.setInt(1, clubId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Entite.Seance s = new Entite.Seance();
                s.setIdSeance(rs.getInt("id_seance"));
                s.setTitre(rs.getString("titre"));
                s.setDescription(rs.getString("description"));
                Timestamp tsStart = rs.getTimestamp("date_debut");
                if (tsStart != null)
                    s.setDateDebut(tsStart.toLocalDateTime());
                Timestamp tsEnd = rs.getTimestamp("date_fin");
                if (tsEnd != null)
                    s.setDateFin(tsEnd.toLocalDateTime());
                s.setIdClub(rs.getInt("id_club"));
                s.setIdCoach(rs.getInt("id_coach"));
                s.setTypeSeance(rs.getString("type_seance"));
                s.setNiveau(rs.getString("niveau"));
                s.setCapaciteMax(rs.getInt("capacite_max"));
                s.setStatut(rs.getString("statut"));
                s.setPhotoSeance(rs.getString("photo_seance"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Coach name by id ────────────────────────────────

    public String getCoachName(int coachId) {
        try {
            PreparedStatement ps = getConnect().prepareStatement(
                    "SELECT CONCAT(nom, ' ', prenom) FROM user WHERE id_user = ?");
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    // ── Helper ───────────────────────────────────────────

    private Club mapRow(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setIdClub(rs.getInt("id_club"));
        c.setNom(rs.getString("nom"));
        c.setAdresse(rs.getString("adresse"));
        c.setVille(rs.getString("ville"));
        c.setTelephone(rs.getString("telephone"));
        c.setEmail(rs.getString("email"));
        Date d = rs.getDate("date_creation");
        if (d != null)
            c.setDateCreation(d);
        c.setDescription(rs.getString("description"));
        c.setPhotoClub(rs.getString("photo_club"));
        c.setPhotoSalle(rs.getString("photo_salle"));
        c.setIdAdminResponsable(rs.getInt("id_admin_responsable"));
        return c;
    }
}
