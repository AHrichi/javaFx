package Entite;

import java.sql.Date;

public class InscriptionEvenement {
    private int idInscription;
    private String nomParticipant;
    private String email;
    private Date dateInscription;
    private int idEvenement;

    public InscriptionEvenement() {
    }

    // Constructeur insertion
    public InscriptionEvenement(String nomParticipant, String email, Date dateInscription, int idEvenement) {
        this.nomParticipant = nomParticipant;
        this.email = email;
        this.dateInscription = dateInscription;
        this.idEvenement = idEvenement;
    }

    // Constructeur complet
    public InscriptionEvenement(int idInscription, String nomParticipant, String email,
            Date dateInscription, int idEvenement) {
        this.idInscription = idInscription;
        this.nomParticipant = nomParticipant;
        this.email = email;
        this.dateInscription = dateInscription;
        this.idEvenement = idEvenement;
    }

    // ===== Getters & Setters =====

    public int getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(int idInscription) {
        this.idInscription = idInscription;
    }

    public String getNomParticipant() {
        return nomParticipant;
    }

    public void setNomParticipant(String nomParticipant) {
        this.nomParticipant = nomParticipant;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    @Override
    public String toString() {
        return "Inscription{" + nomParticipant + " / " + email + "}";
    }
}
