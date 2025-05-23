package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the grid where players assemble their spaceship by placing tiles.
 * It handles drag-and-drop operations for tiles and updates its display
 * based on the current state of the player's {@link ShipBoard}.
 */
public class ShipGrid extends GridPane {
    public static final double CELL_SIZE = 100;
    public static final double TILE_SIZE = CELL_SIZE * 0.9;
    public static final double TILE_BORDER_SIZE = TILE_SIZE * 0.025;

    private final Map<Coordinates, ShipCell> grid = new HashMap<>();

    /**
     * Creates a new ship grid with specified dimensions, initializing cells
     * and setting up base coordinates based on the current game level.
     *
     * @param rows Number of rows in the visual grid.
     * @param cols Number of columns in the visual grid.
     */
    public ShipGrid(int rows, int cols) {
        super();
        initialize(rows, cols);
        update();
    }

    /**
     * Initializes the grid properties, including cell creation and base coordinate calculation.
     * @param rows The number of rows for the grid.
     * @param cols The number of columns for the grid.
     */
    private void initialize(int rows, int cols) {
        // determine baseRow and baseCol based on GameLevel
        GameLevel gameLevel = LobbyState.getGameLevel();

        // these are offsets to align visual grid coordinates with the logical ShipBoard coordinates
        int baseRow = 0, baseCol = 0;
        switch (gameLevel) {
            case TESTFLIGHT, ONE:
                baseRow = 5;
                baseCol = 4;
                break;
            case TWO:
                baseRow = 4;
                baseCol = 3;
                break;
            default: break;  // should never happen
        }

        // create all the cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Coordinates logicalCoords = new Coordinates(r + baseRow, c + baseCol);
                ShipCell cell = new ShipCell(logicalCoords, gameLevel);
                grid.put(logicalCoords, cell);
                this.add(cell, c, r);
            }
        }

        // set fixed size for the ShipGrid itself
        double calculatedWidth = cols * CELL_SIZE;
        double calculatedHeight = rows * CELL_SIZE;
        this.setPrefSize(calculatedWidth, calculatedHeight);
        this.setMinSize(calculatedWidth, calculatedHeight);
        this.setMaxSize(calculatedWidth, calculatedHeight);
    }

    /**
     * Updates the visual representation of the grid based on the current state of the {@link ShipBoard}.
     * It clears existing tiles and re-populates cells with tiles from the ShipBoard.
     * Cell styles are also updated to reflect valid placement areas.
     */
    public void update() {
        ShipBoard shipBoard = ClientManager.getInstance().getLastUpdate().getClientPlayer().getShipBoard();
        if (shipBoard == null) return;  // guard against null shipboard

        Map<Coordinates, TileSkeleton> tilesOnBoard = shipBoard.getTilesOnBoard();

        for (Map.Entry<Coordinates, ShipCell> entry : grid.entrySet()) {
            Coordinates logicalCoords = entry.getKey();
            ShipCell cell = entry.getValue();
            cell.setTile(tilesOnBoard.get(logicalCoords));
            updateNeighbors(logicalCoords, cell);
        }
    }

    private void updateNeighbors(Coordinates coordinates, ShipCell cell) {
        cell.setHasNeighbor(coordinates.getNeighbors().stream().map(grid::get).toList());
    }
}
