package command.interfaces;

import network.Request;

/**
 * Интерфейс клиентской команды.
 * Каждая реализация знает, как сформировать Request для отправки на сервер.
 */
public interface ClientCommand {
    /**
     * Формирует запрос для отправки на сервер.
     * @return Request или null, если отправлять не нужно
     */
    Request buildRequest(String commandName, String[] args, String login, String password);
}
