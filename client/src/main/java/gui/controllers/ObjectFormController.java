package gui.controllers;

import elements.HumanBeing;
import elements.Mood;
import exceptions.InvalidDataException;
import gui.Session;
import gui.i18n.LocaleManager;
import gui.i18n.Localizer;
import gui.model.HumanBeingFx;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import network.Request;
import network.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * Контроллер диалога insert/edit. Один класс обслуживает оба FXML —
 * insert-dialog.fxml содержит дополнительное поле "Ключ", в edit-dialog.fxml
 * это поле отсутствует (соответствующие @FXML-ссылки будут null).
 */
public class ObjectFormController {

    public enum Mode { INSERT, EDIT }

    @FXML private Label titleLabel;
    @FXML private Button closeButton;
    @FXML private Label nameLabel;
    @FXML private TextField nameField;
    @FXML private Label xLabel;
    @FXML private TextField xField;
    @FXML private Label yLabel;
    @FXML private TextField yField;
    @FXML private Label speedLabel;
    @FXML private TextField speedField;
    @FXML private Label soundtrackLabel;
    @FXML private TextField soundtrackField;
    @FXML private Label minutesLabel;
    @FXML private TextField minutesField;
    @FXML private Label moodLabel;
    @FXML private ComboBox<Mood> moodCombo;
    @FXML private Label carLabel;
    @FXML private TextField carField;
    @FXML private CheckBox realHeroCheck;
    @FXML private Label toothpickLabel;
    @FXML private ComboBox<NullableBoolean> toothpickCombo;
    @FXML private Label keyLabel;       // только для insert
    @FXML private TextField keyField;   // только для insert
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    @FXML private HBox errorBox;
    @FXML private Label errorLabel;

    private Mode mode = Mode.INSERT;
    private HumanBeingFx editingFx;
    private Stage stage;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        Localizer.bind(nameLabel.textProperty(), "field.name");
        Localizer.bind(xLabel.textProperty(), "field.x");
        Localizer.bind(yLabel.textProperty(), "field.y");
        Localizer.bind(speedLabel.textProperty(), "field.speed");
        Localizer.bind(soundtrackLabel.textProperty(), "field.soundtrack");
        Localizer.bind(minutesLabel.textProperty(), "field.minutes");
        Localizer.bind(moodLabel.textProperty(), "field.mood");
        Localizer.bind(carLabel.textProperty(), "field.car");
        Localizer.bind(realHeroCheck.textProperty(), "field.realHero");
        Localizer.bind(toothpickLabel.textProperty(), "field.toothpick");
        Localizer.bind(cancelButton.textProperty(), "edit.cancel");
        Localizer.bind(saveButton.textProperty(), "edit.save");

        if (keyLabel != null) {
            Localizer.bind(keyLabel.textProperty(), "field.key");
        }

        moodCombo.getItems().add(null);
        moodCombo.getItems().addAll(Mood.values());
        moodCombo.setCellFactory(lv -> moodListCell());
        moodCombo.setButtonCell(moodListCell());

        toothpickCombo.getItems().setAll(NullableBoolean.values());
        toothpickCombo.setCellFactory(lv -> nullableBooleanListCell());
        toothpickCombo.setButtonCell(nullableBooleanListCell());
        toothpickCombo.setValue(NullableBoolean.UNSPECIFIED);

