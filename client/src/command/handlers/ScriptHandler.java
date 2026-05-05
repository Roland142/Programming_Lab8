package command.handlers;

import managers.FileMod;
import managers.ScriptExecutorManager;

import java.io.FileNotFoundException;

/** Обрабатывает команду execute_script: открывает файл и переключает режим ввода. */
public class ScriptHandler {

    public void handle(String filePath) {
        if (filePath.isBlank()) {
            System.out.println("Ошибка. Не указан путь к файлу");
            return;
        }
        if (ScriptExecutorManager.fileReapeting(filePath)) {
            System.out.println("Найдена рекурсия: скрипт " + filePath + " уже выполняется! Его выполнение пропущено");
            return;
        }
        try {
            ScriptExecutorManager.pushFile(filePath);
            FileMod.setFileMod(true);
            System.out.println("Выполняется скрипт: " + filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + filePath);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
