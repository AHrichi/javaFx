package Entite;

import java.sql.Date;

public class Evenement {
    private int idEvenement;
    private String nom;
    private String description;
    private Date dateEvenement;
    private int capaciteMax;
    private double prix;
    private int idClub;
    private String image;

    // Transient : non stocké en BD, calculé à la volée
    private int nbInscriptions;

    public Evenement() {
    }

    // Constructeur insertion
    public Evenement(String nom, String description, Date dateEvenement,
            int capaciteMax, double prix, int idClub, String image) {
        this.nom = nom;
        this.description = description;
        this.dateEvenement = dateEvenement;
        this.capaciteMax = capaciteMax;
        this.prix = prix;
        this.idClub = idClub;
        this.image = image;
    }

    // Constructeur complet (SELECT)
    public Evenement(int idEvenement, String nom, String description, Date dateEvenement,
            int capaciteMax, double prix, int idClub, String image) {
        this.idEvenement = idEvenement;
        this.nom = nom;
        this.description = description;
        this.dateEvenement = dateEvenement;
        this.capaciteMax = capaciteMax;
        this.prix = prix;
        this.idClub = idClub;
        this.image = image;
    }

    // ===== Getters & Setters =====

    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(Date dateEvenement) {
        this.dateEvenement = dateEvenement;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getIdClub() {
        return idClub;
    }

    public void setIdClub(int idClub) {
        this.idClub = idClub;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNbInscriptions() {
        return nbInscriptions;
    }

    public void setNbInscriptions(int nbInscriptions) {
        this.nbInscriptions = nbInscriptions;
    }

    public int getPlacesRestantes() {
        return Math.max(0, capaciteMax - nbInscriptions);
    }

    public boolean isComplet() {
        return nbInscriptions >= capaciteMax;
    }

    @Override
    public String toString() {
        return nom + " (" + getPlacesRestantes() + " places restantes)";
    }
}
