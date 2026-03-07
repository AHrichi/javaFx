package Utils;

import Service.auth.AuthService;
import java.sql.*;

public class ForceCreateUser {
    public static void main(String[] args) {
        AuthService auth = new AuthService();
        String res = auth.register("Test", "User", "test@sportlink.com", "test123456", "membre");

        if (res == null) {
            System.out.println("SUCCESS: User test@sportlink.com created with password test123456");
            // Force status to actif
            try (Connection con = DataSource.getConnection();
                    PreparedStatement ps = con
                            .prepareStatement("IUPDATE user SET statut = 'actif' WHERE email = 'test@sportlink.com'")) {
                ps.executeUpdate();
                System.out.println("Status forced to 'actif'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("RESULT: " + res);
            if (res.contains("déjà utilisé")) {
                // Force update password and status
                try (Connection con = DataSource.getConnection()) {
                    // Need to hash manually since register failed
                    java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest("test123456".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    StringBuilder hexString = new StringBuilder();
                    for (byte b : hash) {
                        String hex = Integer.toHexString(0xff & b);
                        if (hex.length() == 1)
                            hexString.append('0');
                        hexString.append(hex);
                    }
                    String hashed = hexString.toString();

                    PreparedStatement ps = con.prepareStatement(
                            "IUPDATE user SET mot_de_passe = ?, statut = 'actif', type_user = 'Membre' WHERE email = 'test@sportlink.com'");
                    ps.setString(1, hashed);
                    ps.executeUpdate();
                    System.out.println("SUCCESS: Password and status updated for test@sportlink.com (test123456)");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
