package elements;

import java.io.Serializable;
import exceptions.InvalidDataException;

/**
 * Координаты
 * x должно быть больше -975 и не null, y не может быть null.
 */
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private Double x;
    private Integer y;

    public Coordinates(Double x, Integer y) throws InvalidDataException {
        this.setX(x);
        this.setY(y);
    }

    public Double getX() { return x; }

    public void setX(Double x) throws InvalidDataException {
        if (x == null || x <= -975) throw new InvalidDataException("x должно быть > -975 и не null");
        this.x = x;
    }

    public Integer getY() { return y; }

    public void setY(Integer y) throws InvalidDataException {
        if (y == null) throw new InvalidDataException("y не может быть null");
        this.y = y;
    }

    public String getCoordinates() {
        return x + ";" + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
