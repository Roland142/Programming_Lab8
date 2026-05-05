package commands;

import managers.CommandManager;
import network.Request;
import network.Response;

import java.util.stream.Collectors;

/** Команда history — возвращает последние 12 выполненных команд. */
public class History extends Command {
    private final CommandManager commandManager;

    public History(CommandManager commandManager) {
        super("history");
        this.commandManager = commandManager;
    }

    @Override
    public Response execute(Request request) {
        String history = commandManager.getLastCommands().stream()
                .collect(Collectors.joining("\n"));
        if (history.isEmpty()) return new Response("История команд пуста", true);
        return new Response("Последние команды:\n" + history, true);
    }
}
