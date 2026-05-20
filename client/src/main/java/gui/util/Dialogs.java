package gui.util;

import gui.i18n.LocaleManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Минимальные модальные диалоги в стиле приложения: информация, подтверждение,
 * ввод одной строки. Заголовки и кнопки берутся из ResourceBundle.
 */
public final class Dialogs {

    private Dialogs() {}

    public static void info(Window owner, String titleKey, String message) {
        showInfo(owner, LocaleManager.get().tr(titleKey), message);
    }

    public static void showInfo(Window owner, String title, String message) {
        Stage stage = baseStage(owner, title);
        VBox root = (VBox) stage.getScene().getRoot();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(360);
        Label content = new Label(message == null ? "" : message);
        content.setWrapText(true);
        content.getStyleClass().add("dialog-content");
        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(20));
        scroll.setContent(wrapper);
        scroll.getStyleClass().add("dialog-scroll");

        Button closeBtn = new Button(LocaleManager.get().tr("dialog.close"));
        closeBtn.getStyleClass().addAll("btn", "btn-primary");
        closeBtn.setPrefWidth(160);
        closeBtn.setOnAction(e -> stage.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));

        root.getChildren().addAll(scroll, footer);
        stage.show();
    }

    public static void confirm(Window owner, String questionKey, Runnable onConfirmed) {
        String question = LocaleManager.get().tr(questionKey);
        Stage stage = baseStage(owner, LocaleManager.get().tr("dialog.confirm.title"));
        VBox root = (VBox) stage.getScene().getRoot();

        Label content = new Label(question);
        content.setWrapText(true);
        content.getStyleClass().add("dialog-content");
        VBox body = new VBox(content);
        body.setPadding(new Insets(20));

        Button cancelBtn = new Button(LocaleManager.get().tr("dialog.cancel"));
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setPrefWidth(140);
        cancelBtn.setOnAction(e -> stage.close());

        Button okBtn = new Button(LocaleManager.get().tr("dialog.ok"));
        okBtn.getStyleClass().addAll("btn", "btn-accent");
        okBtn.setPrefWidth(140);
        okBtn.setOnAction(e -> { stage.close(); onConfirmed.run(); });

        HBox footer = new HBox(12, cancelBtn, okBtn);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));

        root.getChildren().addAll(body, footer);
        stage.show();
    }

    public static void prompt(Window owner, String titleKey, String labelKey,
                              Consumer<String> onConfirm) {
        Stage stage = baseStage(owner, LocaleManager.get().tr(titleKey));
        VBox root = (VBox) stage.getScene().getRoot();

        Label label = new Label(LocaleManager.get().tr(labelKey));
        label.getStyleClass().add("field-label");
        TextField input = new TextField();
        input.getStyleClass().add("text-input");
        VBox body = new VBox(8, label, input);
        body.setPadding(new Insets(20));

        Button cancelBtn = new Button(LocaleManager.get().tr("dialog.cancel"));
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setPrefWidth(140);
        cancelBtn.setOnAction(e -> stage.close());

        Button okBtn = new Button(LocaleManager.get().tr("dialog.ok"));
        okBtn.getStyleClass().addAll("btn", "btn-accent");
        okBtn.setPrefWidth(140);
        okBtn.setOnAction(e -> {
            String value = input.getText() == null ? "" : input.getText().trim();
            stage.close();
            onConfirm.accept(value);
        });

        HBox footer = new HBox(12, cancelBtn, okBtn);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));

        root.getChildren().addAll(body, footer);
        stage.show();
        input.requestFocus();
    }

    private static Stage baseStage(Window owner, String title) {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setMinWidth(520);
        stage.setMinHeight(220);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("form-title");
        StackPane header = new StackPane(titleLabel);
        header.getStyleClass().add("form-header");
        header.setPadding(new Insets(14, 20, 14, 20));
        StackPane.setAlignment(titleLabel, javafx.geometry.Pos.CENTER_LEFT);

        VBox root = new VBox(header);
        root.getStyleClass().add("form-root");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(Dialogs.class.getResource("/css/style.css"))
                        .toExternalForm());
        stage.setScene(scene);
        return stage;
    }

}
