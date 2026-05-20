package gui.controllers;

import elements.Mood;
import gui.App;
import gui.Session;
import gui.i18n.LocaleManager;
import gui.i18n.Localizer;
import gui.model.CollectionStore;
import gui.model.HumanBeingFx;
import gui.net.Poller;
import gui.util.LocalizedFormatter;
import gui.view.CollectionCanvas;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.Date;
import java.util.Locale;

public class MainController {

    @FXML private Label appTitleLabel;
    @FXML private Label userLabel;
    @FXML private ComboBox<Locale> languageBox;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button infoButton;
    @FXML private Button helpButton;
    @FXML private Button scriptButton;
    @FXML private Button historyButton;
    @FXML private MenuButton moreMenuButton;
    @FXML private MenuItem printAscendingItem;
    @FXML private MenuItem printSpeedItem;
    @FXML private MenuItem removeLowerItem;
    @FXML private MenuItem removeGreaterKeyItem;
    @FXML private MenuItem removeByMinutesItem;

    @FXML private TextField filterField;
    @FXML private ComboBox<TableBinder.FilterColumn> filterColumnBox;
    @FXML private TableView<HumanBeingFx> objectsTable;
    @FXML private TableColumn<HumanBeingFx, Number> idCol;
    @FXML private TableColumn<HumanBeingFx, Number> keyCol;
    @FXML private TableColumn<HumanBeingFx, String> nameCol;
    @FXML private TableColumn<HumanBeingFx, Date> creationDateCol;
    @FXML private TableColumn<HumanBeingFx, Number> xCol;
    @FXML private TableColumn<HumanBeingFx, Number> yCol;
    @FXML private TableColumn<HumanBeingFx, Boolean> realHeroCol;
    @FXML private TableColumn<HumanBeingFx, Boolean> hasToothpickCol;
    @FXML private TableColumn<HumanBeingFx, Number> speedCol;
    @FXML private TableColumn<HumanBeingFx, String> soundtrackCol;
    @FXML private TableColumn<HumanBeingFx, Number> minutesCol;
    @FXML private TableColumn<HumanBeingFx, Mood> moodCol;
    @FXML private TableColumn<HumanBeingFx, String> carCol;
    @FXML private TableColumn<HumanBeingFx, String> ownerCol;
    @FXML private SplitPane splitPane;

    @FXML private Label canvasTitleLabel;
    @FXML private Pane canvasPane;

    @FXML private Label statusLabel;

    private final CollectionStore store = new CollectionStore();
    private final Poller poller = new Poller(store);
    private CollectionCanvas collectionCanvas;
    private TableBinder tableBinder;
    private ScriptExecutor scriptExecutor;

    @FXML
    public void initialize() {
        bindLocalizedTexts();
        configureLanguageBox();
        configureUserLabel();
        configureCanvas();
        configureTable();
        scriptExecutor = new ScriptExecutor(this::window, this::triggerImmediateSync);
        configureStatusBar();

        // Кастомные cellFactory не пересоздаются при смене локали — заставим таблицу
        // перерисовать ячейки, чтобы локализованные mood и числа обновились.
        LocaleManager.get().localeProperty().addListener((obs, prev, value) -> {
            tableBinder.refresh();
            objectsTable.refresh();
        });

        App.onClose(this::shutdown);
        poller.start();
    }

    private void configureCanvas() {
        collectionCanvas = new CollectionCanvas(store);
        collectionCanvas.prefWidthProperty().bind(canvasPane.widthProperty());
        collectionCanvas.prefHeightProperty().bind(canvasPane.heightProperty());
        canvasPane.getChildren().add(collectionCanvas);

        collectionCanvas.setOnObjectClick(this::showObjectPopup);
    }

    private void showObjectPopup(HumanBeingFx fx) {
        if (collectionCanvas.getScene() == null) return;
        var window = collectionCanvas.getScene().getWindow();
        double x = window.getX() + window.getWidth() / 2 - 190;
        double y = window.getY() + window.getHeight() / 2 - 200;
        ObjectPopupController.show(window, x, y, fx, this::openEditDialog);
    }

    private void openEditDialog(HumanBeingFx fx) {
        if (collectionCanvas.getScene() == null) return;
        ObjectFormController.openEdit(
                collectionCanvas.getScene().getWindow(), fx, this::triggerImmediateSync);
    }

