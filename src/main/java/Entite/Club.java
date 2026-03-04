package Entite;

public class Club {
    private int idClub;
    private String nom;
    private String adresse;
    private String ville;

    public Club() {
    }

    public Club(int idClub, String nom, String adresse, String ville) {
        this.idClub = idClub;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
    }

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

    @Override
    public String toString() {
        return nom; // Pour l'affichage dans le ComboBox
    }
}