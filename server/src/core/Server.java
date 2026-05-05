package core;

import managers.AuthManager;
import managers.CommandManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger("Server");

    private final int port;
    private final CommandManager commandManager;
    private final AuthManager authManager;
    private final ExecutorService processingPool = Executors.newCachedThreadPool();

    public Server(int port, CommandManager commandManager, AuthManager authManager) {
        this.port = port;
        this.commandManager = commandManager;
        this.authManager = authManager;
    }

    public void start() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(port));
            logger.info("Сервер запущен на порту " + port);

            while (true) {
                SocketChannel clientChannel = serverChannel.accept();
                logger.info("Новый клиент: " + clientChannel.getRemoteAddress());
                new Thread(new ClientHandler(clientChannel, commandManager, authManager, processingPool)).start();
            }
        } catch (IOException e) {
            logger.severe("Ошибка сервера: " + e.getMessage());
        }
    }
}
