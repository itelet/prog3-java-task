package app.controllers;

import app.models.User;
import app.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    @FXML
    private void onRegisterClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long.");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        // Register with default READ_ONLY permission (only admin can change permissions)
        if (UserService.register(username, password)) {
            showSuccess("Registration successful! You can now login.");
            // Clear fields
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            showError("Username already exists. Please choose a different username.");
        }
    }

    @FXML
    private void onLoginClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 500);
            
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setResizable(false);
            
            // Close register window
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
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}

