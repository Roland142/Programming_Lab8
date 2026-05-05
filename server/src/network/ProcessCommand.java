package network;

import managers.AuthManager;
import managers.CommandManager;

import java.sql.SQLException;

/**
 * Модуль 3 — проверка авторизации и передача команды в CommandManager.
 * Команды register и login не требуют авторизации.
 */
public class ProcessCommand {

    public static Response process(Request request, CommandManager commandManager, AuthManager authManager) {
        String cmd = request.getCommandName();

        if (!cmd.equals("register") && !cmd.equals("login")) {
            try {
                if (request.getLogin() == null || request.getLogin().isBlank() ||
                        request.getPassword() == null || request.getPassword().isBlank() ||
                        !authManager.authenticate(request.getLogin(), request.getPassword())) {
                    return new Response("Ошибка: необходима авторизация", false);
                }
            } catch (SQLException e) {
                return new Response("Ошибка БД при авторизации: " + e.getMessage(), false);
            }
        }

        return commandManager.execute(request);
    }
}
