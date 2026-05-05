package builders;

import elements.Coordinates;
import exceptions.InvalidDataException;

/** Строит объект Coordinates через ввод с консоли или из скрипта. */
public class CoordinatesBuilder extends Builder {
    public Coordinates create() throws InvalidDataException {
        Double x;
        while (true) {
            x = buildDouble("x");
            if (x > -975) break;
            System.out.println("x должно быть больше -975. Повторите ввод:");
        }
        return new Coordinates(x, buildInt("y"));
    }
}
