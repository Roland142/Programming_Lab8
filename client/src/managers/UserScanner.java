package managers;

import java.util.Scanner;

/**
 * Единый экземпляр Scanner для ввода с консоли.
 */
public class UserScanner {
    public static Scanner customScanner = new Scanner(System.in);

    public static Scanner getUserScanner() {
        return customScanner;
    }
}
