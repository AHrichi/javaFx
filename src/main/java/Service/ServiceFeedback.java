package Service;

import Entite.Feedback;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFeedback {
    private Connection conn;

    public ServiceFeedback() {
        this.conn = DataSource.getConnection();
        setupTable();
    }

    private void setupTable() {
        String sql = "CREATE TABLE IF NOT EXISTS feedback ("
                + "idFeedback INT AUTO_INCREMENT PRIMARY KEY,"
                + "idUser INT NOT NULL,"
                + "idEntite INT NOT NULL,"
                + "typeEntite ENUM('CLUB', 'EVENEMENT') NOT NULL,"
                + "commentaire TEXT,"
                + "categorie VARCHAR(50) DEFAULT 'Commentaire',"
                + "note INT DEFAULT 5,"
                + "dateFeedback TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Attempt to add the column if the table was created previously without it
            try {
                stmt.execute("ALTER TABLE feedback ADD COLUMN categorie VARCHAR(50) DEFAULT 'Commentaire'");
            } catch (SQLException ignore) {
                // Column likely already exists, ignore
            }
        } catch (SQLException e) {
            System.err.println("Error setting up feedback table: " + e.getMessage());
        }
    }

    public boolean ajouter(Feedback f) {
        String sql = "INSERT INTO feedback (idUser, idEntite, typeEntite, commentaire, categorie, note, dateFeedback) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, f.getIdUser());
            pstmt.setInt(2, f.getIdEntite());
            pstmt.setString(3, f.getTypeEntite());
            pstmt.setString(4, f.getCommentaire());
            pstmt.setString(5, f.getCategorie());
            pstmt.setInt(6, f.getNote());
            pstmt.setTimestamp(7, Timestamp.valueOf(f.getDateFeedback()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Feedback> readAllByEntite(int idEntite, String typeEntite) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, COALESCE(u.prenom, 'Inconnu') as prenom, COALESCE(u.nom, '') as nom "
                + "FROM feedback f "
                + "LEFT JOIN user u ON f.idUser = u.id_user "
                + "WHERE f.idEntite = ? AND f.typeEntite = ? "
                + "ORDER BY f.dateFeedback DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEntite);
            pstmt.setString(2, typeEntite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Feedback f = mapResultSet(rs);
                f.setUserName(rs.getString("prenom") + " " + rs.getString("nom"));
                list.add(f);
            }
        } catch (Exception e) {
            System.err.println("Error in readAllByEntite: " + e.getMessage());
        }
        return list;
    }

    public List<Feedback> readAll() {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, COALESCE(u.prenom, 'Inconnu') as prenom, COALESCE(u.nom, '') as nom "
                + "FROM feedback f "
                + "LEFT JOIN user u ON f.idUser = u.id_user "
                + "ORDER BY f.dateFeedback DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Feedback f = mapResultSet(rs);
                f.setUserName(rs.getString("prenom") + " " + rs.getString("nom"));
                list.add(f);
            }
        } catch (Exception e) {
            System.err.println("Error in readAll: " + e.getMessage());
        }
        return list;
    }

    public List<Feedback> readAllByType(String typeEntite) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, COALESCE(u.prenom, 'Inconnu') as prenom, COALESCE(u.nom, '') as nom "
                + "FROM feedback f "
                + "LEFT JOIN user u ON f.idUser = u.id_user "
                + "WHERE f.typeEntite = ? "
                + "ORDER BY f.dateFeedback DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, typeEntite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Feedback f = mapResultSet(rs);
                f.setUserName(rs.getString("prenom") + " " + rs.getString("nom"));
                list.add(f);
            }
        } catch (Exception e) {
            System.err.println("Error in readAllByType: " + e.getMessage());
        }
        return list;
    }

    public boolean supprimer(int idFeedback) {
        String sql = "DELETE FROM feedback WHERE idFeedback = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFeedback);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error in supprimer: " + e.getMessage());
            return false;
        }
    }

    private Feedback mapResultSet(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setIdFeedback(rs.getInt("idFeedback"));
        f.setIdUser(rs.getInt("idUser"));
        f.setIdEntite(rs.getInt("idEntite"));
        f.setTypeEntite(rs.getString("typeEntite"));
        f.setCommentaire(rs.getString("commentaire"));
        String cat = rs.getString("categorie");
        f.setCategorie(cat != null ? cat : "Commentaire");
        f.setNote(rs.getInt("note"));
        Timestamp ts = rs.getTimestamp("dateFeedback");
        f.setDateFeedback(ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now());
        return f;
    }
}
