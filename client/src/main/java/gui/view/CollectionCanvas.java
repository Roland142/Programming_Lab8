package gui.view;

import gui.model.CollectionStore;
import gui.model.HumanBeingFx;
import gui.util.UserColorAssigner;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Канвас с кругами — визуализация коллекции HumanBeing.
 * Каждый объект отрисовывается как Circle: позиция = координаты (x, y),
 * радиус — функция от impactSpeed, цвет — назначен по ownerLogin.
 *
 * Анимации:
 *  - добавление: радиус растёт от 0 до финального значения за {@link #ADD_MS} мс;
 *  - удаление: радиус сжимается до 0 за {@link #REMOVE_MS} мс, затем remove;
 *  - обновление позиции/радиуса: плавная интерполяция за {@link #UPDATE_MS} мс.
 */
public class CollectionCanvas extends Pane {

    private static final double ADD_MS = 350;
    private static final double REMOVE_MS = 250;
    private static final double UPDATE_MS = 200;

    /** Видимая область координат HumanBeing. */
    private static final double X_RANGE = 1000;  // по обе стороны от 0
    private static final double Y_RANGE = 600;

    private final CollectionStore store;
    private final Map<Long, Circle> circles = new HashMap<>();

    private Consumer<HumanBeingFx> onObjectClick;

    public CollectionCanvas(CollectionStore store) {
        this.store = store;
        setStyle("-fx-background-color: #2b2b2b;");
        setMinSize(0, 0);

        store.items().addListener((ListChangeListener<HumanBeingFx>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(this::animateRemove);
                }
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::animateAdd);
                }
            }
        });

        ChangeListener<Object> resizeListener = (obs, prev, value) -> repositionAll();
        widthProperty().addListener(resizeListener);
        heightProperty().addListener(resizeListener);
    }

    public void setOnObjectClick(Consumer<HumanBeingFx> handler) {
        this.onObjectClick = handler;
    }

    private void animateAdd(HumanBeingFx fx) {
        if (circles.containsKey(fx.getKey())) return;
        Circle circle = new Circle(0);
        circle.setStrokeWidth(0);
        circles.put(fx.getKey(), circle);
        getChildren().add(circle);

        bindCircle(circle, fx);
        positionCircle(circle, fx);

        Timeline appear = new Timeline(new KeyFrame(Duration.millis(ADD_MS),
                new KeyValue(circle.radiusProperty(), targetRadius(fx))));
        appear.play();
    }

    private void animateRemove(HumanBeingFx fx) {
        Circle circle = circles.remove(fx.getKey());
        if (circle == null) return;
        Timeline shrink = new Timeline(new KeyFrame(Duration.millis(REMOVE_MS),
                new KeyValue(circle.radiusProperty(), 0)));
        shrink.setOnFinished(e -> getChildren().remove(circle));
        shrink.play();
    }

    private void bindCircle(Circle circle, HumanBeingFx fx) {
        circle.setFill(UserColorAssigner.colorFor(fx.getOwnerLogin()));
        circle.setStroke(Color.web("#1f1f1f"));
        circle.setStrokeWidth(0);

        // Реагируем на изменения координат и скорости (после applyFrom)
        ChangeListener<Object> mover = (obs, prev, value) -> {
            Timeline move = new Timeline(new KeyFrame(Duration.millis(UPDATE_MS),
                    new KeyValue(circle.centerXProperty(), pixelX(fx.getX())),
                    new KeyValue(circle.centerYProperty(), pixelY(fx.getY())),
                    new KeyValue(circle.radiusProperty(), targetRadius(fx))));
            move.play();
        };
        fx.xProperty().addListener(mover);
        fx.yProperty().addListener(mover);
        fx.impactSpeedProperty().addListener(mover);
        fx.ownerLoginProperty().addListener((obs, prev, value) ->
                circle.setFill(UserColorAssigner.colorFor(value)));

        circle.setOnMouseEntered(e -> {
            circle.setStrokeWidth(2);
            setCursor(javafx.scene.Cursor.HAND);
        });
        circle.setOnMouseExited(e -> {
            circle.setStrokeWidth(0);
            setCursor(javafx.scene.Cursor.DEFAULT);
        });
        circle.setOnMouseClicked(e -> {
            if (onObjectClick != null) onObjectClick.accept(fx);
        });
    }

    private void positionCircle(Circle circle, HumanBeingFx fx) {
        circle.setCenterX(pixelX(fx.getX()));
        circle.setCenterY(pixelY(fx.getY()));
    }

    private void repositionAll() {
        for (HumanBeingFx fx : store.items()) {
            Circle circle = circles.get(fx.getKey());
            if (circle != null) positionCircle(circle, fx);
        }
    }

    private double pixelX(double xCoord) {
        double w = Math.max(getWidth(), 100);
        double centerPx = w / 2.0;
        return centerPx + (xCoord / X_RANGE) * (w / 2.0);
    }

    private double pixelY(int yCoord) {
        double h = Math.max(getHeight(), 100);
        double centerPx = h / 2.0;
        // y в JavaFX растёт вниз; инвертируем чтобы положительный y был вверху
        return centerPx - ((double) yCoord / Y_RANGE) * (h / 2.0);
    }

    private double targetRadius(HumanBeingFx fx) {
        double base = 12 + Math.min(48, Math.abs(fx.getImpactSpeed()) / 10.0);
        return base;
    }
}
