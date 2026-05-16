package gui.i18n;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Синглтон, хранящий выбранную пользователем локализацию.
 * Все элементы UI получают локализованные строки через Localizer,
 * который пересчитывает свои биндинги при изменении localeProperty().
 */
public final class LocaleManager {

    public static final Locale RU = new Locale("ru");
    public static final Locale MK = new Locale("mk");
    public static final Locale FR = new Locale("fr");
    public static final Locale ES_NI = new Locale("es", "NI");

    public static final List<Locale> SUPPORTED = List.of(RU, MK, FR, ES_NI);

    private static final String BUNDLE_NAME = "i18n.messages";

    private static final LocaleManager INSTANCE = new LocaleManager();

    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", RU);

    private LocaleManager() {}

    public static LocaleManager get() {
        return INSTANCE;
    }

    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale value) {
        if (value == null) throw new IllegalArgumentException("Locale не может быть null");
        locale.set(value);
    }

    /** Возвращает строку текущей локализации по ключу */
    public String tr(String key) {
        return tr(key, getLocale());
    }

    public String tr(String key, Locale forLocale) {
        try {
            return ResourceBundle.getBundle(BUNDLE_NAME, forLocale).getString(key);
        } catch (MissingResourceException e) {
            return "??" + key + "??";
        }
    }

    /** Возвращает строку с подстановкой аргументов через MessageFormat */
    public String tr(String key, Object... args) {
        String pattern = tr(key);
        if (args == null || args.length == 0) return pattern;
        return new MessageFormat(pattern, getLocale()).format(args);
    }

    /** Имя локализации для отображения в UI (берётся из ключей locale.* текущего бандла) */
    public String displayName(Locale forLocale) {
        String tag = forLocale.equals(ES_NI) ? "es_NI" : forLocale.getLanguage();
        return tr("locale." + tag);
    }
}
