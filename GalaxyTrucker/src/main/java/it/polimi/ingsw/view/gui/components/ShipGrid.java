package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.BoardCoordinates;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the grid where players assemble their spaceship by placing tiles.
 * It handles drag-and-drop operations for tiles and updates its display
 * based on the current state of the player's {@link ShipBoard}.
 */
public class ShipGrid extends GridPane {
    private static final String DEFAULT_CELL_STYLE = "-fx-background-color: lightgray; -fx-border-color: darkgray;";
    private static final String SHIP_CELL_STYLE = "-fx-background-color: lightblue; -fx-border-color: blue;";
    private static final String HIGHLIGHT_CELL_STYLE = "-fx-background-color: #ffffe0; -fx-border-color: #ffeb3b"; // Light yellow for highlight

    private static final double CELL_SIZE = 100;
    private int baseRow, baseCol; // Base offsets for mapping grid coordinates to ShipBoard coordinates
    private final Map<Coordinates, StackPane> gridCells = new HashMap<>();
    private final Map<StackPane, Coordinates> cellCoordinates = new HashMap<>();

    /**
     * Creates a new ship grid with specified dimensions, initializing cells
     * and setting up base coordinates based on the current game level.
     *
     * @param rows Number of rows in the visual grid.
     * @param cols Number of columns in the visual grid.
     */
    public ShipGrid(int rows, int cols) {
        super();
        initializeGridProperties(rows, cols);
        updateGridFromShipBoard();
    }

