package gui.util;

import network.HumanBeingEntry;
import network.Response;

import java.util.Collections;
import java.util.List;

public final class ShowResponseParser {

    private ShowResponseParser() {}

    @SuppressWarnings("unchecked")
    public static List<HumanBeingEntry> parse(Response response) {
        if (response == null || !response.isSuccess()) return Collections.emptyList();
        Object payload = response.getPayload();
        if (payload instanceof List<?>) {
            return (List<HumanBeingEntry>) payload;
        }
        return Collections.emptyList();
    }
}
