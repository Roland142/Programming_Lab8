package gui.controllers;

import gui.Session;
import gui.i18n.LocaleManager;
import gui.i18n.Localizer;
import gui.model.HumanBeingFx;
import gui.util.LocalizedFormatter;
import gui.util.MoodColorMap;
import gui.util.UserColorAssigner;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class ObjectPopupController {

    @FXML private VBox headerBox;
    @FXML private Label nameLabel;
    @FXML private Label ownerLabel;
    @FXML private VBox footerBox;

    @FXML private Label idCaption;
    @FXML private Label keyCaption;
    @FXML private Label creationDateCaption;
    @FXML private Label coordsCaption;
    @FXML private Label speedCaption;
    @FXML private Label soundtrackCaption;
    @FXML private Label minutesCaption;
    @FXML private Label moodCaption;
    @FXML private Label carCaption;
    @FXML private Label heroCaption;
    @FXML private Label toothpickCaption;

    @FXML private Label idValue;
    @FXML private Label keyValue;
    @FXML private Label creationDateValue;
    @FXML private Label coordsValue;
    @FXML private Label speedValue;
    @FXML private Label soundtrackValue;
    @FXML private Label minutesValue;
    @FXML private Label moodValue;
    @FXML private Label carValue;
    @FXML private Label heroValue;
    @FXML private Label toothpickValue;

    @FXML private Button editButton;

    private HumanBeingFx target;
    private Consumer<HumanBeingFx> onEdit;
    private Stage stage;

    @FXML
    public void initialize() {
        Localizer.bind(idCaption.textProperty(), "popup.id");
        Localizer.bind(keyCaption.textProperty(), "popup.key");
        Localizer.bind(creationDateCaption.textProperty(), "popup.creationDate");
        Localizer.bind(coordsCaption.textProperty(), "popup.coords");
        Localizer.bind(speedCaption.textProperty(), "popup.speed");
        Localizer.bind(soundtrackCaption.textProperty(), "popup.soundtrack");
        Localizer.bind(minutesCaption.textProperty(), "popup.minutes");
        Localizer.bind(moodCaption.textProperty(), "popup.mood");
        Localizer.bind(carCaption.textProperty(), "popup.car");
        Localizer.bind(heroCaption.textProperty(), "popup.real_hero");
        Localizer.bind(toothpickCaption.textProperty(), "popup.toothpick");
        Localizer.bind(editButton.textProperty(), "popup.edit");
    }

    public void setObject(HumanBeingFx fx) {
        this.target = fx;
        nameLabel.setText(fx.getName());
        ownerLabel.setText(fx.getOwnerLogin());
        idValue.setText(LocalizedFormatter.formatLong(fx.getId()));
        keyValue.setText(LocalizedFormatter.formatLong(fx.getKey()));
        creationDateValue.setText(LocalizedFormatter.formatDate(fx.getCreationDate()));
        coordsValue.setText(LocalizedFormatter.formatCoordinates(fx.getX(), fx.getY()));
        speedValue.setText(LocalizedFormatter.formatDouble(fx.getImpactSpeed()));
        soundtrackValue.setText(fx.getSoundtrackName());
        minutesValue.setText(LocalizedFormatter.formatInteger(fx.getMinutesOfWaiting()));
        if (fx.getMood() != null) {
            moodValue.setText(LocaleManager.get().tr("mood." + fx.getMood().name()));
            moodValue.setStyle("-fx-text-fill: " + MoodColorMap.hexFor(fx.getMood()) + ";");
        } else {
            moodValue.setText(LocaleManager.get().tr("popup.dash"));
            moodValue.setStyle("");
        }
        carValue.setText(fx.getCarName() != null ? fx.getCarName() : LocaleManager.get().tr("popup.dash"));
        boolean hero = fx.isRealHero();
        heroValue.setText(LocaleManager.get().tr(hero ? "popup.yes" : "popup.no"));
        heroValue.setStyle(hero ? "-fx-text-fill: #5d8b6a;" : "-fx-text-fill: #b9b1a3;");
        Boolean toothpick = fx.getHasToothpick();
        toothpickValue.setText(toothpick == null
                ? LocaleManager.get().tr("popup.dash")
                : LocaleManager.get().tr(toothpick ? "popup.yes" : "popup.no"));

        String ownerHex = UserColorAssigner.hexFor(fx.getOwnerLogin());
        headerBox.setStyle("-fx-background-color: " + ownerHex + ";");

        boolean isOwn = Objects.equals(fx.getOwnerLogin(), Session.get().context().getLogin());
        footerBox.setVisible(isOwn);
        footerBox.setManaged(isOwn);
    }

    public void setOnEdit(Consumer<HumanBeingFx> handler) {
        this.onEdit = handler;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onEdit() {
        if (onEdit != null && target != null) onEdit.accept(target);
        if (stage != null) stage.close();
    }

    /** Открывает popup рядом с указанной точкой экрана. */
    public static void show(Window owner, double screenX, double screenY,
                            HumanBeingFx fx, Consumer<HumanBeingFx> onEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(ObjectPopupController.class
                            .getResource("/fxml/object-popup.fxml")));
            Parent root = loader.load();
            ObjectPopupController controller = loader.getController();

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initOwner(owner);
            stage.initModality(Modality.NONE);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(ObjectPopupController.class
                            .getResource("/css/style.css")).toExternalForm());
            stage.setScene(scene);
            stage.setX(screenX);
            stage.setY(screenY);

            controller.setStage(stage);
            controller.setObject(fx);
            controller.setOnEdit(onEdit);

            stage.show();
            // закрытие при потере фокуса
            stage.focusedProperty().addListener((obs, prev, focused) -> {
                if (!focused) stage.close();
            });
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть popup", e);
        }
    }
}
