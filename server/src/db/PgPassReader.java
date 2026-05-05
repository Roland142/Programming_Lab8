package db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Читает credentials из ~/.pgpass. */
public class PgPassReader {

    public static String[] readCredentials() throws IOException {
        Path pgPass = Path.of(System.getProperty("user.home"), ".pgpass");
        String line = Files.lines(pgPass)
                .filter(l -> !l.startsWith("#") && !l.isBlank())
                .findFirst()
                .orElseThrow(() -> new IOException("Файл .pgpass пуст или не найден"));
        String[] parts = line.split(":");
        return new String[]{parts[3], parts[4]};
    }
}
