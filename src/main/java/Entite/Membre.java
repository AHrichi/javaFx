package Entite;

public class Membre extends User {

    private int tailleCm;
    private double poidsKg;
    private String objectifSportif;
    private String photoProgression;

    public Membre() {
        this.typeUser = "Membre";
    }

    public Membre(User user, int tailleCm, double poidsKg,
                  String objectifSportif, String photoProgression) {

        super(user.idUser, user.nom, user.prenom, user.email,
                user.motDePasse, user.dateInscription, user.telephone,
                user.ville, user.dateNaissance, user.photo,
                "Membre", user.statut);

        this.tailleCm = tailleCm;
        this.poidsKg = poidsKg;
        this.objectifSportif = objectifSportif;
        this.photoProgression = photoProgression;
    }

    // Getters & Setters

    public int getTailleCm() {
        return tailleCm;
    }

    public void setTailleCm(int tailleCm) {
        this.tailleCm = tailleCm;
    }

    public double getPoidsKg() {
        return poidsKg;
    }

    public void setPoidsKg(double poidsKg) {
        this.poidsKg = poidsKg;
    }

    public String getObjectifSportif() {
        return objectifSportif;
    }

    public void setObjectifSportif(String objectifSportif) {
        this.objectifSportif = objectifSportif;
    }

    public String getPhotoProgression() {
        return photoProgression;
    }

    public void setPhotoProgression(String photoProgression) {
        this.photoProgression = photoProgression;
    }
}
