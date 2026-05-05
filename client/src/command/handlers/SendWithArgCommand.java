package command.handlers;

import command.interfaces.ClientCommand;
import network.Request;

/** Команда, которая требует аргумент и отправляет запрос на сервер без объекта. */
public class SendWithArgCommand implements ClientCommand {

    @Override
    public Request buildRequest(String commandName, String[] args, String login, String password) {
        if (args.length == 0) {
            System.out.println("Ошибка: команда " + commandName + " требует аргумент");
            return null;
        }
        return new Request(commandName, args, null, login, password);
    }
}
