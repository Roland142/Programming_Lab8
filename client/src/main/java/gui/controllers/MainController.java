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
import gui.util.UserColorAssigner;
import gui.view.CollectionCanvas;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.util.Date;
import java.util.Locale;
import java.util.function.Predicate;

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

    @FXML
    public void initialize() {
        bindLocalizedTexts();
        configureLanguageBox();
        configureUserLabel();
        configureTable();
        configureFilterAndSort();
        configureCanvas();
        configureStatusBar();

        // Кастомные cellFactory не пересоздаются при смене локали — заставим таблицу
        // перерисовать ячейки, чтобы локализованные mood и числа обновились.
        LocaleManager.get().localeProperty().addListener((obs, prev, value) -> objectsTable.refresh());

        App.onClose(this::shutdown);
        poller.start();
    }

    private void configureCanvas() {
        collectionCanvas = new CollectionCanvas(store);
        collectionCanvas.prefWidthProperty().bind(canvasPane.widthProperty());
        collectionCanvas.prefHeightProperty().bind(canvasPane.heightProperty());
        canvasPane.getChildren().add(collectionCanvas);

        collectionCanvas.setOnObjectClick(this::showObjectPopup);

        // Двойной клик в таблице по своему объекту → редактирование (Шаг 8).
        objectsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<HumanBeingFx> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    HumanBeingFx fx = row.getItem();
                    if (fx.getOwnerLogin().equals(Session.get().context().getLogin())) {
                        openEditDialog(fx);
                    }
                }
            });
            return row;
        });

        // Раскраска ячеек владельца и настроения (текстовый цвет)
        ownerCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("-fx-text-fill: " + UserColorAssigner.hexFor(item) + "; -fx-font-weight: bold;");
            }
        });

        moodCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Mood item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(LocaleManager.get().tr("mood." + item.name()));
                setStyle("-fx-text-fill: " + gui.util.MoodColorMap.hexFor(item) + ";");
            }
        });
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
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        creationDateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        realHeroCol.setCellValueFactory(new PropertyValueFactory<>("realHero"));
        hasToothpickCol.setCellValueFactory(new PropertyValueFactory<>("hasToothpick"));
        speedCol.setCellValueFactory(new PropertyValueFactory<>("impactSpeed"));
        soundtrackCol.setCellValueFactory(new PropertyValueFactory<>("soundtrackName"));
        minutesCol.setCellValueFactory(new PropertyValueFactory<>("minutesOfWaiting"));
        moodCol.setCellValueFactory(new PropertyValueFactory<>("mood"));
        carCol.setCellValueFactory(new PropertyValueFactory<>("carName"));
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerLogin"));

        xCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatDouble(v.doubleValue())));
        yCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatLong(v.longValue())));
        speedCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatDouble(v.doubleValue())));
        idCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatLong(v.longValue())));
        keyCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatLong(v.longValue())));
        minutesCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatInteger(v.intValue())));
        creationDateCol.setCellFactory(col -> dateCell());
        realHeroCol.setCellFactory(col -> booleanCell(false));
        hasToothpickCol.setCellFactory(col -> booleanCell(true));

        // Цветной cellFactory для moodCol и ownerCol устанавливается в configureCanvas.
    }

    private <T extends Number> javafx.scene.control.TableCell<HumanBeingFx, Number>
            numericCell(java.util.function.Function<Number, String> formatter) {
        return new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.apply(item));
            }
        };
    }

    private javafx.scene.control.TableCell<HumanBeingFx, Date> dateCell() {
        return new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : LocalizedFormatter.formatDate(item));
            }
        };
    }

    private javafx.scene.control.TableCell<HumanBeingFx, Boolean> booleanCell(boolean nullable) {
        return new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                if (item == null) {
                    setText(nullable ? LocaleManager.get().tr("popup.dash") : null);
                    return;
                }
                setText(LocaleManager.get().tr(item ? "popup.yes" : "popup.no"));
            }
        };
    }

    private void configureFilterAndSort() {
        FilteredList<HumanBeingFx> filtered = new FilteredList<>(store.items(), x -> true);
        filterField.textProperty().addListener((obs, prev, value) -> filtered.setPredicate(buildFilter(value)));

        SortedList<HumanBeingFx> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(objectsTable.comparatorProperty());
        objectsTable.setItems(sorted);
    }

    /** Предикат фильтрации, построенный через Streams API внутри HumanBeingFx.matchesFilter. */
    private Predicate<HumanBeingFx> buildFilter(String text) {
        return fx -> fx.matchesFilter(text);
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
                gui.util.Dialogs.info(window(), "info.title", response.getMessage()));
    }

    @FXML
    private void onHelp() {
        runCommand("help", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "help.title", response.getMessage()));
    }

    @FXML
    private void onHistory() {
        runCommand("history", new String[0], null, response ->
                gui.util.Dialogs.info(window(), "history.title", response.getMessage()));
    }

    @FXML
    private void onScript() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle(LocaleManager.get().tr("script.choose"));
        java.io.File file = chooser.showOpenDialog(window());
        if (file == null) return;

        java.util.List<String> lines;
        try {
            lines = java.nio.file.Files.readAllLines(file.toPath());
        } catch (java.io.IOException ex) {
            gui.util.Dialogs.info(window(), "script.title", ex.getMessage());
            return;
        }
        if (lines.isEmpty()) {
            gui.util.Dialogs.info(window(), "script.title",
                    LocaleManager.get().tr("script.empty"));
            return;
        }

        Thread runner = new Thread(() -> {
            StringBuilder log = new StringBuilder();
            int executed = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                String cmd = parts[0];
                String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);
                if (!isStatelessCommand(cmd)) {
                    log.append(LocaleManager.get().tr("script.line", i + 1,
                            "skip — " + cmd + " (требует объект)"))
                       .append('\n');
                    continue;
                }
                try {
                    var ctx = Session.get().context();
                    network.Request req = new network.Request(cmd, args, null,
                            ctx.getLogin(), ctx.getPasswordHash());
                    network.Response resp = Session.get().gateway().sendBlocking(req);
                    log.append(LocaleManager.get().tr("script.line", i + 1, resp.getMessage()))
                       .append('\n');
                    executed++;
                } catch (Exception ex) {
                    log.append(LocaleManager.get().tr("script.line", i + 1, ex.getMessage()))
                       .append('\n');
                }
            }
            int finalExecuted = executed;
            String message = LocaleManager.get().tr("script.executed", finalExecuted)
                    + "\n\n" + log;
            javafx.application.Platform.runLater(() -> {
                triggerImmediateSync();
                gui.util.Dialogs.info(window(), "script.title", message);
            });
        }, "script-runner");
        runner.setDaemon(true);
        runner.start();
    }

    private static boolean isStatelessCommand(String cmd) {
        return java.util.Set.of(
                "info", "help", "history", "show", "clear",
                "remove_key", "remove_greater_key",
                "remove_all_by_minutes_of_waiting",
                "print_ascending", "print_field_ascending_impact_speed"
        ).contains(cmd);
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
        gui.util.Dialogs.prompt(window(), "toolbar.remove_lower", "field.name", value -> {
            try {
                elements.HumanBeing dummy = new elements.HumanBeing(
                        value, new elements.Coordinates(0.0, 0), Boolean.TRUE,
                        null, 0.0, "stub", 0, null, null);
                runCommand("remove_lower", new String[0], dummy, response -> {
                    triggerImmediateSync();
                    gui.util.Dialogs.info(window(), "toolbar.remove_lower", response.getMessage());
                });
            } catch (Exception ex) {
                gui.util.Dialogs.info(window(), "toolbar.remove_lower", ex.getMessage());
            }
        });
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
