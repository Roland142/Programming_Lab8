package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        stage.setTitle("Коллекция объектов");
        stage.setOnCloseRequest(e -> Session.get().gateway().close());

        if (!Session.get().gateway().connect()) {
            System.err.println("Не удалось подключиться к серверу localhost. " +
                    "Запустите сервер и перезапустите клиент.");
        }

        showLogin();
        stage.show();
    }

    public static void showLogin() {
        loadScene("/fxml/login.fxml");
    }

    public static void showMain() {
        loadScene("/fxml/main.fxml");
    }

    private static void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(App.class.getResource(fxmlPath),
                            "Не найден ресурс " + fxmlPath));
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, 780, 780);
                scene.getStylesheets().add(
                        Objects.requireNonNull(App.class.getResource("/css/style.css"),
                                "Не найден ресурс /css/style.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить " + fxmlPath, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
