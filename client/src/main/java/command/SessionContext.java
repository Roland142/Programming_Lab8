package command;

/** Хранит учётные данные текущей сессии (логин + хеш пароля). */
public class SessionContext {

    private String login;
    private String passwordHash;

    public void setCredentials(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public String getLogin() { return login; }

    public String getPasswordHash() { return passwordHash; }
}
