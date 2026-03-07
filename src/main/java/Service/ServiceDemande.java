package Service;

import Utils.DataSource;
import Entite.DemandeAdhesion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère les demandes d'adhésion (clubs et événements).
 * Crée automatiquement la table `demande_adhesion` si elle n'existe pas.
 */
public class ServiceDemande {

    private Connection con;

    public ServiceDemande() {
        con = DataSource.getConnection();
        creerTableSiAbsente();
    }

    private void creerTableSiAbsente() {
        String sql = "CREATE TABLE IF NOT EXISTS demande_adhesion (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "type_entite VARCHAR(20) NOT NULL COMMENT 'CLUB ou EVENEMENT'," +
                "id_entite INT NOT NULL," +
                "nom_entite VARCHAR(255) NOT NULL," +
                "id_user INT NOT NULL," +
                "nom_user VARCHAR(255) NOT NULL," +
                "email_user VARCHAR(255) NOT NULL," +
                "statut VARCHAR(20) DEFAULT 'EN_ATTENTE'," +
                "date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement st = con.createStatement()) {
            st.execute(sql);

            // Try adding columns if table exists without them (migration)
            try {
                st.execute("ALTER TABLE demande_adhesion ADD COLUMN id_user INT NOT NULL AFTER nom_entite");
            } catch (Exception ignored) {
            }
            try {
                st.execute("ALTER TABLE demande_adhesion ADD COLUMN nom_user VARCHAR(255) NOT NULL AFTER id_user");
            } catch (Exception ignored) {
            }
            try {
                st.execute("ALTER TABLE demande_adhesion ADD COLUMN email_user VARCHAR(255) NOT NULL AFTER nom_user");
            } catch (Exception ignored) {
            }
        } catch (SQLException e) {
            System.err.println("Erreur création table demande_adhesion: " + e.getMessage());
        }
    }

    public boolean demanderRejoindreClub(int idClub, String nomClub, int idUser, String nomUser, String emailUser) {
        String sql = "INSERT INTO demande_adhesion (type_entite, id_entite, nom_entite, id_user, nom_user, email_user, statut) VALUES ('CLUB', ?, ?, ?, ?, ?, 'EN_ATTENTE')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idClub);
            ps.setString(2, nomClub);
            ps.setInt(3, idUser);
            ps.setString(4, nomUser);
            ps.setString(5, emailUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur demande club: " + e.getMessage());
            return false;
        }
    }

    public boolean demanderRejoindreEvenement(int idEvenement, String nomEvenement, int idUser, String nomUser,
            String emailUser) {
        String sql = "INSERT INTO demande_adhesion (type_entite, id_entite, nom_entite, id_user, nom_user, email_user, statut) VALUES ('EVENEMENT', ?, ?, ?, ?, ?, 'EN_ATTENTE')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEvenement);
            ps.setString(2, nomEvenement);
            ps.setInt(3, idUser);
            ps.setString(4, nomUser);
            ps.setString(5, emailUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur demande événement: " + e.getMessage());
            return false;
        }
    }

    public List<DemandeAdhesion> readAllByType(String type, String statut) throws SQLException {
        List<DemandeAdhesion> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_adhesion WHERE type_entite = ? AND statut = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DemandeAdhesion(
                        rs.getInt("id"),
                        rs.getString("type_entite"),
                        rs.getInt("id_entite"),
                        rs.getString("nom_entite"),
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("email_user"),
                        rs.getString("statut"),
                        rs.getString("date_demande")));
            }
        }
        return list;
    }

    public boolean aDejaDemande(String type, int idEntite, int idUser) {
        String sql = "SELECT COUNT(*) FROM demande_adhesion WHERE type_entite = ? AND id_entite = ? AND id_user = ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, idEntite);
            ps.setInt(3, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur check duplication: " + e.getMessage());
        }
        return false;
    }

    public boolean approuverDemande(int id) throws SQLException {
        String sql = "UPDATE demande_adhesion SET statut = 'APPROUVEE' WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean refuserDemande(int id) throws SQLException {
        String sql = "UPDATE demande_adhesion SET statut = 'REFUSEE' WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public String getStatutDemande(String type, int idEntite, int idUser) {
        String sql = "SELECT statut FROM demande_adhesion WHERE type_entite = ? AND id_entite = ? AND id_user = ? ORDER BY date_demande DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, idEntite);
            ps.setInt(3, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("statut");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération statut: " + e.getMessage());
        }
        return null;
    }
}
