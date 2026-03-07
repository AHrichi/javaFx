package Entite;

public class DemandeAdhesion {
    private int id;
    private String typeEntite;
    private int idEntite;
    private String nomEntite;
    private int idUser;
    private String nomUser;
    private String emailUser;
    private String statut;
    private String dateDemande;

    public DemandeAdhesion(int id, String typeEntite, int idEntite, String nomEntite, int idUser, String nomUser,
            String emailUser, String statut, String dateDemande) {
        this.id = id;
        this.typeEntite = typeEntite;
        this.idEntite = idEntite;
        this.nomEntite = nomEntite;
        this.idUser = idUser;
        this.nomUser = nomUser;
        this.emailUser = emailUser;
        this.statut = statut;
        this.dateDemande = dateDemande;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeEntite() {
        return typeEntite;
    }

    public void setTypeEntite(String typeEntite) {
        this.typeEntite = typeEntite;
    }

    public int getIdEntite() {
        return idEntite;
    }

    public void setIdEntite(int idEntite) {
        this.idEntite = idEntite;
    }

    public String getNomEntite() {
        return nomEntite;
    }

    public void setNomEntite(String nomEntite) {
        this.nomEntite = nomEntite;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNomUser() {
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(String dateDemande) {
        this.dateDemande = dateDemande;
    }
}
