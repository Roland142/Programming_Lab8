package network;

import command.exceptions.ServerUnavailableException;
import core.Connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/** Отправляет запросы на сервер и получает ответы; при потере связи — переподключается. */
public class ServerGateway {

    private SocketChannel channel;

    public boolean connect() {
        channel = Connection.connect();
        if (channel != null) {
            System.out.println("Подключено к localhost:" + Connection.getPort());
        }
        return channel != null;
    }

    public Response send(Request request) throws ServerUnavailableException {
        Response response = trySend(request);
        if (response == null) {
            System.out.println("Соединение потеряно. Переподключение...");
            close();
            channel = Connection.reconnect();
            if (channel == null) throw new ServerUnavailableException();
            response = trySend(request);
            if (response == null) throw new ServerUnavailableException();
            System.out.println("Переподключение успешно!");
        }
        return response;
    }

    private Response trySend(Request request) {
        if (!RequestSender.send(channel, request)) return null;
        return ResponseReceiver.receive(channel);
    }

    public void close() {
        try { if (channel != null) channel.close(); } catch (IOException ignored) {}
    }
}
