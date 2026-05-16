package gui.i18n;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;

import java.util.concurrent.Callable;

/**
 * Утилита для биндинга строковых свойств UI к ключам ResourceBundle
 * При смене LocaleManager.localeProperty() все привязанные подписи автоматически пересчитываются.
 */
public final class Localizer {

    private Localizer() {}

    public static StringBinding binding(String key) {
        Callable<String> compute = () -> LocaleManager.get().tr(key);
        return Bindings.createStringBinding(compute, LocaleManager.get().localeProperty());
    }

    public static StringBinding binding(String key, Object... args) {
        Callable<String> compute = () -> LocaleManager.get().tr(key, args);
        return Bindings.createStringBinding(compute, LocaleManager.get().localeProperty());
    }

    /** Биндит StringProperty (например, label.textProperty()) к ключу. */
    public static void bind(StringProperty property, String key) {
        property.bind(binding(key));
    }

    public static void bind(StringProperty property, String key, Object... args) {
        property.bind(binding(key, args));
    }
}
