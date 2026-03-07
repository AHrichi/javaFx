package Utils;

import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class CreateTestUser {

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String email = "user@sportlink.com";
        String pass = hashPassword("user123");

        String sql = "INSERT INTO user (nom, prenom, email, mot_de_passe, type_user, statut) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "user");
            ps.setString(2, "Test");
            ps.setString(3, email);
            ps.setString(4, pass);
            ps.setString(5, "membre");
            ps.setString(6, "actif");

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Utilisateur de test créé avec succès !");
                System.out.println("Email: " + email);
                System.out.println("Password: user123");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("L'utilisateur existe déjà. Tentative de mise à jour du statut...");
                updateUserStatus(email);
            } else {
                e.printStackTrace();
            }
        }
    }

    private static void updateUserStatus(String email) {
        String sql = "IUPDATE user SET statut = 'actif', type_user = 'Membre', mot_de_passe = ? WHERE email = ?";
        try (Connection con = DataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashPassword("user123"));
            ps.setString(2, email);
            ps.executeUpdate();
            System.out.println(
                    "Utilisateur mis à jour : " + email + " est maintenant actif avec le mot de passe 'user123'.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
