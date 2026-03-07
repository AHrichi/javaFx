package Entite;

import java.sql.Date;

public class Club {
    private int idClub;
    private String nom;
    private String adresse;
    private String ville;
    private String telephone;
    private String email;
    private Date dateCreation;
    private String description;
    private String evenements;
    private String photoClub;
    private String photoSalle;
    private int capacite;
    private int placesRestantes;
    private int idAdminResponsable;

    public Club() {
    }

    // Constructeur pour AJOUT
    public Club(String nom, String adresse, String ville, String telephone, String email,
            Date dateCreation, String description, String evenements, String photoClub, String nomSalle,
            int capacite, int idAdminResponsable) {
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.telephone = telephone;
        this.email = email;
        this.dateCreation = dateCreation;
        this.description = description;
        this.evenements = evenements;
        this.photoClub = photoClub;
        this.photoSalle = nomSalle;
        this.capacite = capacite;
        this.placesRestantes = capacite; // Initialement égal à la capacité
        this.idAdminResponsable = idAdminResponsable;
    }

    // Constructeur pour SELECT/UPDATE
    public Club(int idClub, String nom, String adresse, String ville, String telephone, String email,
            Date dateCreation, String description, String evenements, String photoClub, String nomSalle,
            int capacite, int placesRestantes, int idAdminResponsable) {
        this.idClub = idClub;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.telephone = telephone;
        this.email = email;
        this.dateCreation = dateCreation;
        this.description = description;
        this.evenements = evenements;
        this.photoClub = photoClub;
        this.photoSalle = nomSalle;
        this.capacite = capacite;
        this.placesRestantes = placesRestantes;
        this.idAdminResponsable = idAdminResponsable;
    }

    // Getters & Setters
    public int getIdClub() {
        return idClub;
    }

    public void setIdClub(int idClub) {
        this.idClub = idClub;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvenements() {
        return evenements;
    }

    public void setEvenements(String evenements) {
        this.evenements = evenements;
    }

    public String getPhotoClub() {
        return photoClub;
    }

    public void setPhotoClub(String photoClub) {
        this.photoClub = photoClub;
    }

    public String getPhotoSalle() {
        return photoSalle;
    }

    public void setPhotoSalle(String photoSalle) {
        this.photoSalle = photoSalle;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public int getPlacesRestantes() {
        return placesRestantes;
    }

    public void setPlacesRestantes(int placesRestantes) {
        this.placesRestantes = placesRestantes;
    }

    public boolean isComplet() {
        return placesRestantes <= 0;
    }

    public int getIdAdminResponsable() {
        return idAdminResponsable;
    }

    public void setIdAdminResponsable(int idAdminResponsable) {
        this.idAdminResponsable = idAdminResponsable;
    }

    @Override
    public String toString() {
        return "Club{" +
                "idClub=" + idClub +
                ", nom='" + nom + '\'' +
                ", ville='" + ville + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
