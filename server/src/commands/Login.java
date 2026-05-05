package commands;

import managers.AuthManager;
import network.Request;
import network.Response;

import java.sql.SQLException;

/** Команда login — проверяет credentials пользователя. */
public class Login extends Command {
    private final AuthManager authManager;

    public Login(AuthManager authManager) {
        super("login");
        this.authManager = authManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            boolean ok = authManager.authenticate(request.getLogin(), request.getPassword());
            return ok ? new Response("Вход выполнен", true)
                      : new Response("Неверный логин или пароль", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        }
    }
}
