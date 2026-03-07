package Entite;

import java.sql.Timestamp;

public class AdhesionClub {
    private int idAdhesion;
    private String nomMembre;
    private String email;
    private Timestamp dateAdhesion;
    private int idClub;

    public AdhesionClub() {
    }

    public AdhesionClub(String nomMembre, String email, int idClub) {
        this.nomMembre = nomMembre;
        this.email = email;
        this.idClub = idClub;
    }

    public AdhesionClub(int idAdhesion, String nomMembre, String email, Timestamp dateAdhesion, int idClub) {
        this.idAdhesion = idAdhesion;
        this.nomMembre = nomMembre;
        this.email = email;
        this.dateAdhesion = dateAdhesion;
        this.idClub = idClub;
    }

    public int getIdAdhesion() {
        return idAdhesion;
    }

    public void setIdAdhesion(int idAdhesion) {
        this.idAdhesion = idAdhesion;
    }

    public String getNomMembre() {
        return nomMembre;
    }

    public void setNomMembre(String nomMembre) {
        this.nomMembre = nomMembre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getDateAdhesion() {
        return dateAdhesion;
    }

    public void setDateAdhesion(Timestamp dateAdhesion) {
        this.dateAdhesion = dateAdhesion;
    }

    public int getIdClub() {
        return idClub;
    }

    public void setIdClub(int idClub) {
        this.idClub = idClub;
    }
}
