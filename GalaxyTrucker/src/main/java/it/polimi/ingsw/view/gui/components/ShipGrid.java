package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.HashMap;
import java.util.Map;

/**
 * Grid component for placing ship tiles during game assembly.
 */
public class ShipGrid extends GridPane {
    private static final String DEFAULT_CELL_STYLE = "-fx-background-color: lightgray; -fx-border-color: darkgray;";
    private static final String HIGHLIGHT_CELL_STYLE = "-fx-background-color: lightblue; -fx-border-color: blue;";
    private static final double CELL_SIZE = 100;
    private static final double TILE_VIEW_SIZE = 80;
    private int baseRow, baseCol;

    Map<Coordinates, StackPane> grid = new HashMap<>();

    /**
     * Creates a new ship grid with specified dimensions.
     * 
     * @param rows Number of rows in the grid
     * @param cols Number of columns in the grid
     */
    public ShipGrid(int rows, int cols) {
        super();
        initializeGrid(rows, cols);
    }

    /**
     * Initializes the grid by creating and adding cells.
     */
    private void initializeGrid(int rows, int cols) {

        baseCol = switch (LobbyState.getGameLevel()) {
            case TESTFLIGHT, ONE -> {
                baseRow = 5;
                yield 4;
            }
            case TWO -> {
                baseRow = 4;
                yield 3;
            }
        };

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cell = createCell();
                this.add(cell, c, r);
                grid.put(new Coordinates(r + baseRow, c + baseCol),cell);
            }
        }
    }

    public void updateGridFromShipBoard(){
        ShipBoard shipBoard = AssembleState.getShipBoard();

        Map<Coordinates, TileSkeleton> map = shipBoard.getTilesOnBoard();

        for (Coordinates coordinates : map.keySet()) {
            if (grid.containsKey(coordinates)) {
                grid.get(coordinates).getChildren().add(new DraggableTile(map.get(coordinates)));
            }
            else {
                grid.get(coordinates).getChildren().clear();
            }

        }
    }

    public void wasPlacedLastTime(boolean lastTimeWasPlaced) {
        if (!lastTimeWasPlaced) {
            Platform.runLater(()->{
                ClientManager.getInstance().simulateCommand("discard");
            });
        }
    }

    /**
     * Creates a single cell with drag-and-drop functionality.
     */
    private StackPane createCell() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setStyle(DEFAULT_CELL_STYLE);

        configureDragHandlers(cell);
        
        return cell;
    }
    
    /**
     * Configures all drag-and-drop event handlers for a cell.
     */
    private void configureDragHandlers(StackPane cell) {
        cell.setOnDragOver(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        cell.setOnDragEntered(event -> {
            // Highlight the cell when a tile enters
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                cell.setStyle(HIGHLIGHT_CELL_STYLE);
            }
            event.consume();
        });

        cell.setOnDragExited(event -> {
            cell.setStyle(DEFAULT_CELL_STYLE);
            event.consume();
        });

        cell.setOnDragDropped((event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && db.getString().equals("tile") && cell.getChildren().isEmpty()) {
                DraggableTile draggedTile = AssembleUI.isBeeingDragged;
                AssembleUI.setIsBeeingDragged(null);

                if (draggedTile.getPosition() != WhichPane.FLOATING) {
                    StackPane originalParent = (StackPane) draggedTile.getParent();
                    if (originalParent != null) {
                        originalParent.getChildren().remove(draggedTile);
                    }
                }

                cell.getChildren().add(draggedTile);

                draggedTile.setPosition(WhichPane.SHIPGRID);
                Integer colIndex = GridPane.getColumnIndex(cell);
                Integer rowIndex = GridPane.getRowIndex(cell);
                int col = (colIndex == null) ? 0 : colIndex;
                int row = (rowIndex == null) ? 0 : rowIndex;

                Platform.runLater(() -> {
                    int before = ClientManager.getInstance().getLastUpdate().getClientPlayer().getShipBoard().getTiles().size();
                    ClientManager.getInstance().simulateCommand("place",  Integer.toString(row+baseRow), Integer.toString(col+baseCol));
                    int after = ClientManager.getInstance().getLastUpdate().getClientPlayer().getShipBoard().getTiles().size();

                    wasPlacedLastTime(before != after);
                });

                success = true;

                System.out.println("Tile dropped successfully");
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }
}
