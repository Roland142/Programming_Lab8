package core;

import command.CommandParser;
import command.CommandReader;
import command.SessionContext;
import command.exceptions.ExitCommandException;
import command.exceptions.ServerUnavailableException;
import network.Request;
import network.Response;
import network.ServerGateway;

/** Точка входа клиентского приложения: подключение → аутентификация → цикл команд. */
public class Client {

    public void start() {
        SessionContext session = new SessionContext();
        ServerGateway  gateway = new ServerGateway();
        CommandReader  reader  = new CommandReader();
        CommandParser  parser  = new CommandParser(session);
        AuthFlow       auth    = new AuthFlow(reader, parser, session, gateway);

        if (!gateway.connect()) {
            System.out.println("Не удалось подключиться к серверу");
            return;
        }

        if (!auth.run()) {
            gateway.close();
            return;
        }

        commandLoop(reader, parser, gateway);
        gateway.close();
    }

    private void commandLoop(CommandReader reader, CommandParser parser, ServerGateway gateway) {
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            if (line.isBlank()) continue;

            try {
                Request request = parser.parse(line);
                if (request == null) continue;

                Response response = gateway.send(request);
                if (response != null && !response.getMessage().isEmpty()) {
                    System.out.println(response.getMessage());
                }
            } catch (ExitCommandException e) {
                break;
            } catch (ServerUnavailableException e) {
                System.out.println("Завершение работы.");
                break;
            }
        }
    }
}
