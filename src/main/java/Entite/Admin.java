package Entite;

public class Admin {

    private int idAdmin;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String statut; // actif, inactif

    public Admin() {
    }

    public Admin(int idAdmin, String nom, String prenom, String email,
            String motDePasse, String telephone, String statut) {
        this.idAdmin = idAdmin;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.statut = statut;
    }

    // Getters & Setters
    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "Admin{idAdmin=" + idAdmin + ", nom='" + nom + "', prenom='" + prenom +
                "', email='" + email + "', statut='" + statut + "'}";
    }
}
