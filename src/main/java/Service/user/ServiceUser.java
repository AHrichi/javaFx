package Service.user;

import Service.IService;
import Entite.User;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    private Connection con;

    public ServiceUser() {
        con = DataSource.getConnection();
    }

    @Override
    public boolean ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, email, mot_de_passe, type_user, statut) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getMotDePasse());
        ps.setString(5, user.getTypeUser());
        ps.setString(6, user.getStatut());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(User user) throws SQLException {
        String sql = "DELETE FROM user WHERE id_user = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, user.getIdUser());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(User user) throws SQLException {
        String sql = "IUPDATE user SET nom = ?, prenom = ?, email = ? WHERE id_user = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setInt(4, user.getIdUser());
        return ps.executeUpdate() > 0;
    }

    @Override
    public User findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id_user = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new User(
                    rs.getInt("id_user"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"));
        }
        return null;
    }

    @Override
    public List<User> readAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY nom ASC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new User(
                    rs.getInt("id_user"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email")));
        }
        return list;
    }
}
