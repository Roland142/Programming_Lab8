package command.handlers;

import command.interfaces.ClientCommand;
import network.Request;

/** Команда, заблокированная на клиенте (например, save). */
public class BlockedCommand implements ClientCommand {

    @Override
    public Request buildRequest(String commandName, String[] args, String login, String password) {
        System.out.println("Команда " + commandName + " недоступна для клиента");
        return null;
    }
}
