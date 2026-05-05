package network;

import elements.HumanBeing;
import java.io.Serializable;

/**
 * Объект запроса от клиента к серверу.
 * Содержит имя команды, аргументы, объект HumanBeing и учётные данные пользователя.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String commandName;
    private final String[] args;
    private final HumanBeing humanBeing;
    private final String login;
    private final String password;

    public Request(String commandName, String[] args, HumanBeing humanBeing, String login, String password) {
        this.commandName = commandName;
        this.args = args;
        this.humanBeing = humanBeing;
        this.login = login;
        this.password = password;
    }

    public String getCommandName() { return commandName; }

    public String[] getArgs() { return args; }

    public HumanBeing getHumanBeing() { return humanBeing; }

    public String getLogin() { return login; }

    public String getPassword() { return password; }
}
