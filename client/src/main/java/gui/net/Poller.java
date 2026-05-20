package gui.net;

import command.SessionContext;
import gui.Session;
import gui.model.CollectionStore;
import gui.util.ShowResponseParser;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import network.HumanBeingEntry;
import network.Request;
import network.Response;

import java.util.List;
import java.util.function.Consumer;

/**
 * Периодически опрашивает сервер командой show и синхронизирует
 * локальный CollectionStore. Используется ScheduledService —
 * экземпляр Task создаётся каждый тик заново (требование JavaFX).
 */
public class Poller extends ScheduledService<List<HumanBeingEntry>> {

    private static final Duration PERIOD = Duration.seconds(3);

    private final CollectionStore store;
    private Consumer<Throwable> errorHandler;

    public Poller(CollectionStore store) {
        this.store = store;
        setPeriod(PERIOD);
        setRestartOnFailure(false);

        setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<HumanBeingEntry> entries = (List<HumanBeingEntry>) getValue();
            Platform.runLater(() -> store.sync(entries));
        });
        setOnFailed(e -> {
            if (errorHandler != null) errorHandler.accept(getException());
        });
    }

    public void setErrorHandler(Consumer<Throwable> handler) {
        this.errorHandler = handler;
    }

    @Override
    protected Task<List<HumanBeingEntry>> createTask() {
        return new Task<>() {
            @Override
            protected List<HumanBeingEntry> call() throws Exception {
                SessionContext ctx = Session.get().context();
                Request request = new Request("show", new String[0], null,
                        ctx.getLogin(), ctx.getPasswordHash());
                Response response = Session.get().gateway().sendBlocking(request);
                return ShowResponseParser.parse(response);
            }
        };
    }
}
