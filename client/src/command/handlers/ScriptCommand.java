package command.handlers;

import command.handlers.ScriptHandler;
import command.interfaces.ClientCommand;
import network.Request;

/** Команда execute_script: запускает скрипт на клиенте и регистрирует в истории на сервере. */
public class ScriptCommand implements ClientCommand {

    private final ScriptHandler scriptHandler;

    public ScriptCommand(ScriptHandler scriptHandler) {
        this.scriptHandler = scriptHandler;
    }

    @Override
    public Request buildRequest(String commandName, String[] args, String login, String password) {
        scriptHandler.handle(args.length > 0 ? args[0] : "");
        return new Request(commandName, args, null, login, password);
    }
}
