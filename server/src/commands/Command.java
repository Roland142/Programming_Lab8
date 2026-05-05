package commands;

import network.Request;
import network.Response;
import java.util.Objects;

/**
 * Абстрактная команда сервера.
 * Получает Request целиком — берёт из него args, humanBeing, login, password.
 */
public abstract class Command {
    private final String name;

    public Command(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract Response execute(Request request);

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(name, command.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }
}
