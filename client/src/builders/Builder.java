package builders;

import interfaces.Reader;
import managers.FileMod;
import managers.ManualInput;
import managers.ScriptExecutorManager;

/**
 * Базовый класс для пошагового ввода данных (консоль или скрипт).
 */
public abstract class Builder {
    protected final Reader scanner;

    public Builder() {
        this.scanner = (FileMod.isFileMod) ? new ScriptExecutorManager() : new ManualInput();
    }

    public String buildString(String name) {
        String input;
        while (true) {
            System.out.println("Введите " + name);
            input = scanner.nextLine();
            if (input.isEmpty()) {
                System.err.println("Строка не может быть пустой!");
            } else {
                return input;
            }
        }
    }

    public String buildStringNullable(String name) {
        System.out.println("Введите " + name);
        String input = scanner.nextLine();
        if (input == null || input.trim().isEmpty()) return null;
        return input.trim();
    }

    public Double buildDouble(String name) {
        String input;
        while (true) {
            System.out.println("Введите " + name);
            input = scanner.nextLine();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.err.println("Число должно быть Double");
            }
        }
    }

    public Boolean buildBoolean(String name) {
        while (true) {
            System.out.println("Введите " + name);
            String input = scanner.nextLine();
            if (input == null || input.trim().isEmpty()) {
                System.err.println("Поле не может быть пустым!");
                continue;
            }
            return Boolean.parseBoolean(input);
        }
    }

    public Boolean buildBooleanNullable(String name) {
        System.out.println("Введите " + name);
        String input = scanner.nextLine();
        if (input == null || input.trim().isEmpty()) return null;
        return Boolean.parseBoolean(input.trim());
    }

    public Long buildLong(String name) {
        String input;
        while (true) {
            System.out.println("Введите " + name);
            input = scanner.nextLine();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.err.println("Число должно быть long");
            }
        }
    }

    public Integer buildInt(String name) {
        String input;
        while (true) {
            System.out.println("Введите " + name);
            input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.err.println("Число должно быть int");
            }
        }
    }
}
