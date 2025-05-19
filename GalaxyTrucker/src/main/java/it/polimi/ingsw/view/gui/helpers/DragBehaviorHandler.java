package it.polimi.ingsw.view.gui.helpers;

import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.components.DraggableTile;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;

public class DragBehaviorHandler {

    public static void setGeneralDropBehavior(Pane pane) {
        pane.setOnMouseDragReleased(event -> {
            System.out.println("Drag behavior released");
            DraggableTile t = AssembleUI.getIsBeeingDragged();
            if (t.getPosition() == WhichPane.FLOATING && event.getButton() == MouseButton.PRIMARY) {
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("discard"); // Discard the tile
                });
            }
            pane.getChildren().remove(t);
            t.setVisible(false);
            AssembleUI.setIsBeeingDragged(null);
            event.consume();
        });
    }

}
