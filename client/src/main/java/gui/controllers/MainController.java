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
    @FXML private TableColumn<HumanBeingFx, Number> xCol;
    @FXML private TableColumn<HumanBeingFx, Number> yCol;
    @FXML private TableColumn<HumanBeingFx, Number> speedCol;
    @FXML private TableColumn<HumanBeingFx, Mood> moodCol;
    @FXML private TableColumn<HumanBeingFx, String> ownerCol;
    @FXML private SplitPane splitPane;

    @FXML private Label canvasTitleLabel;
    @FXML private Pane canvasPane;

    @FXML private Label statusLabel;

    private final CollectionStore store = new CollectionStore();
    private final Poller poller = new Poller(store);

    @FXML
    public void initialize() {
        bindLocalizedTexts();
        configureLanguageBox();
        configureUserLabel();
        configureTable();
        configureFilterAndSort();
        configureStatusBar();

        App.onClose(this::shutdown);
        poller.start();
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
        Localizer.bind(xCol.textProperty(), "table.x");
        Localizer.bind(yCol.textProperty(), "table.y");
        Localizer.bind(speedCol.textProperty(), "table.speed");
        Localizer.bind(moodCol.textProperty(), "table.mood");
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
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        speedCol.setCellValueFactory(new PropertyValueFactory<>("impactSpeed"));
        moodCol.setCellValueFactory(new PropertyValueFactory<>("mood"));
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerLogin"));

        xCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatDouble(v.doubleValue())));
        yCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatLong(v.longValue())));
        speedCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatDouble(v.doubleValue())));
        idCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatLong(v.longValue())));
        keyCol.setCellFactory(col -> numericCell(v -> LocalizedFormatter.formatLong(v.longValue())));

        moodCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Mood item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(LocaleManager.get().tr("mood." + item.name()));
            }
        });
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

    // ---- Handlers (заглушки на Шаге 6, реализация на Шагах 8-9) ----

    @FXML private void onAdd() {}
    @FXML private void onDelete() {}
    @FXML private void onClear() {}
    @FXML private void onInfo() {}
    @FXML private void onHelp() {}
    @FXML private void onScript() {}
    @FXML private void onHistory() {}
    @FXML private void onPrintAscending() {}
    @FXML private void onPrintFieldSpeed() {}
    @FXML private void onRemoveLower() {}
    @FXML private void onRemoveGreaterKey() {}
    @FXML private void onRemoveByMinutes() {}
}
