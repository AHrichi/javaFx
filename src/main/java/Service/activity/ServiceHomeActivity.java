package Service.activity;

import Entite.HomeActivity;
import Service.IService;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHomeActivity implements IService<HomeActivity> {

    private Connection getConnect() {
        return DataSource.getConnection();
    }

    @Override
    public boolean ajouter(HomeActivity a) throws SQLException {
        // Check if already saved (by API exercise ID)
        if (a.getApiExerciseId() > 0 && findByApiId(a.getApiExerciseId()) != null) {
            return false; // Already exists
        }

        String sql = "INSERT INTO activites_maison (api_exercise_id, titre, description, categorie, muscles, equipement, image_url, video_url, difficulte) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = getConnect().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, a.getApiExerciseId());
        ps.setString(2, a.getTitre());
        ps.setString(3, a.getDescription());
        ps.setString(4, a.getCategorie());
        ps.setString(5, a.getMuscles());
        ps.setString(6, a.getEquipement());
        ps.setString(7, a.getImageUrl());
        ps.setString(8, a.getVideoUrl());
        ps.setString(9, a.getDifficulte() != null ? a.getDifficulte() : "moyen");

        int rows = ps.executeUpdate();
        if (rows > 0) {
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                a.setId(keys.getInt(1));
            }
        }
        return rows > 0;
    }

    @Override
    public boolean supprimer(HomeActivity a) throws SQLException {
        String sql = "DELETE FROM activites_maison WHERE id = ?";
        PreparedStatement ps = getConnect().prepareStatement(sql);
        ps.setInt(1, a.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(HomeActivity a) throws SQLException {
        String sql = "UPDATE activites_maison SET titre=?, description=?, categorie=?, muscles=?, equipement=?, image_url=?, video_url=?, difficulte=? WHERE id=?";
        PreparedStatement ps = getConnect().prepareStatement(sql);
        ps.setString(1, a.getTitre());
        ps.setString(2, a.getDescription());
        ps.setString(3, a.getCategorie());
        ps.setString(4, a.getMuscles());
        ps.setString(5, a.getEquipement());
        ps.setString(6, a.getImageUrl());
        ps.setString(7, a.getVideoUrl());
        ps.setString(8, a.getDifficulte());
        ps.setInt(9, a.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public HomeActivity findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM activites_maison WHERE id = ?";
        PreparedStatement ps = getConnect().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return mapRow(rs);
        return null;
    }

    /** Find by wger API exercise ID */
    public HomeActivity findByApiId(int apiId) {
        try {
            String sql = "SELECT * FROM activites_maison WHERE api_exercise_id = ?";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setInt(1, apiId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<HomeActivity> readAll() throws SQLException {
        List<HomeActivity> list = new ArrayList<>();
        String sql = "SELECT * FROM activites_maison ORDER BY date_creation DESC";
        PreparedStatement ps = getConnect().prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private HomeActivity mapRow(ResultSet rs) throws SQLException {
        HomeActivity a = new HomeActivity();
        a.setId(rs.getInt("id"));
        a.setApiExerciseId(rs.getInt("api_exercise_id"));
        a.setTitre(rs.getString("titre"));
        a.setDescription(rs.getString("description"));
        a.setCategorie(rs.getString("categorie"));
        a.setMuscles(rs.getString("muscles"));
        a.setEquipement(rs.getString("equipement"));
        a.setImageUrl(rs.getString("image_url"));
        a.setVideoUrl(rs.getString("video_url"));
        a.setDifficulte(rs.getString("difficulte"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null)
            a.setDateCreation(ts.toLocalDateTime());
        return a;
    }
}
