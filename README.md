# SportLink

## Module Notifications — Configuration

### 1. Base de données (phpMyAdmin)
Exécuter le script `scripts/add_notifications_table.sql` dans phpMyAdmin sur la base `sportlinkPj`.

### 2. Connexion MySQL
Adapter dans `Utils.DataSource` : URL, USER, PASSWORD selon votre configuration.

### 3. Police Poppins (optionnel)
Télécharger [Poppins](https://fonts.google.com/specimen/Poppins) et placer `Poppins-Regular.ttf` dans `src/main/resources/fonts/`.

### 4. Lancer l'application
```bash
mvn javafx:run
```