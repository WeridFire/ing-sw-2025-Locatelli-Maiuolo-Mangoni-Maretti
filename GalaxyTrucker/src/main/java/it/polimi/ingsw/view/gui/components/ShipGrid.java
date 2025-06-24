package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import javafx.geometry.Pos;
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

    public static final double TILE_SIZE = CELL_SIZE * 0.95;
    public static final double TILE_BORDER_PERCENTAGE = 0.05;

    public static final double PIXEL_REAL_TO_SCALED = CELL_SIZE / 120.0;  // do NOT modify this!

    private final Map<Coordinates, ShipCell> gridCells = new HashMap<>();
    private final GridPane cellGridPane;
    private final int offsetX;
    private final int offsetY;
    private final int gapX;
    private final int gapY;

    private final ShipCell[] reserveSlots;
    private final GridPane reserveSlotsPane;
    private final int reserveOffsetX;
    private final int reserveOffsetY;

    /**
     * Creates a new ship grid display.
     * The grid consists of a background image, a cell grid for tiles and two slots for reserved tiles.
     * All the information are retrieved thanks to the played game level.
     *
     * @param level The game level to create shipboard grid for.
     */
    public ShipGrid(GameLevel level) {
        int rows, cols;
        switch (level) {
            case TESTFLIGHT, ONE -> {
                rows = 5;
                cols = 7;
                offsetX = 37;
                offsetY = 31;
                gapX = 4;
                gapY = 4;
                reserveOffsetX = 673;
                reserveOffsetY = 22;
            }
            case TWO -> {
                rows = 5;
                cols = 7;
                offsetX = 37;
                offsetY = 31;
                gapX = 4;
                gapY = 4;
                reserveOffsetX = 672;
                reserveOffsetY = 22;
            }
            default -> {
                rows = cols
                        = offsetX = offsetY
                        = gapX = gapY
                        = reserveOffsetX = reserveOffsetY
                        = 0;
            }
        }

        // 1. Setup Background
        ImageView backgroundView = new ImageView();
        backgroundView.setImage(AssetHandler.loadRawImage(Asset.SHIP.toString()));
        backgroundView.setPreserveRatio(true);
        // Set StackPane's preferred size to the background image's native size.
        double imgWidth = calculateWidth(cols);
        double imgHeight = calculateHeight(rows);
        this.setPrefSize(imgWidth, imgHeight);
        // Configure ImageView to fit these dimensions.
        backgroundView.setFitWidth(imgWidth);
        backgroundView.setFitHeight(imgHeight);

        // 2. Setup Cell Grid
        cellGridPane = new GridPane();
        initializeCellGridPane(rows, cols, level);

        // 3. create slots for reserved tiles
        reserveSlots = new ShipCell[2];
        reserveSlotsPane = new GridPane();
        initializeReserveSlots();

        // Add children to StackPane: background first, then cell grid
        this.getChildren().addAll(backgroundView, cellGridPane, reserveSlotsPane);

        update(); // Populate cells with initial data
    }

    private double calculateWidth(int cols) {
        return CELL_SIZE * cols + (cols * gapX + offsetX * 2) * PIXEL_REAL_TO_SCALED;
    }
    private double calculateHeight(int rows) {
        return CELL_SIZE * rows + (rows * gapY + offsetY * 2) * PIXEL_REAL_TO_SCALED;
    }

    /**
     * Initializes the cellGridPane properties, including cell creation and base coordinate calculation.
     * @param rows The number of rows for the cell grid.
     * @param cols The number of columns for the cell grid.
     */
    private void initializeCellGridPane(int rows, int cols, GameLevel level) {
        // determine baseRow and baseCol based on GameLevel
        // these are offsets to align visual grid coordinates with the logical ShipBoard coordinates
        int baseRow = 0, baseCol = 0;
        switch (level) {
            case TESTFLIGHT, ONE, TWO -> {
                baseRow = 5;
                baseCol = 4;
            }
        }

        // create all the cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Coordinates logicalCoords = new Coordinates(r + baseRow, c + baseCol);
                ShipCell cell = new ShipCell(logicalCoords, level);
                gridCells.put(logicalCoords, cell);
                cellGridPane.add(cell, c, r);
            }
        }

        // align cellGridPane to top-left for predictable translation offsets
        StackPane.setAlignment(cellGridPane, Pos.TOP_LEFT);
        cellGridPane.setTranslateX(offsetX * PIXEL_REAL_TO_SCALED);
        cellGridPane.setTranslateY(offsetY * PIXEL_REAL_TO_SCALED);
        cellGridPane.setHgap(gapX * PIXEL_REAL_TO_SCALED);
        cellGridPane.setVgap(gapY * PIXEL_REAL_TO_SCALED);
    }

    private void initializeReserveSlots() {
        StackPane.setAlignment(reserveSlotsPane, Pos.TOP_LEFT);
        reserveSlotsPane.setTranslateX(reserveOffsetX * PIXEL_REAL_TO_SCALED);
        reserveSlotsPane.setTranslateY(reserveOffsetY * PIXEL_REAL_TO_SCALED);
        reserveSlotsPane.setHgap(gapX * PIXEL_REAL_TO_SCALED);
        for (int i = 0; i < reserveSlots.length; i++) {
            reserveSlots[i] = ShipCell.reserveSlot();
            reserveSlotsPane.add(reserveSlots[i], i, 0);
        }
    }

    /**
     * Updates the visual representation of the grid based on the current state of the {@link ShipBoard}.
     * It clears existing tiles and re-populates cells with tiles from the spectated ShipBoard.
     * Cell styles are also updated to reflect valid placement areas.
     */
    public void update() {
        ShipBoard shipBoard = AssembleState.getSpectatedShipBoard();
        if (shipBoard == null) return;  // guard against null shipboard

        // tiles in ship
        Map<Coordinates, TileSkeleton> tilesOnBoard = shipBoard.getTilesOnBoard();
        for (Map.Entry<Coordinates, ShipCell> entry : gridCells.entrySet()) {
            Coordinates logicalCoords = entry.getKey();
            ShipCell cell = entry.getValue();
            cell.setTile(tilesOnBoard.get(logicalCoords));
            updateNeighbors(logicalCoords, cell);
        }

        // reserved tiles
        TileSkeleton[] reservedTiles = AssembleState.getSpectatedReservedTiles();
        for (int i = 0; i < reserveSlots.length; i++) {
            reserveSlots[i].setTile((i < reservedTiles.length) ? reservedTiles[i] : null);
        }
    }

    private void updateNeighbors(Coordinates coordinates, ShipCell cell) {
        cell.setHasNeighbor(coordinates.getNeighbors().stream().map(gridCells::get).toList());
    }
}
