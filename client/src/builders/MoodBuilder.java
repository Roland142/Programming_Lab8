package builders;

import elements.Mood;
import exceptions.InvalidDataException;

/** Строит значение Mood по вводу пользователя. */
public class MoodBuilder extends Builder {
    public Mood create() throws InvalidDataException {
        System.out.println("Возможные настроения: ");
        System.out.println(java.util.Arrays.toString(Mood.values()));
        while (true) {
            String input = scanner.nextLine();
            if (input == null || input.trim().isEmpty()) return null;
            try {
                return Mood.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Такого настроения нет в списке. Повторите ввод:");
            }
        }
    }
}
