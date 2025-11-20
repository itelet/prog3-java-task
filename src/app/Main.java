package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Show login screen first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/login.fxml"));
        Scene scene = new Scene(loader.load(), 400, 500);
        stage.setTitle("Login - Task Manager");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
