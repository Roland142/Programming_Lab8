package commands;

import network.Request;
import network.Response;

/**
 * Серверный обработчик execute_script.
 * Файл выполняется на клиенте — сервер только фиксирует команду в истории.
 */
public class ExecuteScript extends Command {

    public ExecuteScript() {
        super("execute_script");
    }

    @Override
    public Response execute(Request request) {
        return new Response("", true);
    }
}
