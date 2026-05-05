package managers;

import interfaces.Reader;
import java.util.Scanner;

/**
 * Реализация Reader для ввода с консоли.
 */
public class ManualInput implements Reader {
    private static final Scanner userScanner = UserScanner.getUserScanner();

    @Override
    public String nextLine() {
        return userScanner.nextLine();
    }
}
