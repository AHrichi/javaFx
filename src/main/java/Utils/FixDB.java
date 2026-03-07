package Utils;

import java.sql.*;

public class FixDB {
    public static void main(String[] args) {
        Connection conn = DataSource.getConnection();
        if (conn == null) {
            System.err.println("Connection is null!");
            return;
        }
        System.out.println("Connection OK. Attempting to add column.");
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("ALTER TABLE feedback ADD COLUMN categorie VARCHAR(50) DEFAULT 'Commentaire'");
            System.out.println("Column added successfully!");
        } catch (Exception e) {
            System.err.println("Failed to add column: " + e.getMessage());
        }

        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM feedback LIMIT 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("Columns in feedback table:");
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.println("- " + rsmd.getColumnName(i));
            }
        } catch (Exception e) {
            System.err.println("Error reading columns: " + e.getMessage());
        }
    }
}
