package Entite;

import java.time.LocalDateTime;

public class Seance {

    private int idSeance;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    private int idClub;
    private int idCoach;

    private String typeSeance;
    private String niveau;

    private int capaciteMax;
    private String statut;
    private String photoSeance;

    // ✅ Constructeur vide
    public Seance() {
        this.statut = "planifiée";
        this.capaciteMax = 20;
    }

    // ✅ Constructeur complet
    public Seance(String titre, String description,
                  LocalDateTime dateDebut, LocalDateTime dateFin,
                  int idClub, int idCoach,
                  String typeSeance, String niveau,
                  int capaciteMax, String statut,
                  String photoSeance) {

        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.idClub = idClub;
        this.idCoach = idCoach;
        this.typeSeance = typeSeance;
        this.niveau = niveau;
        this.capaciteMax = capaciteMax;
        this.statut = statut;
        this.photoSeance = photoSeance;
    }

    // ✅ Getters & Setters

    public int getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(int idSeance) {
        this.idSeance = idSeance;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public int getIdClub() {
        return idClub;
    }

    public void setIdClub(int idClub) {
        this.idClub = idClub;
    }

    public int getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(int idCoach) {
        this.idCoach = idCoach;
    }

    public String getTypeSeance() {
        return typeSeance;
    }

    public void setTypeSeance(String typeSeance) {
        this.typeSeance = typeSeance;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPhotoSeance() {
        return photoSeance;
    }

    public void setPhotoSeance(String photoSeance) {
        this.photoSeance = photoSeance;
    }

    @Override
    public String toString() {
        return titre + " (" + dateDebut.toLocalTime() + ")";
    }
}
