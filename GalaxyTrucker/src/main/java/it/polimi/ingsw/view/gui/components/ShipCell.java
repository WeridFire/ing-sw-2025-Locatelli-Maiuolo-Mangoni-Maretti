package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.BoardCoordinates;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.gui.helpers.DragDropManager;
import it.polimi.ingsw.view.gui.helpers.DropSlot;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single cell in the ship grid. It can hold a ship tile or be empty.
 * It acts as a drop target for draggable ship tiles and loadable objects.
 */
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

    private List<LoadableObject> loadablesContent = new ArrayList<LoadableObject>();
    private HBox loadableBox;

    /**
     * Private constructor for creating a special reserve slot.
     */
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

    /**
     * Constructs a ship cell for a specific position on the board.
     * @param coordinates The logical coordinates of the cell.
     * @param level The game level, used to determine if the cell is on the actual board.
     */
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

    /**
     * Factory method to create a reserve slot cell.
     * @return A new ShipCell configured as a reserve slot.
     */
    public static ShipCell reserveSlot() {
        return new ShipCell();
    }

    /**
     * Places a tile on the cell or clears it.
     * @param tileOnCell The tile to place, or null to clear the cell.
     */
    public void setTile(TileSkeleton tileOnCell) {
        getChildren().clear();
        loadableBox = null;
        occupied = tileOnCell != null;
        if (occupied) {
            getChildren().add(new ShipTile(tileOnCell));
        }
    }

    /**
     * Updates the cell's state based on whether it has occupied neighbors.
     * @param neighbors A collection of neighboring cells.
     */
    public void setHasNeighbor(Collection<ShipCell> neighbors) {
        if (isReserveSlot) return;
        for (ShipCell neighbor : neighbors) {
            if (neighbor != null && neighbor.occupied) {
                hasNeighbor = true;
                break;
            }
        }
    }

    /**
     * Checks if a tile can be placed on this cell.
     * @return True if a tile can be accepted, false otherwise.
     */
    private boolean canAcceptTile() {
        return CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE) && (!occupied && ((isReserveSlot) || (isOnBoard && hasNeighbor)));
    }

    /**
     * Sets a custom background color for the cell, overriding other styles.
     * The highlight remains until cleared by calling this method with null or a blank string.
     *
     * @param color A valid JavaFX color string (e.g., "rgba(255,0,0,0.5)"), or null/blank to clear the highlight.
     */
    public void setHighlight(String color) {
        if (color != null && !color.isBlank()) {
            Rectangle highlightOverlay = new Rectangle(ShipGrid.CELL_SIZE, ShipGrid.CELL_SIZE, Color.web(color));
            highlightOverlay.setMouseTransparent(true); // Ignora i click
            this.getChildren().add(highlightOverlay); // Aggiungi in cima alla lista
            this.isHighlighted = true;
            System.out.println("Highlighted cell");
        } else {
            this.isHighlighted = false;
            setStyle(DEFAULT_CELL_STYLE);
        }
    }

    /**
     * Sets whether this cell is an active drop target during the adventure phase.
     * @param activeForAdventureDrop True to activate, false to deactivate.
     */
    public void setActiveForAdventureDrop(boolean activeForAdventureDrop) {
        this.isActiveForAdventureDrop = activeForAdventureDrop;
    }

    /**
     * Sets whether loadables can be removed from this cell during the adventure phase.
     * @param activeForAdventureRemove True to activate, false to deactivate.
     */
    public void setActiveForAdventureRemove(boolean activeForAdventureRemove) {
        this.isActiveForAdventureRemove = activeForAdventureRemove;
    }

    /**
     * Checks if the cell is active for dropping loadables during the adventure phase.
     * @return True if the cell is active and highlighted, false otherwise.
     */
    public boolean isActiveForAdventureDrop(){
        return isActiveForAdventureDrop && isHighlighted;
    }

    /**
     * Checks if the cell is active for removing loadables during the adventure phase.
     * @return True if the cell is active and highlighted, false otherwise.
     */
    public boolean isActiveForAdventureRemove() {
        return isActiveForAdventureRemove && isHighlighted;
    }

    /**
     * Adds a loadable object to this cell.
     * @param loadableObject The loadable object to add.
     * @return True if the object was added, false if the cell is full.
     */
    public boolean addLoadable(LoadableObject loadableObject) {
        if (loadablesContent.size() >= 4) {
            return false;
        }

        if (loadableBox == null) {
            loadableBox = new HBox();
            loadableBox.setStyle("-fx-background-color: transparent; -fx-border-color: black;");
            loadableBox.setAlignment(Pos.CENTER);
            getChildren().add(loadableBox);
        }

        loadablesContent.add(loadableObject);
        loadableBox.getChildren().add(loadableObject);

        return true;
    }

    /**
     * Removes a loadable object from this cell.
     * @param loadableObject The loadable object to remove.
     * @return True if the object was successfully removed, false otherwise.
     */
    public boolean removeLoadable(LoadableObject loadableObject) {
        boolean removed = loadablesContent.remove(loadableObject);
        if (removed && loadableBox != null) {
            loadableBox.getChildren().clear();
            loadableBox.getChildren().remove(loadableObject);
        }
        return removed;
    }

    public void setClickAbility() {
        if (isHighlighted && !isActiveForAdventureDrop && !isActiveForAdventureRemove) {
            // if just highlighted it means we are on a activatetile situation
            this.setOnMouseClicked(mouseEvent -> {
                ClientManager.getInstance().simulateCommand("activate", logicalRow, logicalColumn);
            });
        }
    }

    /**
     * Determines if the cell can accept a dragged item.
     * @param dragId The identifier of the dragged item.
     * @return True if the item can be dropped here, false otherwise.
     */
    @Override
    protected boolean canAccept(String dragId) {
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            return dragId.startsWith(ShipTile.BASE_ID) && canAcceptTile();
        }
        else if (isActiveForAdventureDrop()) {
            return dragId.startsWith(LoadableObject.BASE_ID);
        }
        return false;
    }

    /**
     * Handles the drop of an item onto the cell.
     * @param dragId The identifier of the dropped item.
     */
    @Override
    protected void acceptDrop(String dragId) {
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            if (isReserveSlot) {
                ClientManager.getInstance().simulateCommand("reserve");
            } else {
                ClientManager.getInstance().simulateCommand("place", logicalRow, logicalColumn);
            }
            occupied = true;
        } else if (isActiveForAdventureDrop()) {
            if (DragDropManager.getCurrentDraggable().getType() != null){
                ClientManager.getInstance().simulateCommand("allocate",logicalRow, logicalColumn,
                        DragDropManager.getCurrentDraggable().getType().toString(), "1");
                addLoadable((LoadableObject) DragDropManager.getCurrentDraggable());
            }
        }

    }

    /**
     * Handles mouse hover events to provide visual feedback.
     * @param entering True if the mouse is entering the cell, false if it is leaving.
     */
    @Override
    protected void onHover(boolean entering) {
        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            if (isHighlighted) {
                return;
            }
            setStyle((entering && canAcceptTile()) ? HIGHLIGHT_CELL_STYLE : DEFAULT_CELL_STYLE);
        }
    }
}
