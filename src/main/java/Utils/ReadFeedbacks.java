package Utils;

import java.sql.*;

public class ReadFeedbacks {
    public static void main(String[] args) {
        Connection conn = DataSource.getConnection();
        if (conn == null) {
            System.err.println("Connection is null!");
            return;
        }
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM feedback");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("idFeedback") +
                        ", type: " + rs.getString("typeEntite") +
                        ", cat: " + rs.getString("categorie") +
                        ", note: " + rs.getInt("note"));
            }
        } catch (Exception e) {
            System.err.println("Error reading feedbacks: " + e.getMessage());
        }
    }
}
