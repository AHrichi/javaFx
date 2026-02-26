package Service.auth;

import Entite.Admin;
import Entite.User;
import Utils.DataSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final Connection connection;

    public AdminService() {
        this.connection = DataSource.getConnection();
    }

    // ===================== LOGIN =====================

    private String lastError;

    public String getLastError() {
        return lastError;
    }

    public Admin loginAdmin(String email, String password) {
        lastError = null;
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            lastError = "Email et mot de passe requis.";
            return null;
        }
        Admin admin = findAdminByEmail(email);
        if (admin == null) {
            lastError = "Administrateur non trouvé.";
            return null;
        }
        if (!verifyPassword(password, admin.getMotDePasse())) {
            lastError = "Mot de passe incorrect.";
            return null;
        }
        if ("inactif".equals(admin.getStatut())) {
            lastError = "Compte administrateur désactivé.";
            return null;
        }
        return admin;
    }

    // ===================== USER MANAGEMENT =====================

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM User";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int userId) {
        return updateUserStatut(userId, "actif");
    }

    public boolean deactivateUser(int userId) {
        return updateUserStatut(userId, "inactif");
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM User WHERE id_user = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression user: " + e.getMessage());
        }
        return false;
    }

    // ===================== ADMIN MANAGEMENT =====================

    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admin";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                admins.add(mapAdmin(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération admins: " + e.getMessage());
        }
        return admins;
    }

    public String addAdmin(String nom, String prenom, String email, String password, String telephone) {
        if (nom == null || nom.trim().isEmpty() ||
                prenom == null || prenom.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return "Tous les champs obligatoires doivent être remplis.";
        }
        if (password.length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caractères.";
        }
        if (adminEmailExists(email)) {
            return "Cet email est déjà utilisé.";
        }

        String sql = "INSERT INTO admin (nom, prenom, email, mot_de_passe, telephone, statut) VALUES (?, ?, ?, ?, ?, 'actif')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, email);
            stmt.setString(4, hashPassword(password));
            stmt.setString(5, telephone);
            if (stmt.executeUpdate() > 0) {
                return null; // success
            }
        } catch (SQLException e) {
            System.err.println("Erreur création admin: " + e.getMessage());
        }
        return "Erreur lors de la création de l'admin.";
    }

    public boolean deleteAdmin(int adminId) {
        String sql = "DELETE FROM admin WHERE id_admin = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression admin: " + e.getMessage());
        }
        return false;
    }

    // ===================== HELPERS =====================

    private Admin findAdminByEmail(String email) {
        String sql = "SELECT * FROM admin WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapAdmin(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche admin: " + e.getMessage());
        }
        return null;
    }

    private boolean adminEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM admin WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return false;
    }

    private boolean updateUserStatut(int userId, String statut) {
        String sql = "UPDATE User SET statut = ? WHERE id_user = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statut: " + e.getMessage());
        }
        return false;
    }

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setIdAdmin(rs.getInt("id_admin"));
        admin.setNom(rs.getString("nom"));
        admin.setPrenom(rs.getString("prenom"));
        admin.setEmail(rs.getString("email"));
        admin.setMotDePasse(rs.getString("mot_de_passe"));
        admin.setTelephone(rs.getString("telephone"));
        admin.setStatut(rs.getString("statut"));
        return admin;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null)
            user.setDateInscription(ts.toLocalDateTime());
        user.setTelephone(rs.getString("telephone"));
        user.setVille(rs.getString("ville"));
        java.sql.Date d = rs.getDate("date_naissance");
        if (d != null)
            user.setDateNaissance(d.toLocalDate());
        user.setPhoto(rs.getString("photo"));
        user.setTypeUser(rs.getString("type_user"));
        user.setStatut(rs.getString("statut"));
        return user;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }
}
