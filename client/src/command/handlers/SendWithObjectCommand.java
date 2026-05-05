package command.handlers;

import command.interfaces.ClientCommand;
import elements.HumanBeing;
import input.HumanBeingInput;
import network.Request;

/** Команда, которая запрашивает у пользователя HumanBeing и отправляет на сервер. */
public class SendWithObjectCommand implements ClientCommand {

    private final HumanBeingInput humanBeingInput;
    private final boolean requiresArgs;

    public SendWithObjectCommand(HumanBeingInput humanBeingInput, boolean requiresArgs) {
        this.humanBeingInput = humanBeingInput;
        this.requiresArgs = requiresArgs;
    }

    @Override
    public Request buildRequest(String commandName, String[] args, String login, String password) {
        if (requiresArgs && args.length == 0) {
            System.out.println("Ошибка: команда " + commandName + " требует аргумент");
            return null;
        }
        HumanBeing humanBeing = humanBeingInput.build();
        if (humanBeing == null) return null;
        return new Request(commandName, args, humanBeing, login, password);
    }
}
