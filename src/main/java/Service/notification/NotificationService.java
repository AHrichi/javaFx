package Service.notification;

import Entite.Notification;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements INotificationService {

    @Override
    public void create(Notification n) {
        if (n.getTitle() == null || n.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Le titre est requis");
        if (n.getMessage() == null || n.getMessage().trim().isEmpty())
            throw new IllegalArgumentException("Le message est requis");
        if (n.getRecipientRole() == null)
            throw new IllegalArgumentException("Le rôle destinataire est requis");

        String sql = "INSERT INTO notifications (title, message, recipient_id, recipient_role, type, read_status, created_by, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getMessage());
            ps.setObject(3, n.getRecipientId());
            ps.setString(4, n.getRecipientRole().name());
            ps.setString(5, n.getType() != null ? n.getType().name() : "INFO");
            ps.setInt(6, n.isReadStatus() ? 1 : 0);
            ps.setObject(7, n.getCreatedBy());
            ps.setString(8, n.getSource() != null ? n.getSource().name() : "MANUAL");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) n.setId(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur création notification", e);
        }
    }

    @Override
    public void update(Notification n) {
        if (n.getTitle() == null || n.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Le titre est requis");
        if (n.getMessage() == null || n.getMessage().trim().isEmpty())
            throw new IllegalArgumentException("Le message est requis");

        String sql = "UPDATE notifications SET title=?, message=?, recipient_id=?, recipient_role=?, type=?, read_status=? WHERE id=?";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getMessage());
            ps.setObject(3, n.getRecipientId());
            ps.setString(4, n.getRecipientRole().name());
            ps.setString(5, n.getType() != null ? n.getType().name() : "INFO");
            ps.setInt(6, n.isReadStatus() ? 1 : 0);
            ps.setInt(7, n.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour notification", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM notifications WHERE id=?";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression notification", e);
        }
    }

    @Override
    public Notification findById(int id) {
        String sql = "SELECT * FROM notifications WHERE id=?";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche notification", e);
        }
        return null;
    }

    @Override
    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        try (Connection conn = DataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur liste notifications", e);
        }
        return list;
    }

    @Override
    public List<Notification> findByRoleOrUser(Notification.RecipientRole role, Integer userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE recipient_role=? AND (recipient_id IS NULL OR recipient_id=?) ORDER BY created_at DESC";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setObject(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche par rôle/user", e);
        }
        return list;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRecipientId(rs.getObject("recipient_id") != null ? rs.getInt("recipient_id") : null);
        n.setRecipientRole(Notification.RecipientRole.valueOf(rs.getString("recipient_role")));
        n.setType(Notification.NotificationType.valueOf(rs.getString("type")));
        n.setReadStatus(rs.getInt("read_status") == 1);
        Timestamp ts = rs.getTimestamp("created_at");
        n.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        n.setCreatedBy(rs.getObject("created_by") != null ? rs.getInt("created_by") : null);
        n.setSource(Notification.Source.valueOf(rs.getString("source")));
        return n;
    }
}
