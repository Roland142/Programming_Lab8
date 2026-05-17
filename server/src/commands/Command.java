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
    private final String usage;
    private final String descriptionKey;
    private final String fallbackDescription;
    private final boolean visibleInHelp;

    public Command(String name) {
        this(name, name, "help.command." + name, "", true);
    }

    public Command(String name, String usage, String descriptionKey, String fallbackDescription) {
        this(name, usage, descriptionKey, fallbackDescription, true);
    }

    public Command(String name, String usage, String descriptionKey,
                   String fallbackDescription, boolean visibleInHelp) {
        this.name = name;
        this.usage = usage;
        this.descriptionKey = descriptionKey;
        this.fallbackDescription = fallbackDescription;
        this.visibleInHelp = visibleInHelp;
    }

    public String getName() { return name; }

    public String getUsage() { return usage; }

    public String getDescriptionKey() { return descriptionKey; }

    public String getFallbackDescription() { return fallbackDescription; }

    public boolean isVisibleInHelp() { return visibleInHelp; }

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
