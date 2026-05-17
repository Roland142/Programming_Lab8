package commands;

import managers.CommandManager;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Команда history — возвращает последние 12 выполненных команд. */
public class History extends Command {
    private final CommandManager commandManager;

    public History(CommandManager commandManager) {
        super("history", "history", "help.command.history", "вывести последние 12 команд");
        this.commandManager = commandManager;
    }

    @Override
    public Response execute(Request request) {
        List<String> commands = new ArrayList<>(commandManager.getLastCommands());
        String history = commands.stream().collect(Collectors.joining("\n"));
        if (history.isEmpty()) return new Response("История команд пуста", true, commands);
        return new Response("Последние команды:\n" + history, true, commands);
    }
}
