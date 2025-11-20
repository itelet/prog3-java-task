package app.services;

import app.models.Comment;
import app.models.Task;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StorageService {

    private static final String FILE = "tasks.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(dateTime.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            try {
                return LocalDateTime.parse(json.getAsString(), formatter);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static List<Task> loadTasks() {
        try {
            if (!Files.exists(Path.of(FILE))) return new ArrayList<>();
            List<Task> tasks = gson.fromJson(new FileReader(FILE), new TypeToken<List<Task>>(){}.getType());
            // Ensure all tasks have IDs and default values (for backward compatibility with old JSON files)
            if (tasks != null) {
                for (Task task : tasks) {
                    if (task.getId() == null || task.getId().isEmpty()) {
                        task.setId(java.util.UUID.randomUUID().toString());
                    }
                    if (task.getLabels() == null) {
                        task.setLabels(new ArrayList<>());
                    }
                    if (task.getComments() == null) {
                        task.setComments(new ArrayList<>());
                    }
                    if (task.getBackgroundColor() == null) {
                        task.setBackgroundColor(Task.BackgroundColor.WHITE);
                    }
                    if (task.getCreationDate() == null) {
                        task.setCreationDate(LocalDateTime.now());
                    }
                    if (task.getIssueDescription() == null) {
                        task.setIssueDescription("");
                    }
                    // Ensure comments have IDs
                    if (task.getComments() != null) {
                        for (Comment comment : task.getComments()) {
                            if (comment.getId() == null || comment.getId().isEmpty()) {
                                comment.setId(java.util.UUID.randomUUID().toString());
                            }
                            if (comment.getTimestamp() == null) {
                                comment.setTimestamp(LocalDateTime.now());
                            }
                        }
                    }
                }
            }
            return tasks != null ? tasks : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(FILE)) {
            gson.toJson(tasks, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
