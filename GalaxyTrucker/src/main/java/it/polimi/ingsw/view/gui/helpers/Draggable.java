package it.polimi.ingsw.view.gui.helpers;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.gui.components.LoadableObject;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;

public abstract class Draggable extends StackPane {

    private final Pane dragOverlay;
    private boolean dragging = false;

    public Draggable(Pane dragOverlay) {
        this.dragOverlay = dragOverlay;
        this.setOnMousePressed(this::onMousePressed);
    }

    private void onMousePressed(MouseEvent event) {
        if (!canBeDragged()) return;

        onBeginDrag();

        this.setVisible(false);
        dragging = true;

        DragDropManager.startDrag(this, getSnapshot(), dragOverlay, event);
        event.consume();
    }

    protected final WritableImage getSnapshot() {
        boolean visible = this.isVisible();
        this.setVisible(true);
        // Create a transparent snapshot of the node
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = this.snapshot(params, null);
        this.setVisible(visible);  // reset previous visibility
        return snapshot;
    }

    protected final boolean isDragging() {
        return dragging;
    }

    protected final void endDrag(boolean dropAccepted) {
        if (!dragging) return;

        this.setVisible(true);
        dragging = false;

        if (!dropAccepted) {
            fallbackDropHandler();
        }

        onEndDrag(dropAccepted);
    }

    protected abstract String getDragId();

    protected boolean canBeDragged() { return !dragging; }

    protected void onBeginDrag() { }

    protected void onKeyPressedWhileDragging(KeyCode code) { }

    protected void onEndDrag(boolean dropAccepted) { }

    protected void fallbackDropHandler() { }

    public LoadableType getType(){
        return null;
    }
}
