package core;

import command.CommandParser;
import command.CommandReader;
import command.SessionContext;
import command.exceptions.ExitCommandException;
import command.exceptions.ServerUnavailableException;
import network.Request;
import network.Response;
import network.ServerGateway;
import utils.HashUtil;

/** Цикл аутентификации: принимает register/login до первого успешного входа. */
public class AuthFlow {

    private final CommandReader reader;
    private final CommandParser parser;
    private final SessionContext session;
    private final ServerGateway gateway;

    public AuthFlow(CommandReader reader, CommandParser parser,
                    SessionContext session, ServerGateway gateway) {
        this.reader  = reader;
        this.parser  = parser;
        this.session = session;
        this.gateway = gateway;
    }

    /** @return true если аутентификация прошла, false если пользователь прервал ввод. */
    public boolean run() {
        System.out.println("Для начала работы введите:");
        System.out.println("  register <логин>  — создать аккаунт");
        System.out.println("  login <логин>     — войти");

        while (true) {
            String line = reader.readLine();
            if (line == null) return false;
            if (line.isBlank()) continue;

            String[] parts = line.trim().split("\\s+");
            String cmd = parts[0].toLowerCase();

            if (!cmd.equals("register") && !cmd.equals("login")) {
                System.out.println("Сначала необходимо войти. Используйте register или login.");
                continue;
            }
            if (parts.length < 2) {
                System.out.println("Использование: " + cmd + " <логин>");
                continue;
            }

            String login    = parts[1];
            String password = parts.length >= 3 ? parts[2] : readPasswordHidden();
            if (password == null) return false;

            try {
                Request request = parser.parse(cmd + " " + login + " " + password);
                if (request == null) continue;

                Response response = gateway.send(request);
                if (response == null) { System.out.println("Нет ответа от сервера"); continue; }
                System.out.println(response.getMessage());

                if (response.isSuccess()) {
                    session.setCredentials(login, HashUtil.hash(password));
                    System.out.println("Сессия установлена для: " + login);
                    return true;
                }
            } catch (ExitCommandException e) {
                return false;
            } catch (ServerUnavailableException e) {
                System.out.println("Завершение работы.");
                return false;
            }
        }
    }

    private String readPasswordHidden() {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword("Пароль: ");
            return pwd != null ? new String(pwd) : null;
        }
        System.out.print("Пароль: ");
        return reader.readLine();
    }
}
