package Service.chatbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatbotService handles communication with the local Ollama API.
 * This service replaced the previous Gemini and OpenAI implementations
 * for better reliability and zero cost.
 */
public class ChatbotService {

    private static final String API_URL = "http://localhost:11434/api/chat";
    private final List<ChatMessage> conversationHistory;
    private final HttpClient httpClient;
    private final Gson gson;

    public ChatbotService() {
        this.conversationHistory = new ArrayList<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Sends a message to the local Ollama instance and returns the AI response.
     */
    public String sendMessage(String userMessage) throws Exception {
        conversationHistory.add(new ChatMessage("user", userMessage));

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "tinydolphin");
            requestBody.addProperty("stream", false);
            
            JsonArray messages = new JsonArray();
            
            // System context
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", "You are a helpful sports assistant.");
            messages.add(systemMsg);

            // Add history
            for (ChatMessage msg : conversationHistory) {
                JsonObject m = new JsonObject();
                m.addProperty("role", msg.getRole());
                m.addProperty("content", msg.getContent());
                messages.add(m);
            }

            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new IOException("Ollama Error " + response.statusCode() + ": " + response.body());
            }

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            String aiResponse = jsonResponse.getAsJsonObject("message")
                    .get("content").getAsString().trim();

            conversationHistory.add(new ChatMessage("assistant", aiResponse));
            return aiResponse;

        } catch (Exception e) {
            // Remove failed user message from history
            if (!conversationHistory.isEmpty()) {
                conversationHistory.remove(conversationHistory.size() - 1);
            }
            throw new Exception("Local IA Error (Check if Ollama is running): " + e.getMessage());
        }
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public List<ChatMessage> getHistory() {
        return new ArrayList<>(conversationHistory);
    }

    public static class ChatMessage {
        private final String role;
        private final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
