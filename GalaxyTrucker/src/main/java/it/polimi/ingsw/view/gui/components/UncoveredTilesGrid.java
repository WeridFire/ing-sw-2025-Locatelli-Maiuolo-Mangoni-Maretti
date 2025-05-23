package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import javafx.scene.layout.GridPane;

import java.util.List;

public class UncoveredTilesGrid extends GridPane {

    public static final int N_COLS = 7;

    public UncoveredTilesGrid() {
        update();
    }

    public void update(){
        GameData game = AssembleState.getLastUpdate().getCurrentGame();
        List<TileSkeleton> uncoveredTiles = game.getUncoveredTiles();

        this.getChildren().clear();

        int row = 0, col = 0;
        for (TileSkeleton uncoveredTile : uncoveredTiles) {
            ShipTile draggableTile = new ShipTile(uncoveredTile);
            this.add(draggableTile, col, row);

            col++;
            if (col == N_COLS) {
                col = 0;
                row++;
            }
        }
    }
}
