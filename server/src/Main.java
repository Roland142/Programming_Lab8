import core.Server;
import core.ServerInitializer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    private static final int PORT = 1111;
    private static final Logger logger = Logger.getLogger("Server");

    public static void main(String[] args) {
        setupLogger();

        ServerInitializer init;
        try {
            init = new ServerInitializer();
            logger.info("Инициализация завершена");
        } catch (SQLException | IOException e) {
            logger.severe("Ошибка инициализации: " + e.getMessage());
            return;
        }

        new Server(PORT, init.commandManager, init.authManager).start();
    }

    private static void setupLogger() {
        Logger rootLogger = Logger.getLogger("Server");
        rootLogger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        rootLogger.addHandler(handler);
        rootLogger.setLevel(Level.ALL);
    }
}
