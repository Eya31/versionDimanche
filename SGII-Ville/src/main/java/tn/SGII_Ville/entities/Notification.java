package tn.SGII_Ville.entities;

import java.time.LocalDateTime;

public class Notification {
    private int idNotification;
    private String message;
    private LocalDateTime createdAt;
    private int userId;
    private boolean readable;

    // Constructeurs
    public Notification() {}

    public Notification(String message, int userId) {
        this.message = message;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.readable = false;
    }

    // Getters et Setters
    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isReadable() { return readable; }
    public void setReadable(boolean readable) { this.readable = readable; }
}