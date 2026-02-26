package Service.auth;

import Entite.User;
import Utils.DataSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuthService {

    private final Connection connection;

    public AuthService() {
        this.connection = DataSource.getConnection();
    }

    private boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM User WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return false;
    }

    private boolean createUser(User user) {
        String sql = "INSERT INTO User (nom, prenom, email, mot_de_passe, telephone, ville, date_naissance, photo, type_user, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getMotDePasse());
            stmt.setString(5, user.getTelephone());
            stmt.setString(6, user.getVille());
            stmt.setDate(7, user.getDateNaissance() != null ? Date.valueOf(user.getDateNaissance()) : null);
            stmt.setString(8, user.getPhoto());
            stmt.setString(9, user.getTypeUser());
            stmt.setString(10, user.getStatut());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setIdUser(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création: " + e.getMessage());
        }
        return false;
    }

    private User findUserByEmail(String email) {
        String sql = "SELECT * FROM User WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
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
                Date d = rs.getDate("date_naissance");
                if (d != null)
                    user.setDateNaissance(d.toLocalDate());
                user.setPhoto(rs.getString("photo"));
                user.setTypeUser(rs.getString("type_user"));
                user.setStatut(rs.getString("statut"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }
        return null;
    }

    /**
     * Register a new user.
     * 
     * @return null on success, error message on failure
     */
    public String register(String nom, String prenom, String email, String password, String typeUser) {
        if (nom == null || nom.trim().isEmpty() ||
                prenom == null || prenom.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return "Tous les champs obligatoires doivent être remplis.";
        }
        if (password.length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caractères.";
        }
        if (!typeUser.equals("Coach") && !typeUser.equals("Membre")) {
            return "Type d'utilisateur invalide.";
        }
        if (emailExists(email)) {
            return "Cet email est déjà utilisé.";
        }

        User newUser = new User();
        newUser.setNom(nom);
        newUser.setPrenom(prenom);
        newUser.setEmail(email);
        newUser.setMotDePasse(hashPassword(password));
        newUser.setTypeUser(typeUser);
        newUser.setStatut("en attente");

        if (createUser(newUser)) {
            return null; // success
        }
        return "Erreur lors de l'inscription.";
    }

    /**
     * Login a user.
     * 
     * @return the User on success, null on failure (error message in
     *         getLastError())
     */
    public User login(String email, String password) {
        lastError = null;
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            lastError = "Email et mot de passe requis.";
            return null;
        }
        User user = findUserByEmail(email);
        if (user == null) {
            lastError = "Utilisateur non trouvé.";
            return null;
        }
        if (!verifyPassword(password, user.getMotDePasse())) {
            lastError = "Mot de passe incorrect.";
            return null;
        }
        if ("inactif".equals(user.getStatut())) {
            lastError = "Compte désactivé.";
            return null;
        }
        if ("en attente".equals(user.getStatut())) {
            lastError = "Compte en attente de validation.";
            return null;
        }
        return user;
    }

    private String lastError;

    public String getLastError() {
        return lastError;
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
