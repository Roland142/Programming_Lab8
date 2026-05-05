package exceptions;

/**
 * Исключение при выполнении операции над пустой коллекцией.
 */
public class EmptyCollectionException extends RuntimeException {
    public EmptyCollectionException() {
        super("Коллекция пуста");
    }
}
