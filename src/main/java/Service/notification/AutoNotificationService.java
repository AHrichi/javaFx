package Service.notification;

import Entite.Notification;
import Entite.Notification.RecipientRole;
import Entite.Notification.NotificationType;
import Entite.Notification.Source;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles automatic notification creation for system events.
 */
public class AutoNotificationService {

    private static final NotificationService notifService = new NotificationService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ═══════════════════════════════════════════════════════
    // #1/#4/#9 — Session Reminders (startup — Members, Coaches, Admin)
    // ═══════════════════════════════════════════════════════

    public static void notifySessionReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime in24h = now.plusHours(24);

            // 1. Collect all upcoming session data into memory FIRST
            //    (avoids "ResultSet closed" when inner queries reuse the shared connection)
            List<int[]> sessions = new ArrayList<>();       // [seanceId, coachId]
            List<String> titres = new ArrayList<>();
            List<LocalDateTime> dates = new ArrayList<>();

            String sql = "SELECT s.id_seance, s.titre, s.date_debut, s.id_coach " +
                         "FROM seance s WHERE s.date_debut BETWEEN ? AND ? AND s.statut='planifiée'";

            PreparedStatement ps = DataSource.getConnection().prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(now));
            ps.setTimestamp(2, Timestamp.valueOf(in24h));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sessions.add(new int[]{ rs.getInt("id_seance"), rs.getInt("id_coach") });
                titres.add(rs.getString("titre"));
                dates.add(rs.getTimestamp("date_debut").toLocalDateTime());
            }
            rs.close();
            ps.close();

            // 2. Now process each session (safe to make inner DB calls)
            for (int i = 0; i < sessions.size(); i++) {
                int seanceId = sessions.get(i)[0];
                int coachId  = sessions.get(i)[1];
                String titre = titres.get(i);
                LocalDateTime dateDebut = dates.get(i);

                String timeStr = dateDebut.format(TIME_FMT);
                String dateStr = dateDebut.format(DATE_FMT);

                // #1 — Notify each participant (MEMBER)
                List<Integer> participants = getParticipantIds(seanceId);
                for (int membreId : participants) {
                    createIfNotExists(
                        "Rappel : " + titre,
                        "Votre séance '" + titre + "' commence le " + dateStr + " à " + timeStr + ". Préparez-vous !",
                        RecipientRole.MEMBER, membreId, NotificationType.SESSION);
                }

                // #4 — Notify Coach
                createIfNotExists(
                    "Rappel : " + titre,
                    "Votre séance '" + titre + "' est prévue le " + dateStr + " à " + timeStr + ".",
                    RecipientRole.COACH, coachId, NotificationType.SESSION);

                // #9 — Notify all admins
                createIfNotExists(
                    "Séance à venir : " + titre,
                    "La séance '" + titre + "' est prévue le " + dateStr + " à " + timeStr + ".",
                    RecipientRole.ADMIN, null, NotificationType.SESSION);
            }
        } catch (Exception e) {
            System.err.println("AutoNotif — session reminders error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // #2 — Participation Confirmed (Member)
    // ═══════════════════════════════════════════════════════

    public static void notifyParticipationConfirmed(int membreId, int seanceId, String seanceTitre) {
        try {
            Notification n = buildAuto(
                    "Inscription confirmée",
                    "Vous êtes inscrit(e) à la séance '" + seanceTitre + "'. À bientôt !",
                    RecipientRole.MEMBER, membreId, NotificationType.SESSION);
            notifService.create(n);
        } catch (Exception e) {
            System.err.println("AutoNotif — participation confirmed error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // #5 — New Participant Joined (Coach)
    // ═══════════════════════════════════════════════════════

    public static void notifyCoachNewParticipant(int coachId, String seanceTitre, String membreName) {
        try {
            Notification n = buildAuto(
                    "Nouveau participant",
                    membreName + " s'est inscrit(e) à votre séance '" + seanceTitre + "'.",
                    RecipientRole.COACH, coachId, NotificationType.INFO);
            notifService.create(n);
        } catch (Exception e) {
            System.err.println("AutoNotif — Coach new participant error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // #8 — Session Full (Admin)
    // ═══════════════════════════════════════════════════════

    public static void notifySessionFull(String seanceTitre) {
        try {
            Notification n = buildAuto(
                    "Séance complète",
                    "La séance '" + seanceTitre + "' a atteint sa capacité maximale.",
                    RecipientRole.ADMIN, null, NotificationType.INFO);
            notifService.create(n);
        } catch (Exception e) {
            System.err.println("AutoNotif — session full error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // #3/#6 — Account Approved (Member or Coach)
    // ═══════════════════════════════════════════════════════

    public static void notifyAccountApproved(int userId, String userType) {
        try {
            RecipientRole role = "Coach".equalsIgnoreCase(userType) ? RecipientRole.COACH : RecipientRole.MEMBER;
            Notification n = buildAuto(
                    "Compte activé !",
                    "Bienvenue ! Votre compte a été activé. Vous pouvez maintenant accéder à toutes les fonctionnalités de SportLink.",
                    role, userId, NotificationType.SYSTEM);
            notifService.create(n);
        } catch (Exception e) {
            System.err.println("AutoNotif — account approved error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // #7 — New User Registration (Admin)
    // ═══════════════════════════════════════════════════════

    public static void notifyNewRegistration(String userName, String userType) {
        try {
            Notification n = buildAuto(
                    "Nouvelle inscription",
                    userName + " (" + userType + ") vient de s'inscrire et attend votre approbation.",
                    RecipientRole.ADMIN, null, NotificationType.ALERT);
            notifService.create(n);
        } catch (Exception e) {
            System.err.println("AutoNotif — new registration error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════

    private static Notification buildAuto(String title, String message,
            RecipientRole role, Integer recipientId,
            NotificationType type) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setRecipientRole(role);
        n.setRecipientId(recipientId);
        n.setType(type);
        n.setReadStatus(false);
        n.setSource(Source.AUTO);
        return n;
    }

    /**
     * Creates a notification only if one with the same title+recipientId doesn't
     * exist today.
     */
    private static void createIfNotExists(String title, String message,
            RecipientRole role, Integer recipientId,
            NotificationType type) {
        try {
            String sql = "SELECT COUNT(*) FROM notifications WHERE title=? AND recipient_role=? " +
                    "AND (recipient_id=? OR (? IS NULL AND recipient_id IS NULL)) " +
                    "AND DATE(created_at)=CURDATE()";
            try (Connection conn = DataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, title);
                ps.setString(2, role.name());
                ps.setObject(3, recipientId);
                ps.setObject(4, recipientId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0)
                    return; // already exists today
            }
            notifService.create(buildAuto(title, message, role, recipientId, type));
        } catch (Exception e) {
            System.err.println("AutoNotif — createIfNotExists error: " + e.getMessage());
        }
    }

    /** Gets all Member IDs who participate in a given session. */
    private static List<Integer> getParticipantIds(int seanceId) {
        List<Integer> ids = new ArrayList<>();
        try {
            String sql = "SELECT id_membre FROM participation WHERE id_seance=?";
            try (Connection conn = DataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, seanceId);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    ids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("AutoNotif — getParticipantIds error: " + e.getMessage());
        }
        return ids;
    }
}
