package Entite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {

    protected int idUser;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String motDePasse;
    protected LocalDateTime dateInscription;
    protected String telephone;
    protected String ville;
    protected LocalDate dateNaissance;
    protected String photo;
    protected String typeUser;   // Coach ou Membre
    protected String statut;     // actif, inactif, en attente

    public User() {
    }

    public User(int idUser, String nom, String prenom, String email,
                String motDePasse, LocalDateTime dateInscription,
                String telephone, String ville, LocalDate dateNaissance,
                String photo, String typeUser, String statut) {

        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = dateInscription;
        this.telephone = telephone;
        this.ville = ville;
        this.dateNaissance = dateNaissance;
        this.photo = photo;
        this.typeUser = typeUser;
        this.statut = statut;
    }

    // Getters & Setters

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getTypeUser() {
        return typeUser;
    }

    public void setTypeUser(String typeUser) {
        this.typeUser = typeUser;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