    /**
     * Initializes the grid properties, including cell creation and base coordinate calculation.
     * @param rows The number of rows for the grid.
     * @param cols The number of columns for the grid.
     */
    private void initializeGridProperties(int rows, int cols) {
        // Determine baseRow and baseCol based on GameLevel
        // These are offsets to align visual grid coordinates with the logical ShipBoard coordinates
        switch (LobbyState.getGameLevel()) {
            case TESTFLIGHT, ONE:
                baseRow = 5;
                baseCol = 4;
                break;
            case TWO:
                baseRow = 4;
                baseCol = 3;
                break;
            default: // Should not happen
                baseRow = 0;
                baseCol = 0;
                break;
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cell = createCell();
                this.add(cell, c, r); // Add to GridPane
                Coordinates logicalCoords = new Coordinates(r + baseRow, c + baseCol);
                gridCells.put(logicalCoords, cell);
                cellCoordinates.put(cell, logicalCoords);
            }
        }
    }

    /**
     * Updates the visual representation of the grid based on the current state of the {@link ShipBoard}.
     * It clears existing tiles and re-populates cells with tiles from the ShipBoard.
     * Cell styles are also updated to reflect valid placement areas.
     */
    public void updateGridFromShipBoard() {
        ShipBoard shipBoard = ClientManager.getInstance().getLastUpdate().getClientPlayer().getShipBoard();
        if (shipBoard == null) return; // Guard against null shipBoard

        Map<Coordinates, TileSkeleton> tilesOnBoard = shipBoard.getTilesOnBoard();

        for (Map.Entry<Coordinates, StackPane> entry : gridCells.entrySet()) {
            Coordinates logicalCoords = entry.getKey();
            StackPane cellPane = entry.getValue();

            cellPane.getChildren().clear(); // Clear previous tile, if any

            if (tilesOnBoard.containsKey(logicalCoords)) {
                TileSkeleton tileData = tilesOnBoard.get(logicalCoords);
                if (tileData != null) {
                    DraggableTile tileView = new DraggableTile(tileData);
                    tileView.setPosition(WhichPane.SHIPGRID); // Mark as placed on ship
                    cellPane.getChildren().add(tileView);
                }
            }

            // Set cell style
            if (BoardCoordinates.isOnBoard(LobbyState.getGameLevel(), logicalCoords)) {
                cellPane.setStyle(SHIP_CELL_STYLE);
            } else {
                cellPane.setStyle(DEFAULT_CELL_STYLE);
            }
        }
    }

    /**
     * If a tile placement attempt was not successful on the server-side
     * (e.g., invalid placement), this method reverts the client-side changes:
     * it discards the tile and clears it from the cell it was dropped onto.
     *
     * @param placementSuccessful A boolean indicating if the tile placement was successful.
     * @param visualRow The visual row index of the cell where the tile was dropped.
     * @param visualCol The visual column index of the cell where the tile was dropped.
     */
    public void revertCellIfPlaceFailed(boolean placementSuccessful, int visualRow, int visualCol) {
        if (!placementSuccessful) {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("discard"); // Tell server to discard the tile in hand
            });
            if (AssembleUI.isBeeingDragged != null) {
                AssembleUI.isBeeingDragged.setPosition(WhichPane.DRAWN); // Reset tile's logical position
            }
            // Clear the tile from the visual cell
            Coordinates logicalCoords = new Coordinates(visualRow + baseRow, visualCol + baseCol);
            StackPane cellToClear = gridCells.get(logicalCoords);
            if (cellToClear != null) {
                cellToClear.getChildren().clear();
            }
        }
    }

    /**
     * Creates a single cell ({@link StackPane}) for the grid,
     * configuring its size, style, and drag-and-drop event handlers.
     * @return The created {@link StackPane} cell.
     */
    private StackPane createCell() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setStyle(DEFAULT_CELL_STYLE);
        configureDragHandlers(cell);
        return cell;
    }

    /**
     * Configures drag-and-drop event handlers for a given grid cell.
     * Handles drag over, entered, exited, and dropped events.
     * @param cell The {@link StackPane} cell to configure.
     */
    private void configureDragHandlers(StackPane cell) {
        cell.setOnDragOver(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        cell.setOnDragEntered(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                cell.setStyle(HIGHLIGHT_CELL_STYLE); // Highlight cell on drag enter
            }
            event.consume();
        });

        cell.setOnDragExited(event -> {
            Coordinates logicalCoords = cellCoordinates.get(cell);
            if (logicalCoords != null && BoardCoordinates.isOnBoard(LobbyState.getGameLevel(), logicalCoords)) {
                cell.setStyle(SHIP_CELL_STYLE);
            } else {
                cell.setStyle(DEFAULT_CELL_STYLE);
            }
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && Objects.equals(db.getString(), "tile") && cell.getChildren().isEmpty()) {
                DraggableTile draggedTile = AssembleUI.isBeeingDragged;
                if (draggedTile != null) {
                    cell.getChildren().add(draggedTile);
                    draggedTile.setPosition(WhichPane.SHIPGRID);

                    Integer colIndex = GridPane.getColumnIndex(cell);
                    Integer rowIndex = GridPane.getRowIndex(cell);
                    int visualCol = (colIndex == null) ? 0 : colIndex; // Visual column in the GridPane
                    int visualRow = (rowIndex == null) ? 0 : rowIndex; // Visual row in the GridPane

                    Platform.runLater(() -> {
                        ShipBoard currentBoard = ClientManager.getInstance().getLastUpdate().getClientPlayer().getShipBoard();
                        int tilesBeforePlace = (currentBoard != null && currentBoard.getTiles() != null) ? currentBoard.getTiles().size() : 0;

                        ClientManager.getInstance().simulateCommand("place", Integer.toString(visualRow + baseRow), Integer.toString(visualCol + baseCol));

                        currentBoard = ClientManager.getInstance().getLastUpdate().getClientPlayer().getShipBoard();
                        int tileAfterPlace = currentBoard != null ? currentBoard.getTiles().size() : 0;

                        if (tileAfterPlace == tilesBeforePlace) {
                            ClientManager.getInstance().simulateCommand("discard");
                        }
                    });
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Checks if the given cell is one of the reserved cells, which might have special rules
     * depending on the {@link GameLevel}.
     *
     * @param cell The {@link StackPane} cell to check.
     * @return True if the cell is a reserved cell, false otherwise.
     */
    public boolean isReserveCell(StackPane cell) {
        Coordinates coords = cellCoordinates.get(cell);
        if (coords == null) return false;

        GameLevel level = LobbyState.getGameLevel();
        if (Objects.requireNonNull(level) == GameLevel.TWO) {
            // Reserved cells for GameLevel TWO
            return (coords.equals(new Coordinates(4, 10)) || coords.equals(new Coordinates(4, 11)));
        } else {
            // Reserved cells for GameLevel TESTFLIGHT or ONE
            return (coords.equals(new Coordinates(5, 9)) || coords.equals(new Coordinates(5, 10)));
        }
    }
}
