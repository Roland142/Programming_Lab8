package gui.controllers;

import elements.Mood;
import gui.Session;
import gui.i18n.LocaleManager;
import gui.i18n.Localizer;
import gui.model.CollectionStore;
import gui.model.HumanBeingFx;
import gui.util.LocalizedFormatter;
import gui.util.MoodColorMap;
import gui.util.UserColorAssigner;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class TableBinder {
    private final CollectionStore store;
    private final ObservableList<HumanBeingFx> visibleItems = FXCollections.observableArrayList();
    private final Map<HumanBeingFx, InvalidationListener> itemRefreshListeners = new HashMap<>();
    private final TableView<HumanBeingFx> table;
    private final TextField filterField;
    private final ComboBox<FilterColumn> filterColumnBox;
    private final TableColumn<HumanBeingFx, Number> idCol;
    private final TableColumn<HumanBeingFx, Number> keyCol;
    private final TableColumn<HumanBeingFx, String> nameCol;
    private final TableColumn<HumanBeingFx, Date> creationDateCol;
    private final TableColumn<HumanBeingFx, Number> xCol;
    private final TableColumn<HumanBeingFx, Number> yCol;
    private final TableColumn<HumanBeingFx, Boolean> realHeroCol;
    private final TableColumn<HumanBeingFx, Boolean> hasToothpickCol;
    private final TableColumn<HumanBeingFx, Number> speedCol;
    private final TableColumn<HumanBeingFx, String> soundtrackCol;
    private final TableColumn<HumanBeingFx, Number> minutesCol;
    private final TableColumn<HumanBeingFx, Mood> moodCol;
    private final TableColumn<HumanBeingFx, String> carCol;
    private final TableColumn<HumanBeingFx, String> ownerCol;
    private final Consumer<HumanBeingFx> editHandler;

    TableBinder(CollectionStore store,
                TableView<HumanBeingFx> table,
                TextField filterField,
                ComboBox<FilterColumn> filterColumnBox,
                TableColumn<HumanBeingFx, Number> idCol,
                TableColumn<HumanBeingFx, Number> keyCol,
                TableColumn<HumanBeingFx, String> nameCol,
                TableColumn<HumanBeingFx, Date> creationDateCol,
                TableColumn<HumanBeingFx, Number> xCol,
                TableColumn<HumanBeingFx, Number> yCol,
                TableColumn<HumanBeingFx, Boolean> realHeroCol,
                TableColumn<HumanBeingFx, Boolean> hasToothpickCol,
                TableColumn<HumanBeingFx, Number> speedCol,
                TableColumn<HumanBeingFx, String> soundtrackCol,
                TableColumn<HumanBeingFx, Number> minutesCol,
                TableColumn<HumanBeingFx, Mood> moodCol,
                TableColumn<HumanBeingFx, String> carCol,
                TableColumn<HumanBeingFx, String> ownerCol,
                Consumer<HumanBeingFx> editHandler) {
        this.store = store;
        this.table = table;
        this.filterField = filterField;
        this.filterColumnBox = filterColumnBox;
        this.idCol = idCol;
        this.keyCol = keyCol;
        this.nameCol = nameCol;
        this.creationDateCol = creationDateCol;
        this.xCol = xCol;
        this.yCol = yCol;
        this.realHeroCol = realHeroCol;
        this.hasToothpickCol = hasToothpickCol;
        this.speedCol = speedCol;
        this.soundtrackCol = soundtrackCol;
        this.minutesCol = minutesCol;
        this.moodCol = moodCol;
        this.carCol = carCol;
        this.ownerCol = ownerCol;
        this.editHandler = editHandler;
    }

    void configure() {
        configureColumns();
        configureRows();
        configureFilterColumnBox();
        configureFilteringAndSorting();
    }

    void refresh() {
        Predicate<HumanBeingFx> filter = buildFilter(filterField.getText(), filterColumnBox.getValue());
        Comparator<HumanBeingFx> comparator = table.getComparator();

        var stream = store.items().stream().filter(filter);
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }
        visibleItems.setAll(stream.collect(Collectors.toList()));
    }

    private void configureColumns() {
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
        ownerCol.setCellFactory(col -> ownerCell());
        moodCol.setCellFactory(col -> moodCell());
    }

    private void configureRows() {
        table.setRowFactory(tv -> {
            TableRow<HumanBeingFx> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    HumanBeingFx fx = row.getItem();
                    if (fx.getOwnerLogin().equals(Session.get().context().getLogin())) {
                        editHandler.accept(fx);
                    }
                }
            });
            return row;
        });
    }

    private TableCell<HumanBeingFx, Number> numericCell(Function<Number, String> formatter) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.apply(item));
            }
        };
    }

    private TableCell<HumanBeingFx, Date> dateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : LocalizedFormatter.formatDate(item));
            }
        };
    }

    private TableCell<HumanBeingFx, Boolean> booleanCell(boolean nullable) {
        return new TableCell<>() {
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

    private TableCell<HumanBeingFx, String> ownerCell() {
        return new TableCell<>() {
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
        };
    }

    private TableCell<HumanBeingFx, Mood> moodCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Mood item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(LocaleManager.get().tr("mood." + item.name()));
                setStyle("-fx-text-fill: " + MoodColorMap.hexFor(item) + ";");
            }
        };
    }

    private void configureFilteringAndSorting() {
        table.setItems(visibleItems);
        table.setSortPolicy(tableView -> {
            refresh();
            return true;
        });

        filterField.textProperty().addListener((obs, prev, value) -> refresh());
        filterColumnBox.valueProperty().addListener((obs, prev, value) -> refresh());
        store.items().addListener((ListChangeListener<HumanBeingFx>) change -> {
            while (change.next()) {
                change.getRemoved().forEach(this::unregisterItemRefresh);
                change.getAddedSubList().forEach(this::registerItemRefresh);
            }
            refresh();
        });
        store.items().forEach(this::registerItemRefresh);
        refresh();
    }

    private void configureFilterColumnBox() {
        filterColumnBox.getItems().setAll(FilterColumn.values());
        filterColumnBox.promptTextProperty().bind(Localizer.binding("filter.column.all"));
        filterColumnBox.setCellFactory(list -> filterColumnCell());
        filterColumnBox.setButtonCell(filterColumnCell());
        LocaleManager.get().localeProperty().addListener((obs, prev, value) ->
                filterColumnBox.setButtonCell(filterColumnCell()));
    }

    private ListCell<FilterColumn> filterColumnCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(FilterColumn item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : LocaleManager.get().tr(item.labelKey));
            }
        };
    }

    private void registerItemRefresh(HumanBeingFx fx) {
        if (itemRefreshListeners.containsKey(fx)) return;
        InvalidationListener listener = ignored -> refresh();
        itemRefreshListeners.put(fx, listener);
        itemProperties(fx).forEach(property -> property.addListener(listener));
    }

    private void unregisterItemRefresh(HumanBeingFx fx) {
        InvalidationListener listener = itemRefreshListeners.remove(fx);
        if (listener == null) return;
        itemProperties(fx).forEach(property -> property.removeListener(listener));
    }

    private List<ObservableValue<?>> itemProperties(HumanBeingFx fx) {
        return List.of(
                fx.idProperty(),
                fx.keyProperty(),
                fx.nameProperty(),
                fx.creationDateProperty(),
                fx.xProperty(),
                fx.yProperty(),
                fx.realHeroProperty(),
                fx.hasToothpickProperty(),
                fx.impactSpeedProperty(),
                fx.soundtrackNameProperty(),
                fx.minutesOfWaitingProperty(),
                fx.moodProperty(),
                fx.carNameProperty(),
                fx.ownerLoginProperty());
    }

    private Predicate<HumanBeingFx> buildFilter(String text, FilterColumn column) {
        if (column == null || column == FilterColumn.ALL) {
            return fx -> fx.matchesFilter(text);
        }
        return fx -> matchesColumn(fx, column, text);
    }

    private boolean matchesColumn(HumanBeingFx fx, FilterColumn column, String text) {
        if (text == null || text.isBlank()) return true;
        String value = valueForColumn(fx, column);
        return value != null && value.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT).trim());
    }

    private String valueForColumn(HumanBeingFx fx, FilterColumn column) {
        return switch (column) {
            case ID -> LocalizedFormatter.formatLong(fx.getId());
            case KEY -> LocalizedFormatter.formatLong(fx.getKey());
            case NAME -> fx.getName();
            case CREATION_DATE -> LocalizedFormatter.formatDate(fx.getCreationDate());
            case X -> LocalizedFormatter.formatDouble(fx.getX());
            case Y -> LocalizedFormatter.formatInteger(fx.getY());
            case REAL_HERO -> LocaleManager.get().tr(fx.isRealHero() ? "popup.yes" : "popup.no");
            case HAS_TOOTHPICK -> fx.getHasToothpick() == null
                    ? LocaleManager.get().tr("popup.dash")
                    : LocaleManager.get().tr(fx.getHasToothpick() ? "popup.yes" : "popup.no");
            case SPEED -> LocalizedFormatter.formatDouble(fx.getImpactSpeed());
            case SOUNDTRACK -> fx.getSoundtrackName();
            case MINUTES -> LocalizedFormatter.formatInteger(fx.getMinutesOfWaiting());
            case MOOD -> fx.getMood() == null
                    ? LocaleManager.get().tr("popup.dash")
                    : LocaleManager.get().tr("mood." + fx.getMood().name());
            case CAR -> fx.getCarName();
            case OWNER -> fx.getOwnerLogin();
            case ALL -> "";
        };
    }

    enum FilterColumn {
        ALL("filter.column.all"),
        ID("table.id"),
        KEY("table.key"),
        NAME("table.name"),
        CREATION_DATE("table.creationDate"),
        X("table.x"),
        Y("table.y"),
        REAL_HERO("table.realHero"),
        HAS_TOOTHPICK("table.hasToothpick"),
        SPEED("table.speed"),
        SOUNDTRACK("table.soundtrack"),
        MINUTES("table.minutes"),
        MOOD("table.mood"),
        CAR("table.car"),
        OWNER("table.owner");

        private final String labelKey;

        FilterColumn(String labelKey) {
            this.labelKey = labelKey;
        }
    }
}
