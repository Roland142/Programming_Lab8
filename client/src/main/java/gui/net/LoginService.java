package gui.net;

import javafx.concurrent.Task;
import network.Request;
import network.Response;
import utils.HashUtil;

/**
 * Сценарии авторизации: login и register.
 * Возвращают пару {@link AuthAttempt} (login + хеш пароля + Task с
 * запросом). Запись credentials в {@link gui.Session} делает контроллер
 * после успешного ответа сервера — это нужно, чтобы у Task оставался
 * единственный setOnSucceeded на стороне контроллера.
 */
public class LoginService {

    private final GuiGateway gateway;

    public LoginService(GuiGateway gateway) {
        this.gateway = gateway;
    }

    public AuthAttempt login(String login, String password) {
        String hash = HashUtil.hash(password);
        Task<Response> task = gateway.sendTask(
                new Request("login", null, null, login, hash));
        return new AuthAttempt(login, hash, task);
    }

    public AuthAttempt register(String login, String password) {
        String hash = HashUtil.hash(password);
        Task<Response> task = gateway.sendTask(
                new Request("register", null, null, login, hash));
        return new AuthAttempt(login, hash, task);
    }

    public static final class AuthAttempt {
        public final String login;
        public final String passwordHash;
        public final Task<Response> task;

        AuthAttempt(String login, String passwordHash, Task<Response> task) {
            this.login = login;
            this.passwordHash = passwordHash;
            this.task = task;
        }
    }
}
