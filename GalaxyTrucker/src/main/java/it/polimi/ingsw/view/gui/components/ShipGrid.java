package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.TileCluster;
import it.polimi.ingsw.model.shipboard.tiles.BatteryComponentTile;
import it.polimi.ingsw.model.shipboard.tiles.CabinTile;
import it.polimi.ingsw.model.shipboard.tiles.CargoHoldTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.UIs.AdventureUI;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.utils.ShipIntegrityProblemManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.*;

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

    private Set<ShipCell> activeCells = null;
    private List<ShipCell> cellsToActivate = new ArrayList<>();

    private ShipIntegrityProblemManager sipManager = null;

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
                ShipCell cell = new ShipCell(logicalCoords, level, this);
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

    public void handleIntegrityProblemChoice() {
        if (sipManager != null) return;  // it's already managing integrity problem

        ShipBoard shipBoard = CommonState.getPlayer().getShipBoard();
        List<TileCluster> clustersToKeep = shipBoard.getVisitorCheckIntegrity()
                .getProblem(!shipBoard.isFilled())
                .getClustersToKeep();
        int totClusters = clustersToKeep.size();
        sipManager = new ShipIntegrityProblemManager(this, totClusters);

        for (int i = 0; i < totClusters; i++) {
            TileCluster cluster = clustersToKeep.get(i);
            Set<Coordinates> coordinates = cluster.getCoordinates();
            sipManager.addCluster(coordinates, i);

            for (Coordinates coord : coordinates) {
                ShipCell cell = gridCells.get(coord);
                if (cell != null) {
                    sipManager.addShipCell(cell, i);
                }
            }
        }

        sipManager.start();

        // highlight integrity problem (if present)
        if (sipManager != null) {
            //note highlihgtall actually triggers the highlight
            if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE))
                AssembleUI.getInstance().addIntegrityButton();
            if(sipManager.highlightAll()){
                AdventureUI.getInstance().addIntegrityButton();
            }
        }
    }
    public void confirmIntegrityProblemChoice() {
        int choice = sipManager.getChoice();
        dropIntegrityProblemChoice();
        Platform.runLater(() -> ClientManager.getInstance()
                .simulateCommand("choose", String.valueOf(choice)));
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE))
            AssembleUI.getInstance().hideIntegrityButton();
        else
            AdventureUI.getInstance().removeIntegrityButton();
    }
    public void dropIntegrityProblemChoice() {
        if (sipManager == null) return;
        // else: actually need to drop integrity problem choice
        sipManager.unhighlightAll();
        sipManager = null;
        unsetActiveCells();
    }

    /**
     * Updates the visual representation of the grid based on the current state of the {@link ShipBoard}.
     * It clears existing tiles and re-populates cells with tiles from the spectated ShipBoard.
     * Cell styles are also updated to reflect valid placement areas.
     */
    public void update() {
        ShipBoard shipBoard = AssembleState.getSpectatedShipBoard();
        if (!AssembleState.isSpectatingOther()){
            shipBoard = CommonState.getPlayer().getShipBoard();
        }

        Map<Coordinates, CargoHoldTile> loadables = Map.of();
        Map<Coordinates, CabinTile> crew = Map.of();
        Map<Coordinates, BatteryComponentTile> batteries = Map.of();

        if (shipBoard.getVisitorCalculateCargoInfo() != null){
            loadables = shipBoard.getVisitorCalculateCargoInfo().getGoodsInfo().getLocations();
            crew = shipBoard.getVisitorCalculateCargoInfo().getCrewInfo().getLocations();
            batteries = shipBoard.getVisitorCalculateCargoInfo().getBatteriesInfo().getLocations();
        }

        // tiles in ship
        Map<Coordinates, TileSkeleton> tilesOnBoard = shipBoard.getTilesOnBoard();
        for (Map.Entry<Coordinates, ShipCell> entry : gridCells.entrySet()) {
            Coordinates logicalCoords = entry.getKey();
            ShipCell cell = entry.getValue();

            cell.setTile(tilesOnBoard.get(logicalCoords));
            updateNeighbors(logicalCoords, cell);

            if (loadables != null && loadables.get(entry.getKey()) != null) {
                List<LoadableType> goods = loadables.get(entry.getKey()).getLoadedItems();
                System.out.println(goods);
                goods.forEach(x ->
                        cell.addLoadable(new LoadableObject(AdventureUI.getDragOverlay(), x, cell))
                );
            }
            else if (crew != null && crew.get(entry.getKey()) != null) {
                List<LoadableType> goods = crew.get(entry.getKey()).getLoadedItems();
                System.out.println(goods);
                goods.forEach(x ->
                        cell.addLoadable(new LoadableObject(AdventureUI.getDragOverlay(), x, cell))
                );
            }
            else if (batteries != null && batteries.get(entry.getKey()) != null) {
                List<LoadableType> goods = batteries.get(entry.getKey()).getLoadedItems();
                System.out.println(goods);
                goods.forEach(x ->
                        cell.addLoadable(new LoadableObject(AdventureUI.getDragOverlay(), x, cell))
                );
            }
        }

        // reserved tiles
        TileSkeleton[] reservedTiles = AssembleState.getSpectatedReservedTiles();
        for (int i = 0; i < reserveSlots.length; i++) {
            reserveSlots[i].setTile((i < reservedTiles.length) ? reservedTiles[i] : null);
        }

    }

    private void decideCellsClickAbility() {
        for (ShipCell c: gridCells.values()) {
            c.setClickAbility();
        }
    }

    private void updateNeighbors(Coordinates coordinates, ShipCell cell) {
        cell.setHasNeighbor(coordinates.getNeighbors().stream().map(gridCells::get).toList());
    }

    /**
     * Highlights a set of cells on the grid with a specific color.
     * Before applying the new highlights, all previous highlights are cleared.
     * If the provided set of coordinates is null or empty, this method will only clear existing highlights.
     *
     * @param coordinatesToHighlight A {@link Set} of {@link Coordinates} to highlight.
     *                               If null or empty, no new highlights will be applied, but existing ones will be cleared.
     * @param color                  A JavaFX color string (e.g., "rgba(255, 0, 0, 0.5)" or "#FF000080").
     *                               The highlight will be applied only if the color is not null or blank.
     */
    public void highlightCells(Set<Coordinates> coordinatesToHighlight, String color) {
        // Check for corner cases: no coordinates to highlight or no valid color provided
        if (coordinatesToHighlight == null || coordinatesToHighlight.isEmpty() || color == null || color.isBlank()) {
            return;
        }

        // Apply the new highlight to the specified cells
        for (Coordinates coord : coordinatesToHighlight) {
            ShipCell cell = gridCells.get(coord);
            if (cell != null) {
                cell.setHighlight(color);
            }
        }
    }

    /**
     * Sets the active cells based on a given set of coordinates.
     * The provided coordinates are stored for later use, for example to apply specific logic or styles.
     *
     * @param coordinates The set of coordinates to mark as active. If null or empty, the active cells will be cleared.
     */
    public void setActiveCells(Set<Coordinates> coordinates, boolean drop, boolean remove) {
        if (coordinates == null || coordinates.isEmpty()) {
            this.activeCells = null;
        } else {
            this.activeCells = new HashSet<>();
            for (Coordinates coord : coordinates) {
                ShipCell cell = gridCells.get(coord);
                if (cell != null) {
                    cell.setActiveForAdventureDrop(drop);
                    cell.setActiveForAdventureRemove(remove);
                    this.activeCells.add(cell);
                }
            }

            highlightCells(coordinates, drop ? Default.ADD_CARGO_STRING_COLOR : Default.REMOVE_CARGO_STRING_COLOR);
            decideCellsClickAbility();
        }
    }

    /**
     * Clears the currently active cells.
     */
    public void unsetActiveCells() {
        for (ShipCell cell : gridCells.values()) {
            cell.setActiveForAdventureDrop(false);
            cell.setActiveForAdventureRemove(false);

            cell.setHighlight(null);
        }
        this.activeCells = null;
    }

    public List<ShipCell> getCellsToActivate() {
        return this.cellsToActivate;
    }


}
