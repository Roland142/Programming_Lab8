package gui;

/**
 * Класс-посредник для запуска JavaFX-приложения из fat-jar.
 * Без него JRE требует наличия модулей JavaFX в module-path,
 * а main-класс, наследующий Application, отказывается запускаться.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
