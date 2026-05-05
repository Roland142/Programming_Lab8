package core;

import managers.AuthManager;
import managers.CommandManager;
import network.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/** Управляет жизненным циклом одного клиентского подключения. */
public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger("ClientHandler");

    private final SocketChannel channel;
    private final CommandManager commandManager;
    private final AuthManager authManager;
    private final ExecutorService processingPool;

    public ClientHandler(SocketChannel channel, CommandManager commandManager,
                         AuthManager authManager, ExecutorService processingPool) {
        this.channel = channel;
        this.commandManager = commandManager;
        this.authManager = authManager;
        this.processingPool = processingPool;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Request request = ReadRequest.read(channel);
                if (request == null) break;

                String[] args = request.getArgs();
                String argsStr = (args != null && args.length > 0) ? " " + String.join(" ", args) : "";
                logger.info("Получена команда: " + request.getCommandName() + argsStr);

                processingPool.submit(() -> {
                    Response response = ProcessCommand.process(request, commandManager, authManager);
                    sendResponse(response);
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Клиент отключился: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void sendResponse(Response response) {
        try {
            SendResponse.send(channel, response);
            logger.info("Ответ отправлен клиенту");
        } catch (IOException e) {
            logger.warning("Ошибка отправки: " + e.getMessage());
            close();
        }
    }

    private void close() {
        try { channel.close(); } catch (IOException ignored) {}
    }
}
