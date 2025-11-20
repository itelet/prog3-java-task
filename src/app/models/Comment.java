package app.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment {
    private String id;
    private String text;
    private LocalDateTime timestamp;

    public Comment() {
        this.id = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public Comment(String text) {
        this.id = java.util.UUID.randomUUID().toString();
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}

