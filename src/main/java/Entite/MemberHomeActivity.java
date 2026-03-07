package Entite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MemberHomeActivity {

    private int id;
    private int idMembre;
    private int idActivite;
    private LocalDate datePlanifiee;
    private String statut; // planifié / complété
    private LocalDateTime dateCompletion;

    // Transient reference for display
    private HomeActivity activite;

    public MemberHomeActivity() {
    }

    public MemberHomeActivity(int idMembre, int idActivite, LocalDate datePlanifiee) {
        this.idMembre = idMembre;
        this.idActivite = idActivite;
        this.datePlanifiee = datePlanifiee;
        this.statut = "planifié";
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdMembre() {
        return idMembre;
    }

    public void setIdMembre(int idMembre) {
        this.idMembre = idMembre;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public LocalDate getDatePlanifiee() {
        return datePlanifiee;
    }

    public void setDatePlanifiee(LocalDate datePlanifiee) {
        this.datePlanifiee = datePlanifiee;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCompletion() {
        return dateCompletion;
    }

    public void setDateCompletion(LocalDateTime dateCompletion) {
        this.dateCompletion = dateCompletion;
    }

    public HomeActivity getActivite() {
        return activite;
    }

    public void setActivite(HomeActivity activite) {
        this.activite = activite;
    }
}
