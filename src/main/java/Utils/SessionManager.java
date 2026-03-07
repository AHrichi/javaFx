package Utils;

import Entite.Admin;
import Entite.Notification;
import Entite.User;

/**
 * Simple session manager to hold the currently logged-in user or admin.
 */
public class SessionManager {

    private static User currentUser;
    private static Admin currentAdmin;

    public static void setCurrentUser(User user) {
        currentUser = user;
        currentAdmin = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
        currentUser = null;
    }

    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public static boolean isLoggedIn() {
        return currentUser != null || currentAdmin != null;
    }

    /**
     * Returns "admin", "Coach", or "membre".
     */
    public static String getRole() {
        if (currentAdmin != null)
            return "admin";
        if (currentUser != null)
            return currentUser.getTypeUser();
        return null;
    }

    /**
     * Returns the display name for whichever entity is logged in.
     */
    public static String getDisplayName() {
        if (currentAdmin != null)
            return currentAdmin.getNomComplet();
        if (currentUser != null)
            return currentUser.getPrenom() + " " + currentUser.getNom();
        return "";
    }

    /**
     * Returns the photo path (only available for User, Admin has no photo).
     */
    public static String getPhotoPath() {
        if (currentUser != null)
            return currentUser.getPhoto();
        return null;
    }

    /**
     * Returns the current user's or admin's database ID.
     */
    public static Integer getUserId() {
        if (currentAdmin != null)
            return currentAdmin.getIdAdmin();
        if (currentUser != null)
            return currentUser.getIdUser();
        return null;
    }

    /**
     * Maps the session role to the Notification.RecipientRole enum.
     */
    public static Notification.RecipientRole getRecipientRole() {
        String role = getRole();
        if (role == null)
            return null;
        switch (role.toLowerCase()) {
            case "admin":
                return Notification.RecipientRole.ADMIN;
            case "coach":
                return Notification.RecipientRole.COACH;
            case "membre":
                return Notification.RecipientRole.MEMBER;
            default:
                return null;
        }
    }

    public static void logout() {
        currentUser = null;
        currentAdmin = null;
    }
}
