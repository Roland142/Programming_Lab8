package command;

import managers.FileMod;
import managers.ScriptExecutorManager;
import managers.UserScanner;

import java.io.IOException;
import java.util.Scanner;

/** Читает следующую команду из консоли или из текущего файла скрипта. */
public class CommandReader {

    private final Scanner console = UserScanner.getUserScanner();

    /**
     * @return строку с командой, "" если итерацию надо пропустить, null при EOF консоли
     */
    public String readLine() {
        if (FileMod.isFileMod) {
            if (!ScriptExecutorManager.hasNextInScript()) {
                try { ScriptExecutorManager.popfile(); } catch (IOException ignored) {}
                FileMod.setFileMod(ScriptExecutorManager.hasNextInScript());
                return "";
            }
            try {
                return ScriptExecutorManager.readfile();
            } catch (IOException e) {
                System.out.println("Ошибка чтения скрипта: " + e.getMessage());
                FileMod.setFileMod(false);
                return "";
            }
        }
        System.out.println("Введите команду:");
        if (!console.hasNextLine()) return null;
        return console.nextLine();
    }
}
