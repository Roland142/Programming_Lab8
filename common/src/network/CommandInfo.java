package network;

import java.io.Serializable;

/** Structured metadata for a command shown by GUI help. */
public class CommandInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String usage;
    private final String descriptionKey;
    private final String fallbackDescription;

    public CommandInfo(String name, String usage, String descriptionKey, String fallbackDescription) {
        this.name = name;
        this.usage = usage;
        this.descriptionKey = descriptionKey;
        this.fallbackDescription = fallbackDescription;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getFallbackDescription() {
        return fallbackDescription;
    }
}