    private void openInsertDialog() {
        if (collectionCanvas.getScene() == null) return;
        ObjectFormController.openInsert(
                collectionCanvas.getScene().getWindow(), this::triggerImmediateSync);
    }

    /** После успешной операции мутации — перезапускаем Poller, чтобы не ждать 3 секунды. */
    private void triggerImmediateSync() {
        poller.restart();
    }

    private void bindLocalizedTexts() {
        Localizer.bind(appTitleLabel.textProperty(), "app.title");
        Localizer.bind(canvasTitleLabel.textProperty(), "canvas.title");

        Localizer.bind(addButton.textProperty(), "toolbar.add");
        Localizer.bind(deleteButton.textProperty(), "toolbar.delete");
        Localizer.bind(clearButton.textProperty(), "toolbar.clear");
        Localizer.bind(infoButton.textProperty(), "toolbar.info");
        Localizer.bind(helpButton.textProperty(), "toolbar.help");
        Localizer.bind(scriptButton.textProperty(), "toolbar.script");
        Localizer.bind(historyButton.textProperty(), "toolbar.history");
        Localizer.bind(moreMenuButton.textProperty(), "toolbar.more");

        Localizer.bind(printAscendingItem.textProperty(), "toolbar.print_ascending");
        Localizer.bind(printSpeedItem.textProperty(), "toolbar.print_field_speed");
        Localizer.bind(removeLowerItem.textProperty(), "toolbar.remove_lower");
        Localizer.bind(removeGreaterKeyItem.textProperty(), "toolbar.remove_greater_key");
        Localizer.bind(removeByMinutesItem.textProperty(), "toolbar.remove_by_minutes");

        filterField.promptTextProperty().bind(Localizer.binding("filter.placeholder"));

        Localizer.bind(idCol.textProperty(), "table.id");
        Localizer.bind(keyCol.textProperty(), "table.key");
        Localizer.bind(nameCol.textProperty(), "table.name");
        Localizer.bind(creationDateCol.textProperty(), "table.creationDate");
        Localizer.bind(xCol.textProperty(), "table.x");
        Localizer.bind(yCol.textProperty(), "table.y");
        Localizer.bind(realHeroCol.textProperty(), "table.realHero");
        Localizer.bind(hasToothpickCol.textProperty(), "table.hasToothpick");
        Localizer.bind(speedCol.textProperty(), "table.speed");
        Localizer.bind(soundtrackCol.textProperty(), "table.soundtrack");
        Localizer.bind(minutesCol.textProperty(), "table.minutes");
        Localizer.bind(moodCol.textProperty(), "table.mood");
        Localizer.bind(carCol.textProperty(), "table.car");
        Localizer.bind(ownerCol.textProperty(), "table.owner");
    }

    private void configureLanguageBox() {
        languageBox.getItems().setAll(LocaleManager.SUPPORTED);
        languageBox.setValue(LocaleManager.get().getLocale());
        languageBox.setCellFactory(lv -> shortLocaleCell());
        languageBox.setButtonCell(shortLocaleCell());
        languageBox.valueProperty().addListener((obs, prev, value) -> {
            if (value != null) {
                LocaleManager.get().setLocale(value);
                languageBox.setButtonCell(shortLocaleCell());
            }
        });
    }

