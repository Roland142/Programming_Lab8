package builders;

import elements.HumanBeing;
import exceptions.InvalidDataException;

/** Строит объект HumanBeing путём пошагового ввода полей. */
public class HumanBeingBuilder extends Builder {
    public HumanBeing create() throws InvalidDataException {
        return new HumanBeing(
                buildString("name"),
                new CoordinatesBuilder().create(),
                buildBoolean("realHero"),
                buildBooleanNullable("hasToothpick"),
                buildDouble("impactSpeed"),
                buildString("soundtrackName"),
                buildInt("minutesOfWaiting"),
                new MoodBuilder().create(),
                new CarBuilder().create());
    }

    public HumanBeing update(HumanBeing hb) throws InvalidDataException {
        hb.setName(buildString("name"));
        hb.setCoordinates(new CoordinatesBuilder().create());
        hb.setRealHero(buildBoolean("realHero"));
        hb.setHasToothpick(buildBooleanNullable("hasToothpick"));
        hb.setImpactSpeed(buildDouble("impactSpeed"));
        hb.setSoundtrackName(buildString("soundtrackName"));
        hb.setMinutesOfWaiting(buildInt("minutesOfWaiting"));
        hb.setMood(new MoodBuilder().create());
        hb.setCar(new CarBuilder().create());
        return hb;
    }
}
