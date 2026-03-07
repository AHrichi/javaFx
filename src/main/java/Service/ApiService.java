package Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'API externe — Utilise l'API Nager.Date (gratuite, sans clé)
 * pour récupérer les jours fériés d'un pays.
 *
 * URL: https://date.nager.at/api/v3/publicholidays/{YEAR}/{COUNTRY_CODE}
 */
//classe qui communique avec l'API
public class ApiService {
//C’est l’objet qui permet d’envoyer des requêtes HTTP.
    private final HttpClient httpClient;
//constructeur
    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Récupère les jours fériés pour une année et un pays donnés.
     *
     * @param year        L'année (ex: 2024)
     * @param countryCode Le code pays (ex: "TN" pour Tunisie)
     * @return Map<LocalDate, String> où Key=Date et Value=Nom du jour férié
     */
    //methide renvoie Date → Nom du jour férié
    public Map<LocalDate, String> getHolidays(int year, String countryCode) {
        Map<LocalDate, String> holidays = new HashMap<>();
        try {
            //construire URL
            String url = "https://date.nager.at/api/v3/publicholidays/" + year + "/" + countryCode;
//Construire la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
//👉 L’API renvoie un JSON sous forme de texte.
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Erreur API: Code " + response.statusCode());
                return holidays;
            }
//String json = response.body();
//parseHolidaysJson(json, holidays);
            String json = response.body();
            // Le JSON est un tableau d'objets : [{"date":"2024-01-01","localName":"Jour de
            // l'An",...}, ...]
            // Parser manuellement sans Jackson/Gson

            parseHolidaysJson(json, holidays);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API Jours Fériés : " + e.getMessage());
        }
        return holidays;
    }
    //private void parseHolidaysJson(String json, Map<LocalDate, String> holidays) convertir json en MAP
    private void parseHolidaysJson(String json, Map<LocalDate, String> holidays) {
        // Nettoyage basique pour itérer sur les objets
        String content = json.trim();
        if (content.startsWith("["))
            content = content.substring(1);
        if (content.endsWith("]"))
            content = content.substring(0, content.length() - 1);

        // Séparer les objets (approximation basée sur "},{")
        // Attention: cela suppose que le JSON ne contient pas d'objets imbriqués
        // complexes
        String[] objects = content.split("},\\{");

        for (String obj : objects) {
            String dateStr = extractString(obj, "\"date\":");
            String name = extractString(obj, "\"name\":");

            if (!dateStr.isEmpty() && !name.isEmpty()) {
                try {
                    String frenchName = translateToFrench(name);
                    holidays.put(LocalDate.parse(dateStr), frenchName);
                } catch (Exception e) {
                    // Ignore invalid dates
                }
            }
        }
    }

    private String translateToFrench(String englishName) {
        switch (englishName) {
            case "New Year's Day":
                return "Jour de l'An";
            case "Revolution and Youth Day":
                return "Fête de la Révolution";
            case "Independence Day":
                return "Fête de l'Indépendance";
            case "Martyrs' Day":
                return "Fête des Martyrs";
            case "Labour Day":
                return "Fête du Travail";
            case "Eid al-Fitr":
                return "Aïd el-Fitr";
            case "Eid al-Adha":
                return "Aïd el-Adha";
            case "Republic Day":
                return "Fête de la République";
            case "Islamic New Year":
                return "Ras El Am El Hijri";
            case "Evacuation Day":
                return "Fête de l'Évacuation";
            case "Mouled":
                return "Mouled (Naissance du Prophète)";
            default:
                return englishName;
        }
    }

    private String extractString(String source, String key) {
        // Recherche clé
        int keyIndex = source.indexOf(key);
        if (keyIndex == -1)
            return "";

        // Recherche valeur (après clé, entre guillemets)
        // Format attendu: "key":"valeur"
        int startQuote = source.indexOf("\"", keyIndex + key.length());
        if (startQuote == -1)
            return "";

        // Gérer les espaces éventuels entre : et " (ex: "key": "valeur")
        // Simplification: on cherche le premier guillemet après la clé

        int endQuote = source.indexOf("\"", startQuote + 1);
        if (endQuote == -1)
            return "";

        return source.substring(startQuote + 1, endQuote);
    }
}
//Appelle une API externe
//Récupère les jours fériés en JSON
//Analyse le JSON manuellement
//Traduits les noms en français
//Retourne une Map Date → Nom
