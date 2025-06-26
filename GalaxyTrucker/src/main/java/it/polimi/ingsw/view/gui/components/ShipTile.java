package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.DragDropManager;
import it.polimi.ingsw.view.gui.helpers.Draggable;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a draggable ship tile component in the GUI.
 * It can be face up (showing the tile's structure) or face down.
 * It handles drag-and-drop operations, rotation, and interaction with the game logic.
 */
public class ShipTile extends Draggable {
    /**
     * Base string for the drag-and-drop identifier.
     */
    public static final String BASE_ID = "TILE";
    private static Image BACK_IMAGE;

    private ImageView imageView;

    private TileSkeleton tile;

    /**
     * Constructs a ShipTile with a given tile skeleton.
     * @param tile The {@link TileSkeleton} data for this tile. Can be null for a covered tile.
     */
    public ShipTile(TileSkeleton tile) {
        super(AssembleUI.getDragOverlay());
        initialize();
        setTile(tile);
    }

    /**
     * Constructs a covered (face down) ShipTile.
     */
    public ShipTile() {
        this(null);
    }

    /**
     * Sets the tile data for this component and updates its appearance.
     * If the tile is not null, it shows the tile's texture and applies its rotation.
     * @param tile The {@link TileSkeleton} to display.
     */
    private void setTile(TileSkeleton tile) {
        this.tile = tile;
        if (!isCovered()) {
            imageView.setImage(AssetHandler.loadRawImage(tile.getTextureName()));
            this.setRotate(tile.getAppliedRotation().toDegrees());
        }
    }

    /**
     * Gets the underlying tile data.
     * @return The {@link TileSkeleton} of this tile.
     */
    public TileSkeleton getTile() {
        return tile;
    }

    /**
     * Initializes the visual properties and drag behavior of the tile.
     * Sets up the image view, default size, border, and configures drag-and-drop handlers.
     */
    private void initialize() {
        // prepare back image for all if not prepared yet
        if (BACK_IMAGE == null) {
            BACK_IMAGE = AssetHandler.loadRawImage(Default.PATH_BACK_TILE);
        }

        // size preparation
        double imageSize = ShipGrid.TILE_SIZE * (1 - ShipGrid.TILE_BORDER_PERCENTAGE);
        double borderSize = ShipGrid.TILE_SIZE * ShipGrid.TILE_BORDER_PERCENTAGE * 0.5;

        // image view size and default content
        imageView = new ImageView(BACK_IMAGE);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(imageSize);
        imageView.setFitHeight(imageSize);

        // image view border
        Rectangle border = new Rectangle(imageSize + borderSize, imageSize + borderSize);
        border.setFill(Color.WHITE);  // to avoid small transparent gap issue between image and border
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(borderSize);

        // image view attached to this tile, over the border
        this.getChildren().addAll(border, imageView);
    }

    /**
     * Checks if the tile is covered (face down).
     * @return True if the tile data is null, false otherwise.
     */
    public boolean isCovered() {
        return tile == null;
    }

    /**
     * Checks if the tile has been placed on the ship board.
     * @return True if the tile is not covered and is marked as placed.
     */
    public boolean isPlaced() {
        return !isCovered() && tile.isPlaced();
    }

    /**
     * Checks if the tile is currently in the reserve slot.
     * @return True if the tile is reserved, false otherwise.
     */
    public boolean isReserved() {
        return AssembleState.isTileReserved(tile);
    }

    /**
     * Determines if the tile can be dragged based on the current game state.
     * @return True if the tile is draggable, false otherwise.
     */
    @Override
    protected boolean canBeDragged() {
        return super.canBeDragged() && !isPlaced()
                && !AssembleState.isEndedAssembly()
                && !AssembleState.isSpectatingOther();
    }

    /**
     * Handles the start of a drag operation.
     * It simulates a "draw" or "pick" command based on whether the tile was covered or not.
     */
    @Override
    protected void onBeginDrag() {
        if (isCovered()) {
            ClientManager.getInstance().simulateCommand("draw");
        } else {  // is uncovered
            ClientManager.getInstance().simulateCommand("pick", String.valueOf(tile.getTileId()));
        }
        setTile(AssembleState.getTileInHand());
    }

    /**
     * Handles key presses while the tile is being dragged to allow rotation.
     * @param code The {@link KeyCode} of the pressed key.
     */
    @Override
    protected void onKeyPressedWhileDragging(KeyCode code) {
        switch (code) {
            case A -> rotate(Rotation.COUNTERCLOCKWISE);
            case D -> rotate(Rotation.CLOCKWISE);
        }
    }

    /**
     * Handles the end of a drag operation.
     * Removes the tile from its parent container.
     * @param dropAccepted True if the drop was on a valid target, false otherwise.
     */
    @Override
    protected void onEndDrag(boolean dropAccepted) {
        Pane parent = (Pane) this.getParent();
        if (parent == null) return;
        parent.getChildren().remove(this);
    }

    /**
     * Generates a unique identifier for the drag operation.
     * @return The drag identifier string.
     */
    @Override
    protected String getDragId() {
        return BASE_ID + (tile == null ? "" : String.valueOf(tile.getTileId()));
    }

    /**
     * Handles the case where the tile is dropped on an invalid location.
     * It either reserves or discards the tile based on the game rules.
     */
    @Override
    protected void fallbackDropHandler() {
        if (isReserved()) {
            ClientManager.getInstance().simulateCommand("reserve");
            return;
        }
        // else: is valid to discard it
        ClientManager.getInstance().simulateCommand("discard");
    }

    /**
     * Rotates the tile by sending a command to the server and updating the visual representation.
     * @param rotation The {@link Rotation} to apply.
     */
    private void rotate(Rotation rotation) {
        ClientManager.getInstance().simulateCommand("rotate", rotation.toString());
        if (isDragging()) {
            this.setRotate(this.getRotate() + rotation.toDegrees());
            DragDropManager.updateSnapshot(getSnapshot());
        }
    }
}