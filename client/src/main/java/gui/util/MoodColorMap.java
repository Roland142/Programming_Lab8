package gui.util;

import elements.Mood;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

/** Цвет для каждого настроения (для отображения в таблице и popup). */
public final class MoodColorMap {

    private MoodColorMap() {}

    private static final Map<Mood, Color> COLORS = new EnumMap<>(Mood.class);
    static {
        COLORS.put(Mood.SADNESS, Color.web("#5b8aa3"));
        COLORS.put(Mood.GLOOM,   Color.web("#7a6e8a"));
        COLORS.put(Mood.APATHY,  Color.web("#7f7f7f"));
        COLORS.put(Mood.CALM,    Color.web("#5d8b6a"));
    }

    public static Color colorFor(Mood mood) {
        if (mood == null) return Color.web("#888888");
        return COLORS.getOrDefault(mood, Color.web("#888888"));
    }

    public static String hexFor(Mood mood) {
        Color c = colorFor(mood);
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
