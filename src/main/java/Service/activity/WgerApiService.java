package Service.activity;

import Entite.HomeActivity;
import com.google.gson.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to fetch exercises from the wger Workout Manager API.
 * API docs: https://wger.de/api/v2/
 * No API key required — all endpoints are public.
 */
public class WgerApiService {

    private static final String BASE_URL = "https://wger.de/api/v2";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Fetch exercises from the API with pagination.
     * We use the /exerciseinfo endpoint directly because it contains images,
     * videos, and descriptions without needing N+1 requests.
     * 
     * @param language 2 = English
     * @param limit    how many results per page
     * @param offset   starting index
     */
    public List<HomeActivity> fetchExercises(int language, int limit, int offset) throws Exception {
        String url = BASE_URL + "/exerciseinfo/?format=json&language=" + language + "&limit=" + limit + "&offset="
                + offset;
        String json = httpGet(url);
        return parseExerciseInfoList(json);
    }

    /**
     * Fetch exercises filtered by category ID.
     */
    public List<HomeActivity> fetchExercisesByCategory(int categoryId, int language, int limit, int offset)
            throws Exception {
        String url = BASE_URL + "/exerciseinfo/?format=json&language=" + language
                + "&category=" + categoryId + "&limit=" + limit + "&offset=" + offset;
        String json = httpGet(url);
        return parseExerciseInfoList(json);
    }

    /**
     * Fetch full exercise info (name, description, images, videos) for a single
     * exercise.
     */
    public HomeActivity fetchExerciseInfo(int exerciseId) throws Exception {
        String url = BASE_URL + "/exerciseinfo/" + exerciseId + "/?format=json";
        String json = httpGet(url);
        return parseExerciseInfo(json);
    }

    /**
     * Fetch all exercise categories (Arms, Chest, etc.)
     * Returns a list of [id, name] pairs.
     */
    public List<String[]> fetchCategories() throws Exception {
        String url = BASE_URL + "/exercisecategory/?format=json";
        String json = httpGet(url);
        List<String[]> categories = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.getAsJsonArray("results");
        for (JsonElement el : results) {
            JsonObject cat = el.getAsJsonObject();
            String id = cat.get("id").getAsString();
            String name = cat.get("name").getAsString();
            categories.add(new String[] { id, name });
        }
        return categories;
    }

    /**
     * Get the total number of exercises available.
     */
    public int getTotalCount(int language) throws Exception {
        String url = BASE_URL + "/exercise/?format=json&language=" + language + "&limit=1";
        String json = httpGet(url);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return root.get("count").getAsInt();
    }

    // ── Private Helpers ──────────────────────────────────

    private String httpGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("API error: HTTP " + response.statusCode());
        }
        return response.body();
    }

    private List<HomeActivity> parseExerciseInfoList(String json) {
        List<HomeActivity> list = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.getAsJsonArray("results");

        for (JsonElement el : results) {
            HomeActivity ha = parseExerciseInfoObject(el.getAsJsonObject());
            if (ha.getTitre() != null && !ha.getTitre().isEmpty()) {
                list.add(ha);
            }
        }
        return list;
    }

    private HomeActivity parseExerciseInfo(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return parseExerciseInfoObject(root);
    }

    private HomeActivity parseExerciseInfoObject(JsonObject root) {
        HomeActivity ha = new HomeActivity();

        ha.setApiExerciseId(root.get("id").getAsInt());

        // Category
        if (root.has("category") && !root.get("category").isJsonNull()) {
            JsonObject cat = root.getAsJsonObject("category");
            ha.setCategorie(cat.get("name").getAsString());
        }

        // Get English translation (language=2)
        if (root.has("translations") && root.get("translations").isJsonArray()) {
            for (JsonElement tr : root.getAsJsonArray("translations")) {
                JsonObject trans = tr.getAsJsonObject();
                if (trans.get("language").getAsInt() == 2) { // English
                    ha.setTitre(trans.get("name").getAsString());
                    ha.setDescription(trans.get("description").getAsString());
                    break;
                }
            }
        }

        // Muscles
        if (root.has("muscles") && root.get("muscles").isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement m : root.getAsJsonArray("muscles")) {
                JsonObject muscle = m.getAsJsonObject();
                if (sb.length() > 0)
                    sb.append(", ");
                if (muscle.has("name_en") && !muscle.get("name_en").isJsonNull()) {
                    sb.append(muscle.get("name_en").getAsString());
                } else if (muscle.has("name") && !muscle.get("name").isJsonNull()) {
                    sb.append(muscle.get("name").getAsString());
                }
            }
            ha.setMuscles(sb.toString());
        }

        // Equipment
        if (root.has("equipment") && root.get("equipment").isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement eq : root.getAsJsonArray("equipment")) {
                JsonObject equip = eq.getAsJsonObject();
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(equip.get("name").getAsString());
            }
            ha.setEquipement(sb.toString());
        }

        // Images — take first image
        if (root.has("images") && root.get("images").isJsonArray()) {
            JsonArray images = root.getAsJsonArray("images");
            if (images.size() > 0) {
                String imageUrl = images.get(0).getAsJsonObject().get("image").getAsString();
                ha.setImageUrl(imageUrl);
            }
        }

        // Videos — take first video
        if (root.has("videos") && root.get("videos").isJsonArray()) {
            JsonArray videos = root.getAsJsonArray("videos");
            if (videos.size() > 0) {
                String videoUrl = videos.get(0).getAsJsonObject().get("video").getAsString();
                ha.setVideoUrl(videoUrl);
            }
        }

        return ha;
    }
}
