package Service.user;

import Service.IService;

import Entite.Membre;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMembre implements IService<Membre> {

    private Connection getConnect() { return DataSource.getConnection(); }

    private Statement st;

    public ServiceMembre() {
        try {
            st = getConnect().createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Membre membre) throws SQLException {
        boolean test = false;

        // 1️⃣ User
        String reqUser =
                "INSERT INTO User (nom, prenom, email, mot_de_passe, type_user, statut) VALUES (" +
                        "'" + membre.getNom() + "'," +
                        "'" + membre.getPrenom() + "'," +
                        "'" + membre.getEmail() + "'," +
                        "'" + membre.getMotDePasse() + "'," +
                        "'Membre'," +
                        "'" + membre.getStatut() + "')";

        int resUser = st.executeUpdate(reqUser, Statement.RETURN_GENERATED_KEYS);

        if (resUser > 0) {
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                int idUser = rs.getInt(1);

                // 2️⃣ Membre
                String reqMembre =
                        "INSERT INTO Membre (id_membre, taille_cm, poids_kg, objectif_sportif, photo_progression) VALUES (" +
                                idUser + "," +
                                membre.getTailleCm() + "," +
                                membre.getPoidsKg() + "," +
                                "'" + membre.getObjectifSportif() + "'," +
                                "'" + membre.getPhotoProgression() + "')";

                int resMembre = st.executeUpdate(reqMembre);
                if (resMembre > 0)
                    test = true;
            }
        }
        return test;
    }

    @Override
    public List<Membre> readAll() throws SQLException {
        List<Membre> list = new ArrayList<>();

        String query =
                "SELECT * FROM User u JOIN Membre m ON u.id_user = m.id_membre";

        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Membre membre = new Membre();
            membre.setIdUser(rs.getInt("id_user"));
            membre.setNom(rs.getString("nom"));
            membre.setPrenom(rs.getString("prenom"));
            membre.setEmail(rs.getString("email"));
            membre.setStatut(rs.getString("statut"));
            membre.setTailleCm(rs.getInt("taille_cm"));
            membre.setPoidsKg(rs.getDouble("poids_kg"));
            membre.setObjectifSportif(rs.getString("objectif_sportif"));
            membre.setPhotoProgression(rs.getString("photo_progression"));

            list.add(membre);
        }
        return list;
    }

    @Override
    public boolean supprimer(Membre membre) throws SQLException {
        String req = "DELETE FROM User WHERE id_user = " + membre.getIdUser();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean modifier(Membre membre) throws SQLException {
        return false;
    }

    @Override
    public Membre findbyId(int id) throws SQLException {
        return null;
    }
}
