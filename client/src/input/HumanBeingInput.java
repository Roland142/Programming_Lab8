package input;

import builders.HumanBeingBuilder;
import elements.HumanBeing;
import exceptions.InvalidDataException;

/** Запускает HumanBeingBuilder и возвращает введённый объект. */
public class HumanBeingInput {

    public HumanBeing build() {
        try {
            return new HumanBeingBuilder().create();
        } catch (InvalidDataException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
            return null;
        }
    }
}