    private ListCell<Locale> shortLocaleCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                if (item.getCountry() != null && !item.getCountry().isBlank()) {
                    setText(item.getLanguage().toUpperCase() + "-" + item.getCountry());
                } else {
                    setText(item.getLanguage().toUpperCase());
                }
            }
        };
    }

    private void configureUserLabel() {
        String login = Session.get().context().getLogin();
        userLabel.textProperty().bind(
                Localizer.binding("main.user", login == null ? "" : login));
    }

    private void configureTable() {
        tableBinder = new TableBinder(store, objectsTable, filterField, filterColumnBox,
                idCol, keyCol, nameCol, creationDateCol, xCol, yCol, realHeroCol,
                hasToothpickCol, speedCol, soundtrackCol, minutesCol, moodCol, carCol,
                ownerCol, this::openEditDialog);
        tableBinder.configure();
    }

    private void configureStatusBar() {
        IntegerBinding sizeBinding = Bindings.size(store.items());
        statusLabel.textProperty().bind(Bindings.createStringBinding(
                () -> LocaleManager.get().tr("main.objects",
                        LocalizedFormatter.formatInteger(sizeBinding.get())),
                LocaleManager.get().localeProperty(), sizeBinding));
    }

    public void shutdown() {
        poller.cancel();
        Session.get().gateway().close();
    }

    // ============================================================
    // Команды тулбара
    // ============================================================

    private javafx.stage.Window window() {
        return objectsTable.getScene().getWindow();
    }

    private void runCommand(String commandName, String[] args, elements.HumanBeing hb,
                            java.util.function.Consumer<network.Response> onResponse) {
        var ctx = Session.get().context();
        network.Request request = new network.Request(commandName, args, hb,
                ctx.getLogin(), ctx.getPasswordHash());
        var task = Session.get().gateway().sendTask(request);
        task.setOnSucceeded(e -> onResponse.accept(task.getValue()));
        task.setOnFailed(e -> gui.util.Dialogs.info(window(), "login.error.server",
                LocaleManager.get().tr("login.error.server")));
        new Thread(task, "cmd-" + commandName).start();
    }

    @FXML private void onAdd() { openInsertDialog(); }

    @FXML
    private void onDelete() {
        HumanBeingFx selected = objectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!selected.getOwnerLogin().equals(Session.get().context().getLogin())) {
            gui.util.Dialogs.info(window(), "dialog.confirm.title",
                    LocaleManager.get().tr("edit.error.no_perm"));
            return;
        }
        gui.util.Dialogs.confirm(window(), "dialog.confirm.delete", () ->
                runCommand("remove_key", new String[]{ String.valueOf(selected.getKey()) }, null,
                        response -> {
                            if (response.isSuccess()) triggerImmediateSync();
                            else gui.util.Dialogs.info(window(), "dialog.confirm.title",
                                    response.getMessage());
                        }));
    }

    @FXML
    private void onClear() {
        gui.util.Dialogs.confirm(window(), "dialog.confirm.clear", () ->
                runCommand("clear", new String[0], null, response -> {
                    triggerImmediateSync();
                    gui.util.Dialogs.info(window(), "dialog.confirm.title", response.getMessage());
                }));
    }

    @FXML
    private void onInfo() {
        runCommand("info", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "info.title", CommandResponseFormatter.formatInfo(response)));
    }

    @FXML
    private void onHelp() {
        runCommand("help", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "help.title", CommandResponseFormatter.formatHelp(response)));
    }

    @FXML
    private void onHistory() {
        runCommand("history", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "history.title", CommandResponseFormatter.formatHistory(response)));
    }

    @FXML
    private void onScript() {
        scriptExecutor.chooseAndRun();
    }


    @FXML
    private void onPrintAscending() {
        runCommand("print_ascending", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "toolbar.print_ascending", response.getMessage()));
    }

    @FXML
    private void onPrintFieldSpeed() {
        runCommand("print_field_ascending_impact_speed", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "toolbar.print_field_speed", response.getMessage()));
    }

    @FXML
    private void onRemoveLower() {
        ObjectFormController.openRemoveLower(window(), this::triggerImmediateSync);
    }

    @FXML
    private void onRemoveGreaterKey() {
        gui.util.Dialogs.prompt(window(), "toolbar.remove_greater_key", "field.key", value -> {
            try {
                Long.parseLong(value);
                runCommand("remove_greater_key", new String[]{ value }, null, response -> {
                    triggerImmediateSync();
                    gui.util.Dialogs.info(window(), "toolbar.remove_greater_key", response.getMessage());
                });
            } catch (NumberFormatException ex) {
                gui.util.Dialogs.info(window(), "toolbar.remove_greater_key",
                        LocaleManager.get().tr("edit.error.key"));
            }
        });
    }

    @FXML
    private void onRemoveByMinutes() {
        gui.util.Dialogs.prompt(window(), "toolbar.remove_by_minutes", "field.minutes", value -> {
            try {
                Integer.parseInt(value);
                runCommand("remove_all_by_minutes_of_waiting", new String[]{ value }, null, response -> {
                    triggerImmediateSync();
                    gui.util.Dialogs.info(window(), "toolbar.remove_by_minutes", response.getMessage());
                });
            } catch (NumberFormatException ex) {
                gui.util.Dialogs.info(window(), "toolbar.remove_by_minutes",
                        LocaleManager.get().tr("edit.error.number"));
            }
        });
    }
}
