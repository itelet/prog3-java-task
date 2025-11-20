package app.controllers;

import app.models.Task;
import app.models.User;
import app.services.StorageService;
import app.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class BoardController {

    @FXML private VBox backlogColumn;
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox inReviewColumn;
    @FXML private VBox waitingForRetestColumn;
    @FXML private VBox doneColumn;
    @FXML private Button addTaskButton;
    @FXML private Button logoutButton;
    @FXML private Button manageUsersButton;
    @FXML private Label userInfoLabel;

    private List<Task> tasks;

    @FXML
    public void initialize() {
        // Check if user is logged in
        if (!UserService.isLoggedIn()) {
            // Should not happen if login works correctly, but safety check
            return;
        }
        
        tasks = StorageService.loadTasks();
        setupDragAndDrop();
        setupButtonHover();
        setupPermissionBasedUI();
        refreshUI();
    }
    
    private void setupPermissionBasedUI() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) return;
        
        // Display user info
        if (userInfoLabel != null) {
            userInfoLabel.setText("User: " + currentUser.getUsername() + " (" + currentUser.getPermission() + ")");
        }
        
        // Show/hide admin button
        if (manageUsersButton != null) {
            manageUsersButton.setVisible(UserService.isAdmin());
        }
        
        // Hide/disable features based on permission
        switch (currentUser.getPermission()) {
            case READ_ONLY:
                // Hide add button and disable all modifications
                if (addTaskButton != null) {
                    addTaskButton.setVisible(false);
                }
                break;
            case PERMITTED:
                // Can add tasks and modify, but might need card-level restrictions later
                break;
            case ADMIN:
                // Full access, no restrictions
                break;
        }
    }
    
    @FXML
    private void onLogoutClicked() {
        UserService.logout();
        // Close board and show login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 500);
            
            Stage stage = new Stage();
            stage.setTitle("Login - Task Manager");
            stage.setScene(scene);
            stage.setResizable(false);
            
            // Close board window
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean canModifyTask(Task task) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) return false;
        
        switch (currentUser.getPermission()) {
            case ADMIN:
                return true;
            case PERMITTED:
                // For now, permitted users can modify all tasks
                // This can be extended later with task-level permissions
                return true;
            case READ_ONLY:
                return false;
            default:
                return false;
        }
    }
    
    private boolean canViewTask(Task task) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) return false;
        
        // All logged-in users can view tasks for now
        // This can be extended with task-level visibility later
        return true;
    }

    private void setupButtonHover() {
        if (addTaskButton != null) {
            addTaskButton.setOnMouseEntered(e -> 
                addTaskButton.setStyle("-fx-background-color: #61bd4f; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;")
            );
            addTaskButton.setOnMouseExited(e -> 
                addTaskButton.setStyle("-fx-background-color: #5aac44; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;")
            );
        }
    }

    private void setupDragAndDrop() {
        setupColumnDropTarget(backlogColumn, Task.Status.BACKLOG);
        setupColumnDropTarget(todoColumn, Task.Status.TODO);
        setupColumnDropTarget(inProgressColumn, Task.Status.IN_PROGRESS);
        setupColumnDropTarget(inReviewColumn, Task.Status.IN_REVIEW);
        setupColumnDropTarget(waitingForRetestColumn, Task.Status.WAITING_FOR_RETEST);
        setupColumnDropTarget(doneColumn, Task.Status.DONE);
    }

    private void setupColumnDropTarget(VBox column, Task.Status targetStatus) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragEntered(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5;");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle("");
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String taskId = db.getString();
                Task draggedTask = findTaskById(taskId);
                if (draggedTask != null && canModifyTask(draggedTask)) {
                    // Check if status changed
                    boolean statusChanged = draggedTask.getStatus() != targetStatus;
                    if (statusChanged) {
                        draggedTask.setStatus(targetStatus);
                    }
                    
                    // Reorder: move to end of the target column
                    reorderTask(draggedTask, targetStatus, -1);
                    StorageService.saveTasks(tasks);
                    refreshUI();
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    private void reorderTask(Task task, Task.Status targetStatus, int insertIndex) {
        // Remove task from current position
        tasks.remove(task);
        
        if (insertIndex < 0) {
            // Insert at end of tasks with same status
            int insertPos = tasks.size();
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getStatus() == targetStatus) {
                    insertPos = i + 1;
                } else if (tasks.get(i).getStatus().ordinal() > targetStatus.ordinal()) {
                    insertPos = i;
                    break;
                }
            }
            tasks.add(insertPos, task);
        } else {
            // Insert at specific index
            tasks.add(insertIndex, task);
        }
    }

    private void setupCardDropTarget(VBox card, Task targetTask) {
        card.setOnDragOver(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                
                // Show drop indicator
                double y = event.getY();
                double cardHeight = card.getBoundsInLocal().getHeight();
                
                // Determine if dropping above or below the card
                if (y < cardHeight / 2) {
                    // Dropping above - show indicator at top
                    card.setStyle(card.getStyle() + " -fx-border-color: #0079bf; -fx-border-width: 2 0 0 0;");
                } else {
                    // Dropping below - show indicator at bottom
                    card.setStyle(card.getStyle() + " -fx-border-color: #0079bf; -fx-border-width: 0 0 2 0;");
                }
            }
            event.consume();
        });
        
        card.setOnDragExited(event -> {
            // Remove drop indicator
            String bgColor = targetTask.getBackgroundColor() != null ? 
                targetTask.getBackgroundColor().getHex() : "#ffffff";
            card.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 3; " +
                "-fx-padding: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1); " +
                "-fx-cursor: hand;"
            );
            event.consume();
        });
        
        card.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String draggedTaskId = db.getString();
                Task draggedTask = findTaskById(draggedTaskId);
                
                if (draggedTask != null && !draggedTask.getId().equals(targetTask.getId()) && canModifyTask(draggedTask)) {
                    double y = event.getY();
                    double cardHeight = card.getBoundsInLocal().getHeight();
                    boolean dropAbove = y < cardHeight / 2;
                    
                    // Remove dragged task from current position
                    int draggedIndex = tasks.indexOf(draggedTask);
                    tasks.remove(draggedIndex);
                    
                    // Find target task index (may have shifted after removal)
                    int targetIndex = tasks.indexOf(targetTask);
                    
                    // Determine if status needs to change
                    boolean statusChanged = draggedTask.getStatus() != targetTask.getStatus();
                    if (statusChanged) {
                        draggedTask.setStatus(targetTask.getStatus());
                    }
                    
                    // Calculate insert position
                    int insertIndex;
                    if (dropAbove) {
                        insertIndex = targetIndex;
                    } else {
                        insertIndex = targetIndex + 1;
                    }
                    
                    // If we removed an item before the target, adjust index
                    if (draggedIndex < targetIndex) {
                        // No adjustment needed, targetIndex already accounts for removal
                    } else if (draggedIndex > targetIndex && !dropAbove) {
                        // Removed item was after target, and we're inserting below
                        // No adjustment needed
                    }
                    
                    // Ensure we don't go out of bounds
                    insertIndex = Math.max(0, Math.min(insertIndex, tasks.size()));
                    
                    tasks.add(insertIndex, draggedTask);
                    StorageService.saveTasks(tasks);
                    refreshUI();
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private Task findTaskById(String id) {
        // Find task by unique ID (stored in dragboard)
        return tasks.stream()
                .filter(t -> t.getId() != null && t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void refreshUI() {
        // Remove all cards (keep only the header labels)
        backlogColumn.getChildren().removeIf(n -> n.getStyleClass().contains("task-card"));
        todoColumn.getChildren().removeIf(n -> n.getStyleClass().contains("task-card"));
        inProgressColumn.getChildren().removeIf(n -> n.getStyleClass().contains("task-card"));
        inReviewColumn.getChildren().removeIf(n -> n.getStyleClass().contains("task-card"));
        waitingForRetestColumn.getChildren().removeIf(n -> n.getStyleClass().contains("task-card"));
        doneColumn.getChildren().removeIf(n -> n.getStyleClass().contains("task-card"));

        for (Task task : tasks) {
            // Only show tasks user can view
            if (!canViewTask(task)) {
                continue;
            }
            
            VBox card = createTaskCard(task);
            
            switch (task.getStatus()) {
                case BACKLOG -> backlogColumn.getChildren().add(card);
                case TODO -> todoColumn.getChildren().add(card);
                case IN_PROGRESS -> inProgressColumn.getChildren().add(card);
                case IN_REVIEW -> inReviewColumn.getChildren().add(card);
                case WAITING_FOR_RETEST -> waitingForRetestColumn.getChildren().add(card);
                case DONE -> doneColumn.getChildren().add(card);
            }
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(8);
        card.getStyleClass().add("task-card");
        
        // Set background color
        String bgColor = task.getBackgroundColor() != null ? 
            task.getBackgroundColor().getHex() : "#ffffff";
        card.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-background-radius: 3; " +
            "-fx-padding: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1); " +
            "-fx-cursor: hand;"
        );
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(60);

        // Labels at the top
        if (task.getLabels() != null && !task.getLabels().isEmpty()) {
            FlowPane labelsPane = new FlowPane(5, 5);
            labelsPane.setMaxWidth(Double.MAX_VALUE);
            for (String label : task.getLabels()) {
                Label labelTag = new Label(label);
                labelTag.setTextFill(Color.BLACK);
                labelTag.setStyle(
                    "-fx-background-color: #e4e6ea; " +
                    "-fx-background-radius: 3; " +
                    "-fx-padding: 2 6; " +
                    "-fx-font-size: 10px;"
                );
                labelsPane.getChildren().add(labelTag);
            }
            card.getChildren().add(labelsPane);
        }

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setTextFill(Color.BLACK);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(titleLabel);

        if (task.getDescription() != null && !task.getDescription().isEmpty() && !task.getDescription().equals("Description")) {
            Label descLabel = new Label(task.getDescription());
            descLabel.setTextFill(Color.rgb(51, 51, 51)); // Dark gray
            descLabel.setStyle("-fx-font-size: 12px; -fx-wrap-text: true;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(descLabel);
        }

        // Click handler to open task details (only if not dragging)
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && canViewTask(task)) {
                openTaskDetail(task);
            }
        });

        // Drag and drop handlers
        card.setOnDragDetected((MouseEvent event) -> {
            if (!canModifyTask(task)) {
                event.consume();
                return; // Read-only users can't drag
            }
            
            Dragboard dragboard = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // Use unique ID as identifier
            content.putString(task.getId());
            dragboard.setContent(content);
            card.setOpacity(0.5);
            event.consume();
        });

        card.setOnDragDone((DragEvent event) -> {
            card.setOpacity(1.0);
            // Remove any drop indicators
            VBox parent = (VBox) card.getParent();
            if (parent != null) {
                for (var child : parent.getChildren()) {
                    if (child.getStyleClass().contains("drop-indicator")) {
                        child.setStyle("");
                        child.getStyleClass().remove("drop-indicator");
                    }
                }
            }
            event.consume();
        });
        
        // Make card a drop target for reordering
        setupCardDropTarget(card, task);

        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 3; " +
                "-fx-padding: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2); " +
                "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 3; " +
                "-fx-padding: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1); " +
                "-fx-cursor: hand;"
            );
        });

        return card;
    }

    private void openTaskDetail(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/taskDetail.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            
            TaskDetailController controller = loader.getController();
            controller.setTask(task, tasks, this::refreshUI, this::refreshUI);
            
            Stage stage = new Stage();
            stage.setTitle("Task Details");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addTaskButton.getScene().getWindow());
            stage.setResizable(true);
            // Size to fit content, but with reasonable min/max
            stage.sizeToScene();
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            stage.setMaxWidth(800);
            stage.setMaxHeight(900);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAddTaskClicked() {
        if (!canModifyTask(null)) {
            return; // Read-only users can't add tasks
        }
        
        Task newTask = new Task("New Task", "Description", Task.Status.BACKLOG);
        tasks.add(newTask);
        StorageService.saveTasks(tasks);
        refreshUI();
        // Open task detail dialog for the new task
        openTaskDetail(newTask);
    }

    @FXML
    private void onManageUsersClicked() {
        if (!UserService.isAdmin()) {
            return;
        }

        try {
            // Create a new window for user management
            Stage userManagementStage = new Stage();
            userManagementStage.setTitle("User Management - Admin Only");
            userManagementStage.initModality(Modality.WINDOW_MODAL);
            userManagementStage.initOwner(logoutButton.getScene().getWindow());

            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setStyle("-fx-background-color: white;");

            Label titleLabel = new Label("User Management");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            root.getChildren().add(titleLabel);

            // Create table view for users
            TableView<User> userTable = new TableView<>();
            ObservableList<User> users = FXCollections.observableArrayList(UserService.getAllUsers());
            userTable.setItems(users);

            // Username column
            TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setPrefWidth(200);

            // Permission column
            TableColumn<User, User.Permission> permissionColumn = new TableColumn<>("Permission");
            permissionColumn.setCellValueFactory(new PropertyValueFactory<>("permission"));
            permissionColumn.setPrefWidth(150);

            // Permission combo box column
            TableColumn<User, User.Permission> changePermissionColumn = new TableColumn<>("Change Permission");
            changePermissionColumn.setPrefWidth(200);
            changePermissionColumn.setCellFactory(column -> new TableCell<User, User.Permission>() {
                private final ComboBox<User.Permission> comboBox = new ComboBox<>();

                {
                    comboBox.getItems().addAll(User.Permission.values());
                    comboBox.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (user == null) return;
                        
                        if (user.getUsername().equals("admin")) {
                            showAlert(Alert.AlertType.WARNING, "Warning", "The default admin account's permission cannot be changed.");
                            comboBox.setValue(user.getPermission());
                            return;
                        }
                        
                        if (user.getUsername().equals(UserService.getCurrentUser().getUsername())) {
                            showAlert(Alert.AlertType.WARNING, "Warning", "You cannot change your own permission.");
                            comboBox.setValue(user.getPermission());
                            return;
                        }
                        
                        User.Permission newPermission = comboBox.getValue();
                        if (newPermission != null && newPermission != user.getPermission()) {
                            if (UserService.updateUserPermission(user.getUsername(), newPermission)) {
                                // Update the user object in the list
                                user.setPermission(newPermission);
                                // Refresh the table to show updated data
                                userTable.refresh();
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Permission updated successfully for user: " + user.getUsername());
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update permission.");
                                // Reset combo box to current permission
                                comboBox.setValue(user.getPermission());
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(User.Permission item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        User user = getTableRow().getItem();
                        comboBox.setValue(user.getPermission());
                        // Disable if it's the current admin user or the default admin account
                        if (user.getUsername().equals(UserService.getCurrentUser().getUsername()) || 
                            user.getUsername().equals("admin")) {
                            comboBox.setDisable(true);
                            comboBox.setStyle("-fx-opacity: 0.5;");
                        } else {
                            comboBox.setDisable(false);
                            comboBox.setStyle("");
                        }
                        setGraphic(comboBox);
                    }
                }
            });

            userTable.getColumns().addAll(usernameColumn, permissionColumn, changePermissionColumn);
            userTable.setPrefHeight(400);

            // Info label
            Label infoLabel = new Label("Note: The default admin account (username: admin) and your own account cannot have their permissions changed. New users are registered with READ_ONLY permission by default.");
            infoLabel.setStyle("-fx-text-fill: #666; -fx-wrap-text: true;");
            infoLabel.setWrapText(true);

            // Close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> userManagementStage.close());
            closeButton.setStyle("-fx-background-color: #eb5a46; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

            root.getChildren().addAll(userTable, infoLabel, closeButton);

            Scene scene = new Scene(root, 600, 500);
            userManagementStage.setScene(scene);
            userManagementStage.setResizable(false);
            userManagementStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open user management window: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
