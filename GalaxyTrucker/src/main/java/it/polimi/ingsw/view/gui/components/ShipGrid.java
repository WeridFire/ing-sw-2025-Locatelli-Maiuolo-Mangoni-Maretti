package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the display area for a player's spaceship, consisting of a background image
 * and a movable grid of cells for placing tiles.
 * It updates its display based on the current state of the player's {@link ShipBoard}.
 */
public class ShipGrid extends StackPane {
    public static final double CELL_SIZE = 80;
    public static final double TILE_SIZE = CELL_SIZE * 0.9;
    public static final double TILE_BORDER_SIZE = TILE_SIZE * 0.025;

    private final Map<Coordinates, ShipCell> gridCells = new HashMap<>();
    private final ImageView backgroundView;
    private final GridPane cellGridPane;

    /**
     * Creates a new ship grid display.
     * The grid consists of a background image and a cell grid for tiles.
     * The cell grid's dimensions are specified by rows and columns.
     *
     * @param rows Number of rows in the cell grid.
     * @param cols Number of columns in the cell grid.
     */
    public ShipGrid(int rows, int cols) {
        super();

        // 1. Setup Background
        Image bgImage = AssetHandler.loadRawImage(Asset.SHIP.toString());
        this.backgroundView = new ImageView();

        if (bgImage != null) {
            this.backgroundView.setImage(bgImage);
            this.backgroundView.setPreserveRatio(true);

            // Set StackPane's preferred size to the background image's native size.
            double imgWidth = CELL_SIZE * cols + 60;
            double imgHeight =  CELL_SIZE * rows + 60;
            this.setPrefSize(imgWidth, imgHeight);

            // Configure ImageView to fit these dimensions.
            this.backgroundView.setFitWidth(imgWidth);
            this.backgroundView.setFitHeight(imgHeight);
        } else {
            // Fallback size if image loading fails
            this.setPrefSize(cols * CELL_SIZE + 200, rows * CELL_SIZE + 200); // Default padding
        }

        // 2. Setup Cell Grid
        this.cellGridPane = new GridPane();
        initializeCellGridPane(rows, cols);

        // Add children to StackPane: background first, then cell grid
        this.getChildren().addAll(this.backgroundView, this.cellGridPane);

        // Align cellGridPane to top-left for predictable translation offsets
        StackPane.setAlignment(this.cellGridPane, Pos.CENTER);

        update(); // Populate cells with initial data
    }

    /**
     * Sets the position of the cell grid relative to the top-left corner of the background.
     *
     * @param offsetX The horizontal offset.
     * @param offsetY The vertical offset.
     */
    public void setCellGridOffset(double offsetX, double offsetY) {
        this.cellGridPane.setTranslateX(offsetX);
        this.cellGridPane.setTranslateY(offsetY);
    }

    /**
     * Initializes the cellGridPane properties, including cell creation and base coordinate calculation.
     * @param rows The number of rows for the cell grid.
     * @param cols The number of columns for the cell grid.
     */
    private void initializeCellGridPane(int rows, int cols) {
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
                gridCells.put(logicalCoords, cell);
                this.cellGridPane.add(cell, c, r);
            }
        }

        // set fixed size for the cellGridPane itself
        double calculatedWidth = cols * CELL_SIZE;
        double calculatedHeight = rows * CELL_SIZE;
        this.cellGridPane.setPrefSize(calculatedWidth, calculatedHeight);
        this.cellGridPane.setMinSize(calculatedWidth, calculatedHeight);
        this.cellGridPane.setMaxSize(calculatedWidth, calculatedHeight);
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

        for (Map.Entry<Coordinates, ShipCell> entry : gridCells.entrySet()) {
            Coordinates logicalCoords = entry.getKey();
            ShipCell cell = entry.getValue();
            cell.setTile(tilesOnBoard.get(logicalCoords));
            updateNeighbors(logicalCoords, cell);
        }
    }

    private void updateNeighbors(Coordinates coordinates, ShipCell cell) {
        cell.setHasNeighbor(coordinates.getNeighbors().stream().map(gridCells::get).toList());
    }
}
