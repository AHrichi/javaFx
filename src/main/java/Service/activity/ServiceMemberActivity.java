package Service.activity;

import Entite.HomeActivity;
import Entite.MemberHomeActivity;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceMemberActivity {

    private Connection getConnect() {
        return DataSource.getConnection();
    }

    private final ServiceHomeActivity serviceHomeActivity = new ServiceHomeActivity();

    /** Plan an activity for a Member */
    public boolean planifier(int memberId, int activityId, LocalDate datePlanifiee) {
        try {
            // Check if already planned on same date
            String check = "SELECT COUNT(*) FROM activite_membre WHERE id_membre=? AND id_activite=? AND date_planifiee=?";
            PreparedStatement psCheck = getConnect().prepareStatement(check);
            psCheck.setInt(1, memberId);
            psCheck.setInt(2, activityId);
            psCheck.setDate(3, Date.valueOf(datePlanifiee));
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                return false;

            String sql = "INSERT INTO activite_membre (id_membre, id_activite, date_planifiee, statut) VALUES (?, ?, ?, 'planifié')";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setInt(1, memberId);
            ps.setInt(2, activityId);
            ps.setDate(3, Date.valueOf(datePlanifiee));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Mark an activity as completed */
    public boolean marquerComplete(int id) {
        try {
            String sql = "UPDATE activite_membre SET statut='complété', date_completion=? WHERE id=?";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Delete a planned activity */
    public boolean supprimer(int id) {
        try {
            String sql = "DELETE FROM activite_membre WHERE id=?";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Get all activities for a Member (with activity details) */
    public List<MemberHomeActivity> getActivitesMembre(int memberId) {
        List<MemberHomeActivity> list = new ArrayList<>();
        try {
            String sql = "SELECT am.*, a.titre, a.categorie, a.muscles, a.image_url, a.video_url, a.difficulte, a.description, a.equipement, a.api_exercise_id "
                    +
                    "FROM activite_membre am " +
                    "JOIN activites_maison a ON am.id_activite = a.id " +
                    "WHERE am.id_membre = ? " +
                    "ORDER BY am.date_planifiee DESC";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MemberHomeActivity ma = new MemberHomeActivity();
                ma.setId(rs.getInt("id"));
                ma.setIdMembre(rs.getInt("id_membre"));
                ma.setIdActivite(rs.getInt("id_activite"));
                Date d = rs.getDate("date_planifiee");
                if (d != null)
                    ma.setDatePlanifiee(d.toLocalDate());
                ma.setStatut(rs.getString("statut"));
                Timestamp ts = rs.getTimestamp("date_completion");
                if (ts != null)
                    ma.setDateCompletion(ts.toLocalDateTime());

                // Attach activity details
                HomeActivity ha = new HomeActivity();
                ha.setId(rs.getInt("id_activite"));
                ha.setApiExerciseId(rs.getInt("api_exercise_id"));
                ha.setTitre(rs.getString("titre"));
                ha.setDescription(rs.getString("description"));
                ha.setCategorie(rs.getString("categorie"));
                ha.setMuscles(rs.getString("muscles"));
                ha.setEquipement(rs.getString("equipement"));
                ha.setImageUrl(rs.getString("image_url"));
                ha.setVideoUrl(rs.getString("video_url"));
                ha.setDifficulte(rs.getString("difficulte"));
                ma.setActivite(ha);

                list.add(ma);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Get only planned activities */
    public List<MemberHomeActivity> getActivitesPlanifiees(int memberId) {
        List<MemberHomeActivity> all = getActivitesMembre(memberId);
        all.removeIf(a -> !"planifié".equals(a.getStatut()));
        return all;
    }

    /** Get only completed activities */
    public List<MemberHomeActivity> getActivitesCompletees(int memberId) {
        List<MemberHomeActivity> all = getActivitesMembre(memberId);
        all.removeIf(a -> !"complété".equals(a.getStatut()));
        return all;
    }

    /** Count completed this week */
    public int countCompletedThisWeek(int memberId) {
        try {
            String sql = "SELECT COUNT(*) FROM activite_membre WHERE id_membre=? AND statut='complété' " +
                    "AND date_completion >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            PreparedStatement ps = getConnect().prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
