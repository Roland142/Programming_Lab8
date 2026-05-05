package gui.net;

import gui.Session;
import javafx.concurrent.Task;
import network.Request;
import network.Response;
import utils.HashUtil;

/**
 * Сценарии авторизации: login и register.
 * Возвращают {@link Task}, который выполняется в фоновом потоке;
 * результат — {@link Response} от сервера. Контроллер навешивает onSucceeded.
 */
public class LoginService {

    private final GuiGateway gateway;

    public LoginService(GuiGateway gateway) {
        this.gateway = gateway;
    }

    public Task<Response> login(String login, String password) {
        String hash = HashUtil.hash(password);
        Request request = new Request("login", null, null, login, hash);
        Task<Response> task = gateway.sendTask(request);
        task.setOnSucceeded(e -> {
            if (task.getValue().isSuccess()) {
                Session.get().context().setCredentials(login, hash);
            }
        });
        return task;
    }

    public Task<Response> register(String login, String password) {
        String hash = HashUtil.hash(password);
        Request request = new Request("register", null, null, login, hash);
        Task<Response> task = gateway.sendTask(request);
        task.setOnSucceeded(e -> {
            if (task.getValue().isSuccess()) {
                Session.get().context().setCredentials(login, hash);
            }
        });
        return task;
    }
}
