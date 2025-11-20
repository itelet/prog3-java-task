package app.controllers;

import app.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (UserService.login(username, password)) {
            // Close login window and open main board
            openBoard();
        } else {
            showError("Invalid username or password.");
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/register.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(scene);
            stage.setResizable(false);
            
            // Close login window
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBoard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/board.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage stage = new Stage();
            stage.setTitle("Task Manager");
            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.setWidth(1400);
            stage.setHeight(700);
            stage.setResizable(true);
            
            // Close login window
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}

