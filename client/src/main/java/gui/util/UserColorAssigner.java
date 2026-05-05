package gui.util;

import javafx.scene.paint.Color;

/**
 * Детерминированное назначение цвета пользователю по логину.
 * Один и тот же логин на любом клиенте получит один и тот же цвет.
 */
public final class UserColorAssigner {

    private UserColorAssigner() {}

    private static final Color[] PALETTE = new Color[] {
            Color.web("#c8593a"),  // терракот
            Color.web("#5d8b6a"),  // зелёный
            Color.web("#a772a4"),  // фиолетовый
            Color.web("#4a6f8a"),  // синий
            Color.web("#c79a3e"),  // охра
            Color.web("#8a4a6f"),  // розовый-тёмный
            Color.web("#6e655a"),  // серо-коричневый
            Color.web("#4a8a89"),  // бирюзовый
    };

    public static Color colorFor(String login) {
        if (login == null || login.isBlank()) return PALETTE[0];
        int idx = Math.floorMod(login.hashCode(), PALETTE.length);
        return PALETTE[idx];
    }

    public static String hexFor(String login) {
        Color c = colorFor(login);
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
