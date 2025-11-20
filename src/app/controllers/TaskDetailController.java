package app.controllers;

import app.models.Comment;
import app.models.Task;
import app.models.User;
import app.services.StorageService;
import app.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class TaskDetailController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private HBox colorButtonsContainer;
    @FXML private TextField newLabelField;
    @FXML private FlowPane labelsContainer;
    @FXML private Label creationDateLabel;
    @FXML private VBox commentsContainer;
    @FXML private TextArea newCommentArea;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private Task task;
    private List<Task> allTasks;
    private Runnable onTaskUpdated;
    private Runnable onTaskDeleted;

    public void setTask(Task task, List<Task> allTasks, Runnable onTaskUpdated, Runnable onTaskDeleted) {
        this.task = task;
        this.allTasks = allTasks;
        this.onTaskUpdated = onTaskUpdated;
        this.onTaskDeleted = onTaskDeleted;
        loadTaskData();
    }

    @FXML
    public void initialize() {
        setupColorButtons();
        setupPermissionBasedUI();
    }
    
    private void setupPermissionBasedUI() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) return;
        
        boolean canModify = currentUser.getPermission() == User.Permission.ADMIN || 
                           currentUser.getPermission() == User.Permission.PERMITTED;
        
        if (!canModify) {
            // Read-only: disable all editing fields
            if (titleField != null) titleField.setEditable(false);
            if (descriptionArea != null) descriptionArea.setEditable(false);
            if (newLabelField != null) newLabelField.setEditable(false);
            if (newCommentArea != null) newCommentArea.setEditable(false);
            if (colorButtonsContainer != null) {
                for (var node : colorButtonsContainer.getChildren()) {
                    if (node instanceof javafx.scene.control.Button) {
                        ((javafx.scene.control.Button) node).setDisable(true);
                    }
                }
            }
            if (saveButton != null) saveButton.setVisible(false);
            if (deleteButton != null) deleteButton.setVisible(false);
        }
    }

    private void loadTaskData() {
        if (task == null) return;

        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
        creationDateLabel.setText("Created: " + task.getFormattedCreationDate());

        refreshLabels();
        refreshComments();
        selectCurrentColor();
    }

    private void setupColorButtons() {
        colorButtonsContainer.getChildren().clear();
        for (Task.BackgroundColor color : Task.BackgroundColor.values()) {
            Button colorBtn = new Button();
            colorBtn.setPrefSize(40, 40);
            colorBtn.setStyle(
                "-fx-background-color: " + color.getHex() + "; " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 20; " +
                "-fx-cursor: hand;"
            );
            colorBtn.setOnAction(e -> {
                task.setBackgroundColor(color);
                selectCurrentColor();
            });
            colorBtn.setUserData(color);
            colorButtonsContainer.getChildren().add(colorBtn);
        }
    }

    private void selectCurrentColor() {
        if (task == null) return;
        for (var node : colorButtonsContainer.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                Task.BackgroundColor btnColor = (Task.BackgroundColor) btn.getUserData();
                if (btnColor == task.getBackgroundColor()) {
                    btn.setStyle(
                        "-fx-background-color: " + btnColor.getHex() + "; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #0079bf; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 20; " +
                        "-fx-cursor: hand;"
                    );
                } else {
                    btn.setStyle(
                        "-fx-background-color: " + btnColor.getHex() + "; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #ccc; " +
                        "-fx-border-radius: 20; " +
                        "-fx-cursor: hand;"
                    );
                }
            }
        }
    }

    private void refreshLabels() {
        labelsContainer.getChildren().clear();
        if (task.getLabels() != null) {
            for (String label : task.getLabels()) {
                Label labelText = new Label(label);
                labelText.setTextFill(javafx.scene.paint.Color.BLACK);
                labelText.setStyle(
                    "-fx-background-color: #e4e6ea; " +
                    "-fx-background-radius: 3; " +
                    "-fx-padding: 5 10; " +
                    "-fx-font-size: 12px;"
                );
                labelsContainer.getChildren().add(labelText);
            }
        }
    }

    private void refreshComments() {
        commentsContainer.getChildren().clear();
        if (task.getComments() != null) {
            for (Comment comment : task.getComments()) {
                VBox commentBox = new VBox(5);
                commentBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
                
                Label commentText = new Label(comment.getText());
                commentText.setWrapText(true);
                commentText.setMaxWidth(Double.MAX_VALUE);
                
                Label timestampLabel = new Label(comment.getFormattedTimestamp());
                timestampLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                
                commentBox.getChildren().addAll(commentText, timestampLabel);
                commentsContainer.getChildren().add(commentBox);
            }
        }
    }

    @FXML
    private void onAddLabelClicked() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null || currentUser.getPermission() == User.Permission.READ_ONLY) {
            return; // Read-only users can't add labels
        }
        
        String labelText = newLabelField.getText().trim();
        if (!labelText.isEmpty() && !task.getLabels().contains(labelText)) {
            task.getLabels().add(labelText);
            newLabelField.clear();
            refreshLabels();
        }
    }

    @FXML
    private void onAddCommentClicked() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null || currentUser.getPermission() == User.Permission.READ_ONLY) {
            return; // Read-only users can't add comments
        }
        
        String commentText = newCommentArea.getText().trim();
        if (!commentText.isEmpty()) {
            Comment comment = new Comment(commentText);
            task.getComments().add(comment);
            newCommentArea.clear();
            refreshComments();
        }
    }

    @FXML
    private void onSaveClicked() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null || currentUser.getPermission() == User.Permission.READ_ONLY) {
            return; // Read-only users can't save
        }
        
        task.setTitle(titleField.getText());
        task.setDescription(descriptionArea.getText());
        
        StorageService.saveTasks(allTasks);
        if (onTaskUpdated != null) {
            onTaskUpdated.run();
        }
        closeWindow();
    }

    @FXML
    private void onDeleteClicked() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null || currentUser.getPermission() == User.Permission.READ_ONLY) {
            return; // Read-only users can't delete
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                allTasks.remove(task);
                StorageService.saveTasks(allTasks);
                if (onTaskDeleted != null) {
                    onTaskDeleted.run();
                }
                closeWindow();
            }
        });
    }

    private void closeWindow() {
        // Get the stage from any node in the scene
        if (titleField != null && titleField.getScene() != null) {
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
        }
    }
}

