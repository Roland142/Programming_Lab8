package gui;

import command.SessionContext;
import gui.net.GuiGateway;

/**
 * Глобальное состояние GUI-приложения: учётные данные текущего пользователя
 * и единственный сетевой шлюз.
 */
public final class Session {

    private static final Session INSTANCE = new Session();

    private final SessionContext context = new SessionContext();
    private final GuiGateway gateway = new GuiGateway();

    private Session() {}

    public static Session get() {
        return INSTANCE;
    }

    public SessionContext context() {
        return context;
    }

    public GuiGateway gateway() {
        return gateway;
    }

    public boolean isAuthenticated() {
        return context.getLogin() != null && context.getPasswordHash() != null;
    }

    public void clear() {
        context.setCredentials(null, null);
    }
}
