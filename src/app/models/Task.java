package app.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {

    public enum Status { BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, WAITING_FOR_RETEST, DONE }
    
    public enum BackgroundColor {
        WHITE("#ffffff"),
        LIGHT_BLUE("#e3f2fd"),
        LIGHT_GREEN("#e8f5e9"),
        LIGHT_YELLOW("#fff9c4"),
        LIGHT_ORANGE("#ffe0b2"),
        LIGHT_PINK("#fce4ec"),
        LIGHT_PURPLE("#f3e5f5"),
        LIGHT_GRAY("#f5f5f5");

        private final String hex;

        BackgroundColor(String hex) {
            this.hex = hex;
        }

        public String getHex() {
            return hex;
        }
    }

    private String id;
    private String title;
    private String description;
    private String issueDescription;
    private List<String> labels;
    private BackgroundColor backgroundColor;
    private LocalDateTime creationDate;
    private List<Comment> comments;
    private Status status;

    public Task() {
        // Generate ID if not set (for JSON deserialization)
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.labels == null) {
            this.labels = new ArrayList<>();
        }
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        if (this.backgroundColor == null) {
            this.backgroundColor = BackgroundColor.WHITE;
        }
        if (this.creationDate == null) {
            this.creationDate = LocalDateTime.now();
        }
    }

    public Task(String title, String description, Status status) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.issueDescription = "";
        this.labels = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.backgroundColor = BackgroundColor.WHITE;
        this.creationDate = LocalDateTime.now();
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
    
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    
    public BackgroundColor getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(BackgroundColor backgroundColor) { this.backgroundColor = backgroundColor; }
    
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    public String getFormattedCreationDate() {
        if (creationDate == null) return "";
        return creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
