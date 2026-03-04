package Service.club;

import Entite.Club;
import Service.IService;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceClub implements IService<Club> {

    // Récupération dynamique de la connexion (comme dans ServiceSeance)
    private Connection getConnect() {
        return DataSource.getConnection();
    }

    @Override
    public boolean ajouter(Club club) throws SQLException { return false; }

    @Override
    public boolean modifier(Club club) throws SQLException { return false; }

    @Override
    public boolean supprimer(Club club) throws SQLException { return false; }

    @Override
    public List<Club> readAll() throws SQLException {
        List<Club> clubs = new ArrayList<>();
        String req = "SELECT * FROM club";

        // Création locale du Statement pour éviter les erreurs de connexion
        Statement st = getConnect().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Club c = new Club();
            c.setIdClub(rs.getInt("id_club"));
            c.setNom(rs.getString("nom"));
            c.setAdresse(rs.getString("adresse"));
            c.setVille(rs.getString("ville"));
            clubs.add(c);
        }

        return clubs;
    }

    @Override
    public Club findbyId(int id) throws SQLException {
        String req = "SELECT * FROM club WHERE id_club = ?";
        PreparedStatement ps = getConnect().prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Club c = new Club();
            c.setIdClub(rs.getInt("id_club"));
            c.setNom(rs.getString("nom"));
            return c;
        }
        return null;
    }
}