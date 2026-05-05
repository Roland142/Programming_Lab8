package command.handlers;

import command.interfaces.ClientCommand;
import network.Request;
import utils.HashUtil;

/** Команда register/login: логин и пароль берутся из аргументов, пароль хешируется на клиенте. */
public class AuthCommand implements ClientCommand {

    @Override
    public Request buildRequest(String commandName, String[] args, String login, String password) {
        if (args.length < 2) {
            System.out.println("Использование: " + commandName + " <логин> <пароль>");
            return null;
        }
        return new Request(commandName, null, null, args[0], HashUtil.hash(args[1]));
    }
}
