package commands;

import managers.AuthManager;
import network.Request;
import network.Response;

import java.sql.SQLException;

/** Команда register — регистрирует нового пользователя. */
public class Register extends Command {
    private final AuthManager authManager;

    public Register(AuthManager authManager) {
        super("register", "register", "help.command.register",
                "зарегистрировать нового пользователя", false);
        this.authManager = authManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            boolean ok = authManager.register(request.getLogin(), request.getPassword());
            return ok ? new Response("Регистрация успешна", true)
                      : new Response("Логин уже занят", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        }
    }
}
