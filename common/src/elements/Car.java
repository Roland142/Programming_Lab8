package elements;

import java.io.Serializable;

/**
 * Машина — поле элемента коллекции (может быть null).
 */
public class Car implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public Car(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return this.getName();
    }
}
