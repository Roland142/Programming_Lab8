package utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Утилита для хеширования паролей алгоритмом SHA-224.
 * Используется и на клиенте, и на сервере.
 */
public class HashUtil {

    private HashUtil() {}

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-224");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return String.format("%056x", new BigInteger(1, bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-224 недоступен", e);
        }
    }
}
