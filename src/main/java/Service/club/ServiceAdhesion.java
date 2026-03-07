package Service.club;

import Service.IService;


import Entite.AdhesionClub;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAdhesion {
    private Connection con;

    public ServiceAdhesion() {
        con = DataSource.getConnection();
    }

    public boolean adherer(AdhesionClub adhesion) throws SQLException {
        // 1. Vérifier capacité dans la table club
        String sqlCap = "SELECT capacite, places_restantes FROM club WHERE id_club = ?";
        PreparedStatement psCap = con.prepareStatement(sqlCap);
        psCap.setInt(1, adhesion.getIdClub());
        ResultSet rs = psCap.executeQuery();

        if (rs.next()) {
            int placesRestantes = rs.getInt("places_restantes");
            if (placesRestantes <= 0) {
                return false; // Complet
            }
        } else {
            return false; // Club non trouvé
        }

        // 2. Insérer l'adhésion
        String sql = "INSERT INTO adhesion_club (nom_membre, email, id_club) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, adhesion.getNomMembre());
        ps.setString(2, adhesion.getEmail());
        ps.setInt(3, adhesion.getIdClub());

        if (ps.executeUpdate() > 0) {
            // 3. Décrémenter places_restantes
            String sqlUpdate = "UPDATE club SET places_restantes = places_restantes - 1 WHERE id_club = ?";
            PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
            psUpdate.setInt(1, adhesion.getIdClub());
            psUpdate.executeUpdate();
            return true;
        }
        return false;
    }

    public boolean emailDejaAdherent(String email, int idClub) throws SQLException {
        String sql = "SELECT COUNT(*) FROM adhesion_club WHERE email = ? AND id_club = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ps.setInt(2, idClub);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getInt(1) > 0;
        return false;
    }

    public List<AdhesionClub> afficherParClub(int idClub) throws SQLException {
        List<AdhesionClub> list = new ArrayList<>();
        String sql = "SELECT * FROM adhesion_club WHERE id_club = ? ORDER BY date_adhesion DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idClub);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new AdhesionClub(
                    rs.getInt("id_adhesion"),
                    rs.getString("nom_membre"),
                    rs.getString("email"),
                    rs.getTimestamp("date_adhesion"),
                    rs.getInt("id_club")));
        }
        return list;
    }
}
