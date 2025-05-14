package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.util.Default;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class CoveredTilesPane extends Pane {

    private static final int COVERED_TILE_SIZE = 80;
    private static final int COVERED_PANE_SIZE = 400;
    private static final int MAX_RANDOM_POSITION = 240;

    public CoveredTilesPane() {
        super();
        double x,y;

        this.setBackground(new Background(
                new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        this.setPrefSize(COVERED_PANE_SIZE, COVERED_PANE_SIZE);

        for (int i = 0; i < Default.TOTAL_TILES_NUMBER; i++) {
            DraggableTile tile = new DraggableTile();

            x = Math.random() * MAX_RANDOM_POSITION;
            y = Math.random() * MAX_RANDOM_POSITION;

            tile.setSize(COVERED_TILE_SIZE, COVERED_TILE_SIZE);
            tile.setLayoutX(x);
            tile.setLayoutY(y);

            this.getChildren().add(tile);
        }
    }
}
