package managers;

import exceptions.AccessToFileException;
import interfaces.Reader;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Scanner;

/**
 * Чтение ввода из файла скрипта (очередь файлов для execute_script).
 */
public class ScriptExecutorManager implements Reader {
    private static final ArrayDeque<String> filepaths = new ArrayDeque<>();
    private static final ArrayDeque<Scanner> reader = new ArrayDeque<>();

    public static String readfile() throws IOException {
        return reader.getFirst().nextLine();
    }

    public static void pushFile(String file_path) throws AccessToFileException, FileNotFoundException {
        File file = new File(file_path);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        if (!file.canRead()) {
            throw new AccessToFileException("Ошибка доступа к файлу");
        }
        reader.push(new Scanner(file));
        filepaths.push(file_path);
    }

    public static void popfile() throws IOException {
        reader.getFirst().close();
        reader.pop();
        filepaths.pop();
    }

    public static boolean fileReapeting(String filepath) {
        String absolutePath = new File(filepath).getAbsolutePath();
        return filepaths.contains(absolutePath);
    }

    /** Возвращает true, если в стеке есть файл со строками для чтения. */
    public static boolean hasNextInScript() {
        return !reader.isEmpty() && reader.getFirst().hasNextLine();
    }

    @Override
    public String nextLine() {
        try {
            return readfile();
        } catch (IOException e) {
            return "";
        }
    }
}
