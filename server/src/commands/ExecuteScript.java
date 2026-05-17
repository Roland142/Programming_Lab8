package commands;

import network.Request;
import network.Response;

/**
 * Серверный обработчик execute_script.
 * Файл выполняется на клиенте — сервер только фиксирует команду в истории.
 */
public class ExecuteScript extends Command {

    public ExecuteScript() {
        super("execute_script", "execute_script {file_name}", "help.command.execute_script",
                "исполнить скрипт из файла");
    }

    @Override
    public Response execute(Request request) {
        return new Response("", true);
    }
}
