package Entite;

import java.time.LocalDateTime;

public class Feedback {
    private int idFeedback;
    private int idUser;
    private String userName; // Helper to display in Admin Table
    private int idEntite; // ID of Club or Event
    private String typeEntite; // "CLUB" or "EVENEMENT"
    private String commentaire;
    private String categorie; // "Commentaire", "Amélioration", "Critique", "Non Satisfaction", "Satisfaction"
    private int note; // 1-5 stars
    private LocalDateTime dateFeedback;

    public Feedback() {
    }

    public Feedback(int idUser, int idEntite, String typeEntite, String commentaire, String categorie, int note) {
        this.idUser = idUser;
        this.idEntite = idEntite;
        this.typeEntite = typeEntite;
        this.commentaire = commentaire;
        this.categorie = categorie;
        this.note = note;
        this.dateFeedback = LocalDateTime.now();
    }

    // Getters and Setters
    public int getIdFeedback() {
        return idFeedback;
    }

    public void setIdFeedback(int idFeedback) {
        this.idFeedback = idFeedback;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getIdEntite() {
        return idEntite;
    }

    public void setIdEntite(int idEntite) {
        this.idEntite = idEntite;
    }

    public String getTypeEntite() {
        return typeEntite;
    }

    public void setTypeEntite(String typeEntite) {
        this.typeEntite = typeEntite;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public LocalDateTime getDateFeedback() {
        return dateFeedback;
    }

    public void setDateFeedback(LocalDateTime dateFeedback) {
        this.dateFeedback = dateFeedback;
    }
}
