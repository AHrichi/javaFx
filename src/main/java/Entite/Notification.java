package Entite;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String title;
    private String message;
    private Integer recipientId;
    private RecipientRole recipientRole;
    private NotificationType type;
    private boolean readStatus;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private Source source;

    public enum RecipientRole { ADMIN, COACH, MEMBER }
    public enum NotificationType { INFO, ALERT, PAYMENT, SESSION, SYSTEM }
    public enum Source { MANUAL, AUTO }

    public Notification() {}

    public Notification(String title, String message, RecipientRole recipientRole, NotificationType type) {
        this.title = title;
        this.message = message;
        this.recipientRole = recipientRole;
        this.type = type;
        this.readStatus = false;
        this.source = Source.MANUAL;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getRecipientId() { return recipientId; }
    public void setRecipientId(Integer recipientId) { this.recipientId = recipientId; }

    public RecipientRole getRecipientRole() { return recipientRole; }
    public void setRecipientRole(RecipientRole recipientRole) { this.recipientRole = recipientRole; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}
