package Entite;

public class Coach extends User {

    private String specialite;
    private int experienceAnnees;
    private String certification;
    private String biographie;
    private String photoCertification;

    public Coach() {
        this.typeUser = "Coach";
    }

    public Coach(User user, String specialite, int experienceAnnees,
                 String certification, String biographie,
                 String photoCertification) {

        super(user.idUser, user.nom, user.prenom, user.email,
                user.motDePasse, user.dateInscription, user.telephone,
                user.ville, user.dateNaissance, user.photo,
                "Coach", user.statut);

        this.specialite = specialite;
        this.experienceAnnees = experienceAnnees;
        this.certification = certification;
        this.biographie = biographie;
        this.photoCertification = photoCertification;
    }

    // Getters & Setters

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public int getExperienceAnnees() {
        return experienceAnnees;
    }

    public void setExperienceAnnees(int experienceAnnees) {
        this.experienceAnnees = experienceAnnees;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public String getBiographie() {
        return biographie;
    }

    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }

    public String getPhotoCertification() {
        return photoCertification;
    }

    public void setPhotoCertification(String photoCertification) {
        this.photoCertification = photoCertification;
    }
}
