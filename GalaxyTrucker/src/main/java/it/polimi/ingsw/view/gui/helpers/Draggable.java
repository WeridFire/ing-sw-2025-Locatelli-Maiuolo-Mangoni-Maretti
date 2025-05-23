package it.polimi.ingsw.view.gui.helpers;

import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

public abstract class Draggable extends StackPane {

    public Draggable() {
        this.setOnDragDetected(this::onDragDetected);
        this.setOnDragDone(this::onDragDone);
    }

    private void onDragDetected(MouseEvent event) {
        if (!canBeDragged() || !event.isPrimaryButtonDown()) return;

        // start drag and drop
        onBeginDrag();
        Dragboard db = startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(getDragId());
        db.setContent(content);

        // snapshot as visual feedback
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);  // semi-transparent
        params.setTransform(Transform.scale(1.25, 1.25));
        WritableImage snapshot = this.snapshot(params, null);
        db.setDragView(snapshot, snapshot.getWidth() / 2, snapshot.getHeight() / 2);
        // set original as invisible
        this.setVisible(false);

        this.setMouseTransparent(true);
        event.consume();
    }

    private void onDragDone(DragEvent event) {
        this.setMouseTransparent(false);

        onEndDrag();

        if (event.getTransferMode() == null) {
            // none accepted the drop -> fallback
            fallbackDropHandler();
        }

        event.consume();
    }

    protected abstract String getDragId();

    protected boolean canBeDragged() { return true; }

    protected void onBeginDrag() { }

    protected void onEndDrag() { }

    protected void fallbackDropHandler() { }
}