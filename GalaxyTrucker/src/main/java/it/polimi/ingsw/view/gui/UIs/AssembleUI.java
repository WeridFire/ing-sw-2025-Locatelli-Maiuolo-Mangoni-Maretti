package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.view.gui.components.CoveredTilesPane;
import it.polimi.ingsw.view.gui.components.DraggableTile;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.List;

/**
 * UI component for the ship assembly phase of the game.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

    private final GridPane mainGrid;
    private final GridPane topGrid;
    private final GridPane leftGrid;
    private Pane rightPane;

    public static DraggableTile isBeeingDragged;
    public static void setIsBeeingDragged(DraggableTile isBeeingDragged) {
        AssembleUI.isBeeingDragged = isBeeingDragged;
    }
    /**
     * Creates a new assembly UI with all required components.
     */
    public AssembleUI() {
        mainGrid = new GridPane();

        topGrid = createDrawnTilesGrid(3, 8);
        leftGrid = createShipGrid();
        rightPane = createCoveredTilesPane();

        // Setup layout
        mainGrid.add(topGrid, 0, 0, 2, 1);
        mainGrid.add(leftGrid, 0, 1);
        mainGrid.add(rightPane, 1, 1);

        configureGridBehavior();
    }

    /**
     * Configures grid components to resize properly.
     */
    private void configureGridBehavior() {
        for (Node node : new Node[]{topGrid, leftGrid, rightPane}) {
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setVgrow(node, Priority.ALWAYS);
        }
    }

    /**
     * Creates the grid for displaying drawn tiles.
     */
    private GridPane createDrawnTilesGrid(int rows, int cols) {
        GameData game = AssembleState.getLastUpdate().getCurrentGame();
        List<TileSkeleton> drawnTiles = game.getUncoveredTiles();
        int counter = 0;

        GridPane grid = new GridPane();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (counter < drawnTiles.size()) {
                    DraggableTile draggableTile = new DraggableTile(drawnTiles.get(counter));
                    counter++;
                    grid.add(draggableTile, c, r);
                }
            }
        }

        return grid;
    }

    /**
     * Creates the ship grid with dimensions based on game level.
     */
    private GridPane createShipGrid() {
        int rows, cols;

        cols = switch (LobbyState.getGameLevel()) {
            case TESTFLIGHT, ONE -> {
                rows = 5;
                yield 7;
            }
            case TWO -> {
                rows = 6;
                yield 9;
            }
        };

        return new ShipGrid(rows, cols);
    }

    /**
     * Creates the pane containing covered tiles.
     */
    private Pane createCoveredTilesPane() {
        rightPane = new CoveredTilesPane();
        return rightPane;
    }

    /**
     * Returns the main layout for this UI.
     */
    public GridPane getLayout() {
        return mainGrid;
    }

    /**
     * Updates the UI based on client updates.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        // Update the drawn tiles grid with fresh data
        Platform.runLater(() -> {
            GridPane newTopGrid = createDrawnTilesGrid(3, 8);
            mainGrid.getChildren().remove(topGrid);
            mainGrid.add(newTopGrid, 0, 0, 2, 1);
            GridPane.setHgrow(newTopGrid, Priority.ALWAYS);
            GridPane.setVgrow(newTopGrid, Priority.ALWAYS);

            ((ShipGrid) leftGrid).updateGridFromShipBoard();
        });

    }
}
