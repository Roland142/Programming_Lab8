package gui.net;

import command.exceptions.ServerUnavailableException;
import javafx.concurrent.Task;
import network.Request;
import network.Response;
import network.ServerGateway;

/**
 * Синхронизированная обёртка над {@link ServerGateway} для GUI:
 * блокирующий {@link #sendBlocking(Request)} (вызывается из фонового потока)
 * и {@link #sendTask(Request)} — JavaFX {@link Task}, который удобно
 * подписать через onSucceeded/onFailed в контроллере.
 */
public class GuiGateway {

    private final ServerGateway gateway = new ServerGateway();

    public synchronized boolean connect() {
        return gateway.connect();
    }

    public synchronized Response sendBlocking(Request request) throws ServerUnavailableException {
        return gateway.send(request);
    }

    public Task<Response> sendTask(Request request) {
        return new Task<>() {
            @Override
            protected Response call() throws Exception {
                return sendBlocking(request);
            }
        };
    }

    public synchronized void close() {
        gateway.close();
    }
}
