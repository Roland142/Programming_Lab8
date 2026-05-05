package managers;

/**
 * Флаг режима ввода: консоль или файл скрипта.
 */
public class FileMod {
    public static boolean isFileMod = false;

    public static void setFileMod(boolean fileMod) {
        isFileMod = fileMod;
    }
}
