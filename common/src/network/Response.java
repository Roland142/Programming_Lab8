package network;

import java.io.Serializable;

/**
 * Объект ответа от сервера клиенту.
 * Содержит текст результата выполнения команды и флаг успеха.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final boolean success;

    public Response(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() { return message; }

    public boolean isSuccess() { return success; }
}
