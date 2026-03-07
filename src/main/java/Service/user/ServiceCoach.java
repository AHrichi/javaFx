package Service.user;

import Service.IService;

import Entite.Coach;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCoach implements IService<Coach> { // Changed Object to Coach

    private Connection getConnect() { return DataSource.getConnection(); }
    private Statement st;

    public ServiceCoach() {
        try {
            st = getConnect().createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Coach Coach) throws SQLException { // Changed parameter type
        boolean test = false;

        // 1️⃣ Insertion into User
        String reqUser = "INSERT INTO user (nom, prenom, email, mot_de_passe, type_user, statut) VALUES (" +
                "'" + Coach.getNom() + "'," +
                "'" + Coach.getPrenom() + "'," +
                "'" + Coach.getEmail() + "'," +
                "'" + Coach.getMotDePasse() + "'," +
                "'Coach'," +
                "'" + Coach.getStatut() + "')";

        // Use RETURN_GENERATED_KEYS to get the new ID
        int resUser = st.executeUpdate(reqUser, Statement.RETURN_GENERATED_KEYS);

        if (resUser > 0) {
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                int idUser = rs.getInt(1);

                // 2️⃣ Insertion into Coach using the retrieved ID
                String reqCoach = "INSERT INTO coach (id_coach, specialite, experience_annees, certification, biographie, photo_certification) VALUES (" +
                        idUser + "," +
                        "'" + Coach.getSpecialite() + "'," +
                        Coach.getExperienceAnnees() + "," +
                        "'" + Coach.getCertification() + "'," +
                        "'" + Coach.getBiographie() + "'," +
                        "'" + Coach.getPhotoCertification() + "')";

                int resCoach = st.executeUpdate(reqCoach);
                if (resCoach > 0)
                    test = true;
            }
        }
        return test;
    }

    @Override
    public List<Coach> readAll() throws SQLException { // Changed return type
        List<Coach> list = new ArrayList<>();

        String query = "SELECT * FROM user u JOIN coach c ON u.id_user = c.id_coach";
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            // Reconstruct the Coach object
            // Note: You need a constructor in Coach that accepts these values or use setters
            Coach Coach = new Coach();
            Coach.setIdUser(rs.getInt("id_user"));
            Coach.setNom(rs.getString("nom"));
            Coach.setPrenom(rs.getString("prenom"));
            Coach.setEmail(rs.getString("email"));
            Coach.setStatut(rs.getString("statut"));
            // ... set other user fields ...

            Coach.setSpecialite(rs.getString("specialite"));
            Coach.setExperienceAnnees(rs.getInt("experience_annees"));
            Coach.setCertification(rs.getString("certification"));
            Coach.setBiographie(rs.getString("biographie"));
            Coach.setPhotoCertification(rs.getString("photo_certification"));

            list.add(Coach);
        }
        return list;
    }

    @Override
    public boolean supprimer(Coach Coach) throws SQLException {
        String req = "DELETE FROM user WHERE id_user = " + Coach.getIdUser();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean modifier(Coach Coach) throws SQLException {
        return false; // Implement update logic here
    }

    @Override
    public Coach findbyId(int id) throws SQLException {
        return null; // Implement find logic here
    }
}