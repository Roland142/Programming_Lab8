package exceptions;

public class AccessToFileException extends RuntimeException {
    public AccessToFileException(String message) {
        super(message);
    }
}
