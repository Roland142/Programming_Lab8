package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(App.class.getResource("/fxml/login.fxml"),
                        "Не найден ресурс /fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 780, 760);
        stage.setTitle("Коллекция объектов");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
