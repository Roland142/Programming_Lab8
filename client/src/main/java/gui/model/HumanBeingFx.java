package gui.model;

import elements.Car;
import elements.Coordinates;
import elements.HumanBeing;
import elements.Mood;
import exceptions.InvalidDataException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import network.HumanBeingEntry;

import java.util.Date;
import java.util.stream.Stream;

/**
 * JavaFX-обёртка над {@link HumanBeing} для биндинга в TableView и канвасе.
 * Содержит все поля исходного объекта плюс key (TreeMap-ключ на сервере)
 * и ownerLogin (логин владельца).
 */
public class HumanBeingFx {

    private final LongProperty key = new SimpleLongProperty(this, "key");
    private final LongProperty id = new SimpleLongProperty(this, "id");
    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final DoubleProperty x = new SimpleDoubleProperty(this, "x");
    private final IntegerProperty y = new SimpleIntegerProperty(this, "y");
    private final ObjectProperty<Date> creationDate = new SimpleObjectProperty<>(this, "creationDate");
    private final BooleanProperty realHero = new SimpleBooleanProperty(this, "realHero");
    private final ObjectProperty<Boolean> hasToothpick = new SimpleObjectProperty<>(this, "hasToothpick");
    private final DoubleProperty impactSpeed = new SimpleDoubleProperty(this, "impactSpeed");
    private final StringProperty soundtrackName = new SimpleStringProperty(this, "soundtrackName", "");
    private final IntegerProperty minutesOfWaiting = new SimpleIntegerProperty(this, "minutesOfWaiting");
    private final ObjectProperty<Mood> mood = new SimpleObjectProperty<>(this, "mood");
    private final StringProperty carName = new SimpleStringProperty(this, "carName");
    private final StringProperty ownerLogin = new SimpleStringProperty(this, "ownerLogin", "");

    public static HumanBeingFx from(HumanBeingEntry entry) {
        HumanBeingFx fx = new HumanBeingFx();
        fx.applyFrom(entry);
        return fx;
    }

    public void applyFrom(HumanBeingEntry entry) {
        HumanBeing hb = entry.getHumanBeing();
        key.set(entry.getKey());
        id.set(hb.getId());
        name.set(hb.getName());
        x.set(hb.getCoordinates().getX());
        y.set(hb.getCoordinates().getY());
        creationDate.set(hb.getCreationDate());
        realHero.set(Boolean.TRUE.equals(hb.getRealHero()));
        hasToothpick.set(hb.getHasToothpick());
        impactSpeed.set(hb.getImpactSpeed());
        soundtrackName.set(hb.getSoundtrackName());
        minutesOfWaiting.set(hb.getMinutesOfWaiting());
        mood.set(hb.getMood());
        carName.set(hb.getCar() != null ? hb.getCar().getName() : null);
        ownerLogin.set(entry.getOwnerLogin() != null ? entry.getOwnerLogin() : "");
    }

    /** Преобразование обратно в HumanBeing для отправки на сервер. */
    public HumanBeing toHumanBeing() throws InvalidDataException {
        Coordinates coords = new Coordinates(x.get(), y.get());
        Car car = (carName.get() == null || carName.get().isBlank()) ? null : new Car(carName.get());
        HumanBeing hb = new HumanBeing(
                name.get(),
                coords,
                realHero.get(),
                hasToothpick.get(),
                impactSpeed.get(),
                soundtrackName.get(),
                minutesOfWaiting.get(),
                mood.get(),
                car);
        if (id.get() > 0) hb.setId(id.get());
        if (creationDate.get() != null) hb.setCreationDate(creationDate.get());
        return hb;
    }

    /**
     * Совпадение фильтра по любому полю через Streams API.
     * Сравнение регистронезависимое; пустой фильтр считается совпавшим везде.
     */
    public boolean matchesFilter(String text) {
        if (text == null || text.isBlank()) return true;
        String needle = text.toLowerCase().trim();
        return Stream.of(
                String.valueOf(id.get()),
                String.valueOf(key.get()),
                name.get(),
                String.valueOf(x.get()),
                String.valueOf(y.get()),
                String.valueOf(impactSpeed.get()),
                String.valueOf(minutesOfWaiting.get()),
                soundtrackName.get(),
                mood.get() != null ? mood.get().name() : "",
                carName.get() != null ? carName.get() : "",
                ownerLogin.get(),
                String.valueOf(realHero.get()))
                .filter(s -> s != null)
                .map(String::toLowerCase)
                .anyMatch(s -> s.contains(needle));
    }

    public LongProperty keyProperty() { return key; }
    public long getKey() { return key.get(); }

    public LongProperty idProperty() { return id; }
    public long getId() { return id.get(); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }

    public DoubleProperty xProperty() { return x; }
    public double getX() { return x.get(); }

    public IntegerProperty yProperty() { return y; }
    public int getY() { return y.get(); }

    public ObjectProperty<Date> creationDateProperty() { return creationDate; }
    public Date getCreationDate() { return creationDate.get(); }

    public BooleanProperty realHeroProperty() { return realHero; }
    public boolean isRealHero() { return realHero.get(); }

    public ObjectProperty<Boolean> hasToothpickProperty() { return hasToothpick; }
    public Boolean getHasToothpick() { return hasToothpick.get(); }

    public DoubleProperty impactSpeedProperty() { return impactSpeed; }
    public double getImpactSpeed() { return impactSpeed.get(); }

    public StringProperty soundtrackNameProperty() { return soundtrackName; }
    public String getSoundtrackName() { return soundtrackName.get(); }

    public IntegerProperty minutesOfWaitingProperty() { return minutesOfWaiting; }
    public int getMinutesOfWaiting() { return minutesOfWaiting.get(); }

    public ObjectProperty<Mood> moodProperty() { return mood; }
    public Mood getMood() { return mood.get(); }

    public StringProperty carNameProperty() { return carName; }
    public String getCarName() { return carName.get(); }

    public StringProperty ownerLoginProperty() { return ownerLogin; }
    public String getOwnerLogin() { return ownerLogin.get(); }
}
