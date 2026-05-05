package builders;

import elements.Car;
import exceptions.InvalidDataException;

/** Строит объект Car через ввод с консоли или из скрипта. */
public class CarBuilder extends Builder {
    public Car create() throws InvalidDataException {
        String name = buildStringNullable("Car name");
        if (name == null) return null;
        return new Car(name);
    }
}
