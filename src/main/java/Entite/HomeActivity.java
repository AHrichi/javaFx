package Entite;

import java.time.LocalDateTime;

public class HomeActivity {

    private int id;
    private int apiExerciseId;
    private String titre;
    private String description;
    private String categorie;
    private String muscles;
    private String equipement;
    private String imageUrl;
    private String videoUrl;
    private String difficulte;
    private LocalDateTime dateCreation;

    public HomeActivity() {
    }

    public HomeActivity(int apiExerciseId, String titre, String description,
            String categorie, String muscles, String equipement,
            String imageUrl, String videoUrl, String difficulte) {
        this.apiExerciseId = apiExerciseId;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.muscles = muscles;
        this.equipement = equipement;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.difficulte = difficulte;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApiExerciseId() {
        return apiExerciseId;
    }

    public void setApiExerciseId(int apiExerciseId) {
        this.apiExerciseId = apiExerciseId;
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getMuscles() {
        return muscles;
    }

    public void setMuscles(String muscles) {
        this.muscles = muscles;
    }

    public String getEquipement() {
        return equipement;
    }

    public void setEquipement(String equipement) {
        this.equipement = equipement;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(String difficulte) {
        this.difficulte = difficulte;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return titre + " (" + categorie + ")";
    }
}
