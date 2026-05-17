package managers;

import commands.Command;
import network.Request;
import network.Response;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Менеджер команд сервера.
 */
public class CommandManager {
    private final HashMap<String, Command> commands = new LinkedHashMap<>();
    private final ArrayDeque<String> lastCommands = new ArrayDeque<>();

    public void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public Response execute(Request request) {
        Command command = commands.get(request.getCommandName());
        if (command == null) {
            return new Response("Неизвестная команда: " + request.getCommandName(), false);
        }
        Response response = command.execute(request);
        if (response.isSuccess() && shouldRecord(request.getCommandName())) {
            if (lastCommands.size() >= 12) lastCommands.removeFirst();
            lastCommands.addLast(request.getCommandName());
        }
        return response;
    }

    private boolean shouldRecord(String commandName) {
        return !"show".equals(commandName);
    }

    public HashMap<String, Command> getCommands() { return commands; }

    public ArrayDeque<String> getLastCommands() { return lastCommands; }
}
