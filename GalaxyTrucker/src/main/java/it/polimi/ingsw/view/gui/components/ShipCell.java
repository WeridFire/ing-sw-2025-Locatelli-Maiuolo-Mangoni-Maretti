package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.BoardCoordinates;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.helpers.DropSlot;
import it.polimi.ingsw.view.gui.managers.ClientManager;

import java.util.Collection;

public class ShipCell extends DropSlot {
    private static final String SHIP_CELL_STYLE = "-fx-background-color: lightblue; -fx-border-color: blue;";
    private static final String SPACE_CELL_STYLE = "-fx-background-color: lightgray; -fx-border-color: darkgray;";
    private static final String HIGHLIGHT_CELL_STYLE = "-fx-background-color: #ffffe0; -fx-border-color: #ffeb3b"; // Light yellow for highlight

    private final String DEFAULT_CELL_STYLE;

    private final String logicalRow, logicalColumn;
    private final boolean isOnBoard;
    private boolean occupied;
    private boolean hasNeighbor;

    public ShipCell(Coordinates coordinates, GameLevel level) {
        logicalRow = String.valueOf(coordinates.getRow());
        logicalColumn = String.valueOf(coordinates.getColumn());

        isOnBoard = BoardCoordinates.isOnBoard(level, coordinates);
        DEFAULT_CELL_STYLE = isOnBoard ? SHIP_CELL_STYLE : SPACE_CELL_STYLE;
        setStyle(DEFAULT_CELL_STYLE);

        setPrefSize(ShipGrid.CELL_SIZE, ShipGrid.CELL_SIZE);

        occupied = false;
        hasNeighbor = false;
    }

    public void setTile(TileSkeleton tileOnCell) {
        getChildren().clear();
        occupied = tileOnCell != null;
        if (occupied) {
            getChildren().add(new ShipTile(tileOnCell));
        }
    }

    public void setHasNeighbor(Collection<ShipCell> neighbors) {
        for (ShipCell neighbor : neighbors) {
            if (neighbor != null && neighbor.occupied) {
                hasNeighbor = true;
                break;
            }
        }
    }

    private boolean canAcceptTile() {
        return !occupied && isOnBoard && hasNeighbor;
    }

    @Override
    protected boolean canAccept(String dragId) {
        return dragId.startsWith(ShipTile.BASE_ID) && canAcceptTile();
    }

    @Override
    protected void acceptDrop(String dragId) {
        ClientManager.getInstance().simulateCommand("place", logicalRow, logicalColumn);
        occupied = true;
    }

    @Override
    protected void onHover(boolean entering) {
        setStyle((entering && canAcceptTile()) ? HIGHLIGHT_CELL_STYLE : DEFAULT_CELL_STYLE);
    }
}
