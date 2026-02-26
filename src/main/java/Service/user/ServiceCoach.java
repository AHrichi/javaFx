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
    public boolean ajouter(Coach coach) throws SQLException { // Changed parameter type
        boolean test = false;

        // 1️⃣ Insertion into User
        String reqUser = "INSERT INTO User (nom, prenom, email, mot_de_passe, type_user, statut) VALUES (" +
                "'" + coach.getNom() + "'," +
                "'" + coach.getPrenom() + "'," +
                "'" + coach.getEmail() + "'," +
                "'" + coach.getMotDePasse() + "'," +
                "'Coach'," +
                "'" + coach.getStatut() + "')";

        // Use RETURN_GENERATED_KEYS to get the new ID
        int resUser = st.executeUpdate(reqUser, Statement.RETURN_GENERATED_KEYS);

        if (resUser > 0) {
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                int idUser = rs.getInt(1);

                // 2️⃣ Insertion into Coach using the retrieved ID
                String reqCoach = "INSERT INTO Coach (id_coach, specialite, experience_annees, certification, biographie, photo_certification) VALUES (" +
                        idUser + "," +
                        "'" + coach.getSpecialite() + "'," +
                        coach.getExperienceAnnees() + "," +
                        "'" + coach.getCertification() + "'," +
                        "'" + coach.getBiographie() + "'," +
                        "'" + coach.getPhotoCertification() + "')";

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

        String query = "SELECT * FROM User u JOIN Coach c ON u.id_user = c.id_coach";
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            // Reconstruct the Coach object
            // Note: You need a constructor in Coach that accepts these values or use setters
            Coach coach = new Coach();
            coach.setIdUser(rs.getInt("id_user"));
            coach.setNom(rs.getString("nom"));
            coach.setPrenom(rs.getString("prenom"));
            coach.setEmail(rs.getString("email"));
            coach.setStatut(rs.getString("statut"));
            // ... set other user fields ...

            coach.setSpecialite(rs.getString("specialite"));
            coach.setExperienceAnnees(rs.getInt("experience_annees"));
            coach.setCertification(rs.getString("certification"));
            coach.setBiographie(rs.getString("biographie"));
            coach.setPhotoCertification(rs.getString("photo_certification"));

            list.add(coach);
        }
        return list;
    }

    @Override
    public boolean supprimer(Coach coach) throws SQLException {
        String req = "DELETE FROM User WHERE id_user = " + coach.getIdUser();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean modifier(Coach coach) throws SQLException {
        return false; // Implement update logic here
    }

    @Override
    public Coach findbyId(int id) throws SQLException {
        return null; // Implement find logic here
    }
}