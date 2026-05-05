package command.exceptions;

public class ServerUnavailableException extends Exception {
    public ServerUnavailableException() {
        super("Сервер недоступен");
    }
}
