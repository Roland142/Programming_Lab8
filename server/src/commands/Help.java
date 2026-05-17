package commands;

import managers.CommandManager;
import network.CommandInfo;
import network.Request;
import network.Response;

import java.util.List;
import java.util.stream.Collectors;

/** Команда help — возвращает справку по всем доступным командам. */
public class Help extends Command {
    private final CommandManager commandManager;

    public Help(CommandManager commandManager) {
        super("help", "help", "help.command.help", "вывести справку по доступным командам");
        this.commandManager = commandManager;
    }

    @Override
    public Response execute(Request request) {
        List<CommandInfo> commands = commandManager.getCommands().values().stream()
                .filter(Command::isVisibleInHelp)
                .map(command -> new CommandInfo(command.getName(), command.getUsage(),
                        command.getDescriptionKey(), command.getFallbackDescription()))
                .collect(Collectors.toList());

        String help = "Доступные команды:\n" + commands.stream()
                .map(command -> command.getUsage() + " : " + command.getFallbackDescription())
                .collect(Collectors.joining("\n"));
        return new Response(help, true, commands);
    }
}
