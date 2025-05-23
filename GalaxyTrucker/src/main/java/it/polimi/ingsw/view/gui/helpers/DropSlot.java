package it.polimi.ingsw.view.gui.helpers;

import javafx.scene.input.*;
import javafx.scene.layout.StackPane;

public abstract class DropSlot extends StackPane {

    public DropSlot() {
        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString() &&
                    canAccept(event.getDragboard().getString())) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        this.setOnDragEntered(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                onHover(true);
            }
            event.consume();
        });

        this.setOnDragExited(event -> {
            onHover(false);
            event.consume();
        });

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && canAccept(db.getString())) {
                acceptDrop(db.getString());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /** Override to define which ID to accept */
    protected abstract boolean canAccept(String dragId);

    /** Override to manage drop of the draggable */
    protected abstract void acceptDrop(String dragId);

    /** Override to show/hide hovering style */
    protected void onHover(boolean entering) { }
}