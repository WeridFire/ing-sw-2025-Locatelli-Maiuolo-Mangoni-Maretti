package it.polimi.ingsw.view.gui.helpers;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Set;

public class DragDropManager {

    private static final Set<DropSlot> dropSlots = new HashSet<>();
    private static final double opacity = 0.85;
    private static final double scaleFactor = 1.25;

    private static Draggable current;
    private static double startX, startY;
    private static double startPosX, startPosY;

    private static Scene scene;
    private static Pane overlayPane;
    private static ImageView dragOverlay;
    private static DropSlot currentHover;

    public static void registerDropSlot(DropSlot slot) {
        dropSlots.add(slot);
    }

    public static void unregisterDropSlot(DropSlot slot) {
        dropSlots.remove(slot);
    }

    public static void startDrag(Draggable d, WritableImage snapshot, Pane overlayPane, MouseEvent event) {
        current = d;
        DragDropManager.overlayPane = overlayPane;

        Node dParent = d.getParent();
        Bounds draggableBoundsInScene = dParent.localToScene(dParent.getBoundsInLocal());
        Bounds overlayPaneBoundsInScene = overlayPane.localToScene(overlayPane.getBoundsInLocal());
        double offsetX = overlayPaneBoundsInScene.getMinX() - draggableBoundsInScene.getMinX();
        double offsetY = overlayPaneBoundsInScene.getMinY() - draggableBoundsInScene.getMinY();

        startPosX = current.getLayoutX() - offsetX;
        startPosY = current.getLayoutY() - offsetY;
        startX = event.getSceneX();
        startY = event.getSceneY();

        updateSnapshot(snapshot);

        // Start listening for drag events
        scene = overlayPane.getScene();
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, DragDropManager::trackHover);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, DragDropManager::trackHover);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, DragDropManager::handleRelease);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, DragDropManager::handleKeyPress);
    }

    public static void updateOverlay(double sceneX, double sceneY) {
        if (dragOverlay != null) {
            dragOverlay.setTranslateX(sceneX - startX);
            dragOverlay.setTranslateY(sceneY - startY);
        }
    }

    public static void endDrag(boolean dropAccepted) {
        if (overlayPane != null && dragOverlay != null) {
            overlayPane.getChildren().remove(dragOverlay);
        }

        scene.removeEventFilter(MouseEvent.MOUSE_MOVED, DragDropManager::trackHover);
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED, DragDropManager::trackHover);
        scene.removeEventFilter(MouseEvent.MOUSE_RELEASED, DragDropManager::handleRelease);

        if (currentHover != null) {
            currentHover.onHover(false);
            currentHover = null;
        }
        if (current != null) {
            current.endDrag(dropAccepted);
            current = null;
        }

        dragOverlay = null;
    }

    public static Draggable getCurrentDraggable() {
        return current;
    }

    public static void updateSnapshot(WritableImage snapshot) {
        double oldTranslateX = 0, oldTranslateY = 0;
        if (dragOverlay != null) {
            oldTranslateX = dragOverlay.getTranslateX();
            oldTranslateY = dragOverlay.getTranslateY();
            if (overlayPane != null) {
                overlayPane.getChildren().remove(dragOverlay);
            }
        }

        dragOverlay = new ImageView(snapshot);
        dragOverlay.setMouseTransparent(true);
        dragOverlay.setOpacity(opacity);
        dragOverlay.setScaleX(scaleFactor);
        dragOverlay.setScaleY(scaleFactor);
        dragOverlay.setLayoutX(startPosX);
        dragOverlay.setLayoutY(startPosY);
        updateOverlay(oldTranslateX + startX, oldTranslateY + startY);

        if (overlayPane != null) {
            overlayPane.getChildren().add(dragOverlay);
        }
    }

    private static void trackHover(MouseEvent event) {
        if (current == null) return;

        double x = event.getSceneX();
        double y = event.getSceneY();
        DropSlot hovered = null;

        for (DropSlot slot : dropSlots) {
            Bounds b = slot.localToScene(slot.getBoundsInLocal());
            if (b.contains(x, y) && slot.canAccept(current.getDragId())) {
                hovered = slot;
                break;
            }
        }

        if (hovered != currentHover) {
            if (currentHover != null) currentHover.onHover(false);
            if (hovered != null) hovered.onHover(true);
            currentHover = hovered;
        }

        updateOverlay(x, y);
        event.consume();
    }

    private static void handleRelease(MouseEvent event) {
        boolean dropAccepted = false;
        if (currentHover != null && current != null) {
            if (currentHover.canAccept(current.getDragId())) {
                currentHover.acceptDrop(current.getDragId());
                dropAccepted = true;
            }
        }
        endDrag(dropAccepted);
        event.consume();
    }

    private static void handleKeyPress(KeyEvent event) {
        if (current == null) return;
        current.onKeyPressedWhileDragging(event.getCode());
        event.consume();
    }
}
