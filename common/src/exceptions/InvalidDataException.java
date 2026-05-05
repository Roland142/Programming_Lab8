package exceptions;

/**
 * Исключение при некорректных данных
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