        errorBox.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
        errorBox.managedProperty().bind(errorBox.visibleProperty());
    }

    private ListCell<Mood> moodListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Mood item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                if (item == null) { setText(LocaleManager.get().tr("popup.dash")); return; }
                setText(LocaleManager.get().tr("mood." + item.name()));
            }
        };
    }

    private ListCell<NullableBoolean> nullableBooleanListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(NullableBoolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : LocaleManager.get().tr(item.key));
            }
        };
    }

    public void configure(Mode mode, HumanBeingFx fx) {
        this.mode = mode;
        this.editingFx = fx;

        if (mode == Mode.INSERT) {
            Localizer.bind(titleLabel.textProperty(), "insert.title");
            realHeroCheck.setSelected(true);
            toothpickCombo.setValue(NullableBoolean.UNSPECIFIED);
            moodCombo.setValue(null);
        } else {
            Localizer.bind(titleLabel.textProperty(), "edit.title");
            populateFromFx(fx);
        }
    }

    private void populateFromFx(HumanBeingFx fx) {
        nameField.setText(fx.getName());
        xField.setText(String.valueOf(fx.getX()));
        yField.setText(String.valueOf(fx.getY()));
        speedField.setText(String.valueOf(fx.getImpactSpeed()));
        soundtrackField.setText(fx.getSoundtrackName());
        minutesField.setText(String.valueOf(fx.getMinutesOfWaiting()));
        moodCombo.setValue(fx.getMood());
        carField.setText(fx.getCarName() != null ? fx.getCarName() : "");
        realHeroCheck.setSelected(fx.isRealHero());
        toothpickCombo.setValue(NullableBoolean.from(fx.getHasToothpick()));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void onCancel() {
        if (stage != null) stage.close();
    }

    @FXML
    private void onSave() {
        clearError();
        try {
            HumanBeing hb = buildHumanBeingFromFields();
            Request request = (mode == Mode.INSERT) ? buildInsertRequest(hb) : buildUpdateRequest(hb);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);
            var task = Session.get().gateway().sendTask(request);
            task.setOnSucceeded(e -> {
                Response response = task.getValue();
                if (response.isSuccess()) {
                    if (onSaved != null) onSaved.run();
                    if (stage != null) stage.close();
                } else {
                    saveButton.setDisable(false);
                    cancelButton.setDisable(false);
                    errorLabel.setText(response.getMessage());
                }
            });
            task.setOnFailed(e -> {
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                errorLabel.setText(LocaleManager.get().tr("login.error.server"));
            });
            new Thread(task, "save-task").start();
        } catch (FieldValidationException ex) {
            errorLabel.setText(ex.getLocalizedMessage());
        }
    }

    private void clearError() {
        errorLabel.setText("");
    }

    private HumanBeing buildHumanBeingFromFields() throws FieldValidationException {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String soundtrack = soundtrackField.getText() == null ? "" : soundtrackField.getText().trim();
        if (name.isEmpty() || soundtrack.isEmpty()) {
            throw new FieldValidationException("edit.error.empty");
        }

        double x;
        int y;
        double speed;
        int minutes;
        try {
            x = Double.parseDouble(xField.getText().trim().replace(',', '.'));
            y = Integer.parseInt(yField.getText().trim());
            speed = Double.parseDouble(speedField.getText().trim().replace(',', '.'));
            minutes = Integer.parseInt(minutesField.getText().trim());
        } catch (NumberFormatException nfe) {
            throw new FieldValidationException("edit.error.number");
        }
        if (x <= -975) throw new FieldValidationException("edit.error.x");

        Mood mood = moodCombo.getValue();
        NullableBoolean toothpick = toothpickCombo.getValue();
        String carName = carField.getText() == null ? "" : carField.getText().trim();
        elements.Car car = carName.isEmpty() ? null : new elements.Car(carName);

        try {
            HumanBeing hb = new HumanBeing(
                    name,
                    new elements.Coordinates(x, y),
                    realHeroCheck.isSelected(),
                    toothpick == null ? null : toothpick.value,
                    speed,
                    soundtrack,
                    minutes,
                    mood,
                    car);
            if (mode == Mode.EDIT && editingFx != null) {
                hb.setId(editingFx.getId());
                hb.setCreationDate(editingFx.getCreationDate());
            }
            return hb;
        } catch (InvalidDataException ide) {
            throw new FieldValidationException(ide.getMessage(), false);
        }
    }

    private Request buildInsertRequest(HumanBeing hb) throws FieldValidationException {
        if (keyField == null) throw new FieldValidationException("edit.error.key");
        long key;
        try {
            key = Long.parseLong(keyField.getText().trim());
        } catch (NumberFormatException nfe) {
            throw new FieldValidationException("edit.error.key");
        }
        return new Request("insert", new String[]{ String.valueOf(key) }, hb,
                Session.get().context().getLogin(), Session.get().context().getPasswordHash());
    }

    private Request buildUpdateRequest(HumanBeing hb) {
        return new Request("update", new String[]{ String.valueOf(editingFx.getId()) }, hb,
                Session.get().context().getLogin(), Session.get().context().getPasswordHash());
    }

    /** Открывает insert-диалог как modal stage. */
    public static void openInsert(Window owner, Runnable onSaved) {
        open(owner, "/fxml/insert-dialog.fxml", Mode.INSERT, null, onSaved);
    }

    /** Открывает edit-диалог как modal stage. */
    public static void openEdit(Window owner, HumanBeingFx fx, Runnable onSaved) {
        open(owner, "/fxml/edit-dialog.fxml", Mode.EDIT, fx, onSaved);
    }

    private static void open(Window owner, String fxml, Mode mode,
                             HumanBeingFx fx, Runnable onSaved) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(ObjectFormController.class.getResource(fxml)));
            Parent root = loader.load();
            ObjectFormController controller = loader.getController();

            Stage stage = new Stage(StageStyle.UTILITY);
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(ObjectFormController.class
                            .getResource("/css/style.css")).toExternalForm());
            stage.setScene(scene);

            controller.setStage(stage);
            controller.configure(mode, fx);
            controller.setOnSaved(onSaved);

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось открыть диалог " + fxml, e);
        }
    }

    /** Внутреннее исключение валидации, несущее ключ ResourceBundle или готовое сообщение. */
    private static class FieldValidationException extends Exception {
        private final boolean isKey;
        FieldValidationException(String key) { this(key, true); }
        FieldValidationException(String text, boolean isKey) {
            super(text);
            this.isKey = isKey;
        }
        @Override
        public String getLocalizedMessage() {
            return isKey ? LocaleManager.get().tr(getMessage()) : getMessage();
        }
    }

    private enum NullableBoolean {
        YES(Boolean.TRUE, "popup.yes"),
        NO(Boolean.FALSE, "popup.no"),
        UNSPECIFIED(null, "popup.dash");

        private final Boolean value;
        private final String key;

        NullableBoolean(Boolean value, String key) {
            this.value = value;
            this.key = key;
        }

        private static NullableBoolean from(Boolean value) {
            if (value == null) return UNSPECIFIED;
            return value ? YES : NO;
        }
    }
}
