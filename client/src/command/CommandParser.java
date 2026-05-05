package command;

import command.exceptions.ExitCommandException;
import command.interfaces.ClientCommand;
import command.types.ClientCommandType;
import network.Request;

/**
 * Разбирает строку ввода: определяет тип команды через enum,
 * получает объект команды через фабрику, формирует Request.
 * Credentials берёт из SessionContext.
 */
public class CommandParser {

    private final CommandFactory factory = new CommandFactory();
    private final SessionContext session;

    public CommandParser(SessionContext session) {
        this.session = session;
    }

    /**
     * Парсит строку и возвращает готовый Request.
     * @return Request для отправки на сервер, или null если команда не требует отправки
     * @throws ExitCommandException если пользователь ввёл exit
     */
    public Request parse(String line) throws ExitCommandException {
        String[] parts = line.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? java.util.Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        ClientCommandType type = ClientCommandType.fromString(commandName);

        if (type == null) {
            System.out.println("Неизвестная команда: " + commandName);
            return null;
        }

        if (type == ClientCommandType.EXIT) {
            throw new ExitCommandException();
        }

        ClientCommand command = factory.getCommand(type);
        return command.buildRequest(commandName, args, session.getLogin(), session.getPasswordHash());
    }
}
