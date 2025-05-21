package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.view.gui.helpers.DragBehaviorHandler;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import javafx.scene.layout.GridPane;

import java.util.List;

public class DrawnTilesGrid extends GridPane {

    private final int DEF_ROWS = 20, DEF_COLS = 8;

    public DrawnTilesGrid() {
        updateTilesGrid();
        DragBehaviorHandler.setGeneralDropBehavior(this);
    }

    public void updateTilesGrid(){
        GameData game = AssembleState.getLastUpdate().getCurrentGame();
        List<TileSkeleton> drawnTiles = game.getUncoveredTiles();
        int counter = 0;

        this.getChildren().clear();
        for (int r = 0; r < DEF_ROWS; r++) {
            for (int c = 0; c < DEF_COLS; c++) {
                if (counter < drawnTiles.size()) {
                    DraggableTile draggableTile = new DraggableTile(drawnTiles.get(counter));
                    draggableTile.setPosition(WhichPane.DRAWN);
                    counter++;
                    this.add(draggableTile, c, r);
                }
            }
        }
    }
}
