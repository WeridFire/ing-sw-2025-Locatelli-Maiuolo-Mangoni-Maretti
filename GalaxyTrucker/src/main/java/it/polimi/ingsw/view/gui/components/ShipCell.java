package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.BoardCoordinates;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.helpers.DropSlot;
import it.polimi.ingsw.view.gui.managers.ClientManager;

import java.util.Collection;

public class ShipCell extends DropSlot {
    private static final String SHIP_CELL_STYLE = "";//"-fx-background-color: rgba(173, 216, 230, 0.5); -fx-border-color: blue;"; // lightblue semi-trasparente
    private static final String SPACE_CELL_STYLE = "";//"-fx-background-color: rgba(211, 211, 211, 0.5); -fx-border-color: darkgray;"; // lightgray semi-trasparente
    private static final String HIGHLIGHT_CELL_STYLE = "-fx-background-color: rgba(255, 255, 224, 0.5); -fx-border-color: #ffeb3b;"; // light yellow semi-trasparente

    private final String DEFAULT_CELL_STYLE;

    private final String logicalRow, logicalColumn;
    private final boolean isOnBoard;
    private boolean occupied;
    private boolean hasNeighbor;
    private boolean isHighlighted = false;

    private boolean isActiveForAdventureDrop = false;
    private boolean isActiveForAdventureRemove = false;

    private final boolean isReserveSlot;

    private ShipCell() {
        isReserveSlot = true;
        DEFAULT_CELL_STYLE = "";
        logicalRow = logicalColumn = null;
        occupied = false;
        // simulate all validity condition
        isOnBoard = true;
        hasNeighbor = true;

        setPrefSize(ShipGrid.CELL_SIZE, ShipGrid.CELL_SIZE);
    }

    public ShipCell(Coordinates coordinates, GameLevel level) {
        isReserveSlot = false;
        logicalRow = String.valueOf(coordinates.getRow());
        logicalColumn = String.valueOf(coordinates.getColumn());

        isOnBoard = BoardCoordinates.isOnBoard(level, coordinates);
        DEFAULT_CELL_STYLE = isOnBoard ? SHIP_CELL_STYLE : SPACE_CELL_STYLE;
        setStyle(DEFAULT_CELL_STYLE);

        setPrefSize(ShipGrid.CELL_SIZE, ShipGrid.CELL_SIZE);

        occupied = false;
        hasNeighbor = false;
    }

    public static ShipCell reserveSlot() {
        return new ShipCell();
    }

    public void setTile(TileSkeleton tileOnCell) {
        getChildren().clear();
        occupied = tileOnCell != null;
        if (occupied) {
            getChildren().add(new ShipTile(tileOnCell));
        }
    }

    public void setHasNeighbor(Collection<ShipCell> neighbors) {
        if (isReserveSlot) return;
        for (ShipCell neighbor : neighbors) {
            if (neighbor != null && neighbor.occupied) {
                hasNeighbor = true;
                break;
            }
        }
    }

    private boolean canAcceptTile() {
        return CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE) && (!occupied && ((isReserveSlot) || (isOnBoard && hasNeighbor)));
    }

    @Override
    protected boolean canAccept(String dragId) {
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            return dragId.startsWith(ShipTile.BASE_ID) && canAcceptTile();
        }
        //TODO
        return false;
    }

    @Override
    protected void acceptDrop(String dragId) {
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            if (isReserveSlot) {
                ClientManager.getInstance().simulateCommand("reserve");
            } else {
                ClientManager.getInstance().simulateCommand("place", logicalRow, logicalColumn);
            }
            occupied = true;
        }

    }

    @Override
    protected void onHover(boolean entering) {
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            if (isHighlighted) {
                return;
            }
            setStyle((entering && canAcceptTile()) ? HIGHLIGHT_CELL_STYLE : DEFAULT_CELL_STYLE);
        }
    }

    /**
     * Sets a custom background color for the cell, overriding other styles.
     * The highlight remains until cleared by calling this method with null or a blank string.
     *
     * @param color A valid JavaFX color string (e.g., "rgba(255,0,0,0.5)"), or null/blank to clear the highlight.
     */
    public void setHighlight(String color) {
        if (color != null && !color.isBlank()) {
            this.isHighlighted = true;
            setStyle("-fx-background-color: " + color + ";");
        } else {
            this.isHighlighted = false;
            setStyle(DEFAULT_CELL_STYLE);
        }
    }

    public void setActiveForAdventureDrop(boolean activeForAdventureDrop) {
        this.isActiveForAdventureDrop = activeForAdventureDrop;
    }

    public void setActiveForAdventureRemove(boolean activeForAdventureRemove) {
        this.isActiveForAdventureRemove = activeForAdventureRemove;
    }
}
