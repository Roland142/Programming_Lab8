package network;

import elements.HumanBeing;

import java.io.Serializable;

/**
 * Запись коллекции HumanBeing вместе с метаданными хранения:
 * map_key (ключ TreeMap на сервере) и owner_login (логин владельца).
 * Используется для передачи структурированной коллекции клиенту в Response.payload.
 */
public class HumanBeingEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long key;
    private final HumanBeing humanBeing;
    private final String ownerLogin;

    public HumanBeingEntry(long key, HumanBeing humanBeing, String ownerLogin) {
        this.key = key;
        this.humanBeing = humanBeing;
        this.ownerLogin = ownerLogin;
    }

    public long getKey() { return key; }

    public HumanBeing getHumanBeing() { return humanBeing; }

    public String getOwnerLogin() { return ownerLogin; }
}
