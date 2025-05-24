package it.polimi.ingsw.view.gui.helpers;

import javafx.scene.layout.StackPane;

public abstract class DropSlot extends StackPane {

    public DropSlot() {
        this.setPickOnBounds(true);
        DragDropManager.registerDropSlot(this);

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                DragDropManager.unregisterDropSlot(this);
            }
        });
    }

    protected abstract boolean canAccept(String dragId);
    protected abstract void acceptDrop(String dragId);
    protected void onHover(boolean enter) {}
}