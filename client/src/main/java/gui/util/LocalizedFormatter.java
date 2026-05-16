package gui.util;

import gui.i18n.LocaleManager;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

/**
 * Форматирование чисел и дат согласно текущей локализации.
 * Все методы читают активную локализацию из LocaleManager, поэтому при
 * пересчёте StringBinding (Localizer) числа/даты обновятся вместе с подписями.
 */
public final class LocalizedFormatter {

    private LocalizedFormatter() {}

    private static Locale active() {
        return LocaleManager.get().getLocale();
    }

    public static String formatDouble(double value) {
        return NumberFormat.getNumberInstance(active()).format(value);
    }

    public static String formatLong(long value) {
        return NumberFormat.getIntegerInstance(active()).format(value);
    }

    public static String formatInteger(int value) {
        return NumberFormat.getIntegerInstance(active()).format(value);
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(active())
                .format(ld);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(active())
                .format(ldt);
    }

    public static String formatCoordinates(double x, int y) {
        return "(" + formatDouble(x) + ", " + formatLong(y) + ")";
    }
}
