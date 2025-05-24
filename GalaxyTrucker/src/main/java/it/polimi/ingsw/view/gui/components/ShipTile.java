package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
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

public class ShipTile extends Draggable {
    public static final String BASE_ID = "TILE";
    private static Image BACK_IMAGE;

    private ImageView imageView;

    private TileSkeleton tile;

    public ShipTile(TileSkeleton tile) {
        super(AssembleUI.getDragOverlay());
        initialize();
        setTile(tile);
    }
    public ShipTile() {
        this(null);
    }

    private void setTile(TileSkeleton tile) {
        this.tile = tile;
        if (!isCovered()) {
            imageView.setImage(AssetHandler.loadRawImage(tile.getTextureName()));
            this.setRotate(tile.getAppliedRotation().toDegrees());
        }
    }

    /**
     * Initializes the visual properties and drag behavior of the tile.
     * Sets up the image view, default size, border, and configures drag-and-drop handlers.
     */
    private void initialize() {
        // prepare back image for all if not prepared yet
        if (BACK_IMAGE == null) {
            BACK_IMAGE = AssetHandler.loadRawImage(Default.BACK_TILE_PATH);
        }

        // image view size and default content
        imageView = new ImageView(BACK_IMAGE);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(ShipGrid.TILE_SIZE);
        imageView.setFitHeight(ShipGrid.TILE_SIZE);
        // image view border
        this.setBorder(new Border(new BorderStroke(Color.WHITE,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(ShipGrid.TILE_BORDER_SIZE))));
        // image view attached to this tile
        this.getChildren().add(imageView);
    }

    public boolean isCovered() {
        return tile == null;
    }

    public boolean isUncovered() {
        return !isCovered() && !tile.isPlaced();
    }

    public boolean isPlaced() {
        return !isCovered() && tile.isPlaced();
    }

    @Override
    protected boolean canBeDragged() {
        return super.canBeDragged() && !isPlaced();
    }

    @Override
    protected void onBeginDrag() {
        if (isCovered()) {
            ClientManager.getInstance().simulateCommand("draw");
        } else {  // is uncovered
            ClientManager.getInstance().simulateCommand("pick", String.valueOf(tile.getTileId()));
        }
        setTile(AssembleState.getTileInHand());
    }

    @Override
    protected void onKeyPressedWhileDragging(KeyCode code) {
        switch (code) {
            case A -> rotate(Rotation.COUNTERCLOCKWISE);
            case D -> rotate(Rotation.CLOCKWISE);
        }
    }

    @Override
    protected void onEndDrag() {
        Pane parent = (Pane) this.getParent();
        if (parent == null) return;
        parent.getChildren().remove(this);
    }

    @Override
    protected String getDragId() {
        return BASE_ID + (tile == null ? "" : String.valueOf(tile.getTileId()));
    }

    @Override
    protected void fallbackDropHandler() {
        ClientManager.getInstance().simulateCommand("discard");
    }

    private void rotate(Rotation rotation) {
        ClientManager.getInstance().simulateCommand("rotate", rotation.toString());
        if (isDragging()) {
            this.setRotate(this.getRotate() + rotation.toDegrees());
            DragDropManager.updateSnapshot(getSnapshot());
        }
    }
}