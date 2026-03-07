package Utils;

import java.sql.*;

public class ListUsers {
    public static void main(String[] args) {
        String sql = "SELECT email, type_user, statut FROM user";
        try (Connection con = DataSource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            System.out.println("--- Liste des Utilisateurs ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("Email: %s | Type: %s | Statut: %s%n",
                        rs.getString("email"),
                        rs.getString("type_user"),
                        rs.getString("statut"));
            }
            if (!found) {
                System.out.println("Aucun utilisateur trouvé.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }
}
