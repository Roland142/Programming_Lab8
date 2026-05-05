package command.types;

/**
 * Перечисление всех команд клиента.
 */
public enum ClientCommandType {

    EXIT("exit", null),
    SAVE("save", CommandAction.BLOCKED),
    EXECUTE_SCRIPT("execute_script", CommandAction.SCRIPT),
    HELP("help", CommandAction.SEND),
    INFO("info", CommandAction.SEND),
    SHOW("show", CommandAction.SEND),
    CLEAR("clear", CommandAction.SEND),
    HISTORY("history", CommandAction.SEND),
    REMOVE_KEY("remove_key", CommandAction.SEND_WITH_ARG),
    REMOVE_GREATER_KEY("remove_greater_key", CommandAction.SEND_WITH_ARG),
    PRINT_ASCENDING("print_ascending", CommandAction.SEND),
    REMOVE_ALL_BY_MINUTES_OF_WAITING("remove_all_by_minutes_of_waiting", CommandAction.SEND_WITH_ARG),
    PRINT_FIELD_ASCENDING_IMPACT_SPEED("print_field_ascending_impact_speed", CommandAction.SEND),
    INSERT("insert", CommandAction.SEND_WITH_OBJ_AND_ARG),
    UPDATE("update", CommandAction.SEND_WITH_OBJ_AND_ARG),
    REMOVE_LOWER("remove_lower", CommandAction.SEND_WITH_OBJECT),
    REGISTER("register", CommandAction.AUTH),
    LOGIN("login", CommandAction.AUTH);

    private final String name;
    private final CommandAction action;

    ClientCommandType(String name, CommandAction action) {
        this.name = name;
        this.action = action;
    }

    public static ClientCommandType fromString(String commandName) {
        for (ClientCommandType cmd : values()) {
            if (cmd.name.equals(commandName)) return cmd;
        }
        return null;
    }

    public String getName() { return name; }
    public CommandAction getAction() { return action; }
}
