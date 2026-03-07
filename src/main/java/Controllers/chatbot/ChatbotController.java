package Controllers.chatbot;

import Service.chatbot.ChatbotService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class ChatbotController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox chatContainer;
    @FXML
    private VBox welcomeMessage;
    @FXML
    private TextField txtMessage;
    @FXML
    private Button btnSend;
    @FXML
    private Button btnClearChat;
    @FXML
    private Label lblStatus;

    private final ChatbotService chatbotService = new ChatbotService();
    private boolean isProcessing = false;

    @FXML
    public void initialize() {
        // Auto-scroll to bottom when new messages are added
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });

        // Enable send button only when there's text
        txtMessage.textProperty().addListener((obs, oldVal, newVal) -> {
            btnSend.setDisable(newVal == null || newVal.trim().isEmpty() || isProcessing);
        });

        btnSend.setDisable(true);
    }

    @FXML
    private void handleSendMessage() {
        String message = txtMessage.getText().trim();

        if (message.isEmpty() || isProcessing) {
            return;
        }

        // Hide welcome message on first message
        if (welcomeMessage.isVisible()) {
            welcomeMessage.setVisible(false);
            welcomeMessage.setManaged(false);
        }

        // Add user message to chat
        addUserMessage(message);

        // Clear input field
        txtMessage.clear();

        // Disable input while processing
        setProcessing(true);
        showStatus("Thinking...");

        // Call API in background thread
        new Thread(() -> {
            try {
                String response = chatbotService.sendMessage(message);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    addBotMessageWithTyping(response);
                    setProcessing(false);
                    hideStatus();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    addBotMessage("❌ Sorry, I encountered an error: " + e.getMessage());
                    setProcessing(false);
                    showStatus("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleClearChat() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Chat");
        alert.setHeaderText("Are you sure you want to clear the chat history?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearChat();
            }
        });
    }

    private void clearChat() {
        chatbotService.clearHistory();
        chatContainer.getChildren().clear();

        // Re-add welcome message
        chatContainer.getChildren().add(welcomeMessage);
        welcomeMessage.setVisible(true);
        welcomeMessage.setManaged(true);

        hideStatus();
    }

    private void addUserMessage(String message) {
        VBox messageBox = createMessageBox(message, true);
        chatContainer.getChildren().add(messageBox);
    }

    private void addBotMessage(String message) {
        VBox messageBox = createMessageBox(message, false);
        chatContainer.getChildren().add(messageBox);
    }

    private void addBotMessageWithTyping(String message) {
        VBox messageBox = new VBox(8);
        messageBox.setMaxWidth(700);

        // Header with role
        Label roleLabel = new Label("SportLink AI");
        roleLabel.setStyle(
                "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: #27ae60;");

        // Message content
        TextFlow textFlow = new TextFlow();
        Text messageText = new Text("");
        messageText.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50;");
        textFlow.getChildren().add(messageText);
        
        // Minimalist bubble: no shadow, just a subtle background
        textFlow.setStyle(
                "-fx-background-color: #f1f3f4; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-padding: 10px 14px; " +
                        "-fx-line-spacing: 2px;");

        messageBox.getChildren().addAll(roleLabel, textFlow);

        // Align based on sender
        messageBox.setAlignment(Pos.TOP_LEFT);

        VBox.setMargin(messageBox, new Insets(0, 0, 10, 0));

        chatContainer.getChildren().add(messageBox);

        // Animate typing effect — word-chunked, capped at 3 seconds max
        String[] words = message.split("(?<=\\s)"); // split but keep whitespace
        int totalWords = words.length;

        // Cap total animation at 3000ms; calculate per-word delay
        double maxAnimationMs = 3000.0;
        double perWordDelay = Math.min(50.0, maxAnimationMs / Math.max(totalWords, 1));

        Timeline timeline = new Timeline();
        StringBuilder accumulated = new StringBuilder();
        for (int i = 0; i < totalWords; i++) {
            accumulated.append(words[i]);
            final String textSoFar = accumulated.toString();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * perWordDelay), e -> {
                messageText.setText(textSoFar);
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        // Ensure full text is shown at the end
        timeline.setOnFinished(e -> messageText.setText(message));
        timeline.play();
    }

    private VBox createMessageBox(String message, boolean isUser) {
        VBox messageBox = new VBox(8);
        messageBox.setMaxWidth(700);

        // Header with role
        Label roleLabel = new Label(isUser ? "You" : "SportLink AI");
        roleLabel.setStyle(
                "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: " + (isUser ? "#3498db" : "#27ae60") + ";");

        // Message content
        TextFlow textFlow = new TextFlow();
        Text messageText = new Text(message);
        
        // Minimalist bubble: Flat design
        textFlow.setStyle(
                "-fx-background-color: " + (isUser ? "#007bff" : "#f1f3f4") + "; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-padding: 10px 14px; " +
                        "-fx-line-spacing: 2px;");

        messageText.setStyle("-fx-font-size: 14px; -fx-fill: " + (isUser ? "#ffffff" : "#2c3e50") + ";");

        textFlow.getChildren().add(messageText);
        messageBox.getChildren().addAll(roleLabel, textFlow);

        // Align based on sender
        if (isUser) {
            messageBox.setAlignment(Pos.TOP_RIGHT);
            HBox.setHgrow(messageBox, Priority.NEVER);
        } else {
            messageBox.setAlignment(Pos.TOP_LEFT);
        }

        VBox.setMargin(messageBox, new Insets(0, 0, 10, 0));

        return messageBox;
    }

    private void setProcessing(boolean processing) {
        isProcessing = processing;
        txtMessage.setDisable(processing);
        btnSend.setDisable(processing || txtMessage.getText().trim().isEmpty());
        btnClearChat.setDisable(processing);
    }

    private void showStatus(String message) {
        lblStatus.setText(message);
        lblStatus.setVisible(true);
    }

    private void hideStatus() {
        lblStatus.setVisible(false);
    }
}
