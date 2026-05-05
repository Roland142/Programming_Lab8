package command;

import command.handlers.*;
import command.interfaces.ClientCommand;
import command.types.ClientCommandType;
import command.types.CommandAction;
import input.HumanBeingInput;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика клиентских команд.
 * Связывает каждый ClientCommandType с объектом ClientCommand через Map.
 */
public class CommandFactory {

    private final Map<CommandAction, ClientCommand> commands = new HashMap<>();

    public CommandFactory() {
        HumanBeingInput humanBeingInput = new HumanBeingInput();

        commands.put(CommandAction.SEND, new SendCommand());
        commands.put(CommandAction.SEND_WITH_ARG, new SendWithArgCommand());
        commands.put(CommandAction.SEND_WITH_OBJECT, new SendWithObjectCommand(humanBeingInput, false));
        commands.put(CommandAction.SEND_WITH_OBJ_AND_ARG, new SendWithObjectCommand(humanBeingInput, true));
        commands.put(CommandAction.BLOCKED, new BlockedCommand());
        commands.put(CommandAction.SCRIPT, new ScriptCommand(new ScriptHandler()));
        commands.put(CommandAction.AUTH, new AuthCommand());
    }

    public ClientCommand getCommand(ClientCommandType type) {
        return commands.get(type.getAction());
    }
}
