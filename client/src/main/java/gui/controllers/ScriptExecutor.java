package gui.controllers;

import elements.Car;
import elements.Coordinates;
import elements.HumanBeing;
import elements.Mood;
import exceptions.InvalidDataException;
import gui.Session;
import gui.i18n.LocaleManager;
import gui.util.Dialogs;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import network.Request;
import network.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

final class ScriptExecutor {
    private final Supplier<Window> windowSupplier;
    private final Runnable afterScript;

    ScriptExecutor(Supplier<Window> windowSupplier, Runnable afterScript) {
        this.windowSupplier = windowSupplier;
        this.afterScript = afterScript;
    }

    void chooseAndRun() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(LocaleManager.get().tr("script.choose"));
        File file = chooser.showOpenDialog(windowSupplier.get());
        if (file == null) return;

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException ex) {
            Dialogs.info(windowSupplier.get(), "script.title", ex.getMessage());
            return;
        }
        if (lines.isEmpty()) {
            Dialogs.info(windowSupplier.get(), "script.title",
                    LocaleManager.get().tr("script.empty"));
            return;
        }

        Thread runner = new Thread(() -> runLines(lines), "script-runner");
        runner.setDaemon(true);
        runner.start();
    }

    private void runLines(List<String> lines) {
        StringBuilder log = new StringBuilder();
        int executed = 0;
        ScriptCursor cursor = new ScriptCursor(lines);
        while (cursor.hasNext()) {
            String line = cursor.nextCommandLine();
            if (line == null) break;
            int lineNumber = cursor.lastLineNumber();
            String[] parts = line.split("\\s+");
            String cmd = parts[0];
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                if ("exit".equals(cmd)) {
                    log.append(LocaleManager.get().tr("script.line", lineNumber, "exit"))
                       .append('\n');
                    break;
                }
                if ("execute_script".equals(cmd)) {
                    log.append(LocaleManager.get().tr("script.line", lineNumber,
                            "skip — nested execute_script"))
                       .append('\n');
                    continue;
                }
                var ctx = Session.get().context();
                Request req = buildScriptRequest(cmd, args, cursor,
                        ctx.getLogin(), ctx.getPasswordHash());
                Response resp = Session.get().gateway().sendBlocking(req);
                log.append(LocaleManager.get().tr("script.line", lineNumber, resp.getMessage()))
                   .append('\n');
                executed++;
            } catch (Exception ex) {
                log.append(LocaleManager.get().tr("script.line", lineNumber, ex.getMessage()))
                   .append('\n');
            }
        }
        int finalExecuted = executed;
        String message = LocaleManager.get().tr("script.executed", finalExecuted)
                + "\n\n" + log;
        Platform.runLater(() -> {
            afterScript.run();
            Dialogs.info(windowSupplier.get(), "script.title", message);
        });
    }

    private Request buildScriptRequest(String cmd, String[] args, ScriptCursor cursor,
                                       String login, String passwordHash) {
        if ("insert".equals(cmd) && args.length == 0) {
            throw new IllegalArgumentException("insert requires key");
        }
        if ("update".equals(cmd) && args.length == 0) {
            throw new IllegalArgumentException("update requires id");
        }
        HumanBeing hb = switch (cmd) {
            case "insert", "update", "remove_lower" -> readHumanBeingFromScript(cursor);
            default -> null;
        };
        if (hb == null && !isStatelessCommand(cmd)) {
            throw new IllegalArgumentException("unsupported command: " + cmd);
        }
        return new Request(cmd, args, hb, login, passwordHash);
    }

    private HumanBeing readHumanBeingFromScript(ScriptCursor cursor) {
        try {
            String name = cursor.nextValue("name");
            double x = Double.parseDouble(cursor.nextValue("x").replace(',', '.'));
            int y = Integer.parseInt(cursor.nextValue("y"));
            boolean realHero = parseBoolean(cursor.nextValue("realHero"));
            Boolean hasToothpick = parseNullableBoolean(cursor.nextValue("hasToothpick"));
            double impactSpeed = Double.parseDouble(cursor.nextValue("impactSpeed").replace(',', '.'));
            String soundtrackName = cursor.nextValue("soundtrackName");
            int minutesOfWaiting = Integer.parseInt(cursor.nextValue("minutesOfWaiting"));
            Mood mood = parseMood(cursor.nextValue("mood"));
            String carName = cursor.nextValue("car");
            Car car = isNullToken(carName) ? null : new Car(carName);
            return new HumanBeing(name, new Coordinates(x, y), realHero,
                    hasToothpick, impactSpeed, soundtrackName, minutesOfWaiting, mood, car);
        } catch (InvalidDataException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid number: " + e.getMessage(), e);
        }
    }

    private static boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IllegalArgumentException("expected true or false, got: " + value);
    }

    private static Boolean parseNullableBoolean(String value) {
        if (isNullToken(value)) return null;
        return parseBoolean(value);
    }

    private static Mood parseMood(String value) {
        if (isNullToken(value)) return null;
        return Mood.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private static boolean isNullToken(String value) {
        return value == null || value.isBlank()
                || "-".equals(value)
                || "null".equalsIgnoreCase(value);
    }

    private static boolean isStatelessCommand(String cmd) {
        return Set.of(
                "info", "help", "history", "show", "clear",
                "remove_key", "remove_greater_key",
                "remove_all_by_minutes_of_waiting",
                "print_ascending", "print_field_ascending_impact_speed"
        ).contains(cmd);
    }

    private static final class ScriptCursor {
        private final List<String> lines;
        private int index;
        private int lastLineNumber;

        private ScriptCursor(List<String> lines) {
            this.lines = lines;
        }

        private boolean hasNext() {
            return index < lines.size();
        }

        private int lastLineNumber() {
            return lastLineNumber;
        }

        private String nextCommandLine() {
            while (index < lines.size()) {
                String line = lines.get(index).trim();
                lastLineNumber = index + 1;
                index++;
                if (!line.isEmpty()) return line;
            }
            return null;
        }

        private String nextValue(String fieldName) {
            String value = nextCommandLine();
            if (value == null) {
                throw new IllegalArgumentException("missing field: " + fieldName);
            }
            return value;
        }
    }
}
