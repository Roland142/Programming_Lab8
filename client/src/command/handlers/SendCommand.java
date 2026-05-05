package command.handlers;

import command.interfaces.ClientCommand;
import network.Request;

/** Команда, которая просто отправляет запрос на сервер без объекта. */
public class SendCommand implements ClientCommand {

    @Override
    public Request buildRequest(String commandName, String[] args, String login, String password) {
        return new Request(commandName, args, null, login, password);
    }
}
