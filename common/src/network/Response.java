package network;

import java.io.Serializable;

/**
 * Объект ответа от сервера клиенту.
 * Содержит текст результата, флаг успеха и опциональный структурированный payload
 * (например, список элементов коллекции для GUI-клиента).
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 2L;

    private final String message;
    private final boolean success;
    private final Object payload;

    public Response(String message, boolean success) {
        this(message, success, null);
    }

    public Response(String message, boolean success, Object payload) {
        this.message = message;
        this.success = success;
        this.payload = payload;
    }

    public String getMessage() { return message; }

    public boolean isSuccess() { return success; }

    public Object getPayload() { return payload; }
}
