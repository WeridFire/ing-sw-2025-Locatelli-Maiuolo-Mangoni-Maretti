package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * A draggable tile component that can be moved around in the UI.
 */
public class DraggableTile extends StackPane {
    private static final double DEFAULT_SIZE = 80;
    private static final double DEFAULT_BORDER_WIDTH = 2;
    private static final String DRAG_IDENTIFIER = "tile";

    private final ImageView imageView;
    private Image backTileImage;
    private Image frontTileImage;
    private boolean isCovered;
    private TileSkeleton tile;
    private WhichPane position;

    /**
     * Creates a new covered draggable tile.
     */
    public DraggableTile() {
        this.backTileImage = AssetHandler.loadRawImage(Default.BACK_TILE_PATH);
        this.frontTileImage = null;
        this.tile = null;
        this.imageView = new ImageView(this.backTileImage);
        this.isCovered = true;
        this.position = WhichPane.COVERED;
        initialize();
    }

    /**
     * Creates a new draggable tile with the specified tile skeleton.
     * 
     * @param tile The tile skeleton to display
     */
    public DraggableTile(TileSkeleton tile) {
        this.backTileImage = AssetHandler.loadRawImage(Default.BACK_TILE_PATH);
        this.frontTileImage = AssetHandler.loadRawImage(tile.getTextureName());
        this.tile = tile;
        this.position = null;
        this.isCovered = false;
        this.imageView = new ImageView(this.frontTileImage);
        initialize();
    }

    /**
     * Initialize the tile view and setup drag behaviors.
     */
    private void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(DEFAULT_SIZE);
        imageView.setFitHeight(DEFAULT_SIZE);
        setBorderColor(Color.WHITE, DEFAULT_BORDER_WIDTH);
        this.getChildren().add(imageView);

        configureDragBehavior();
    }

    /**
     * Configure the drag and drop behavior for this tile.
     */
    private void configureDragBehavior() {
        // Drag start
        this.setOnDragDetected(event -> {
            if (position == WhichPane.SHIPGRID) {
                return;
            }

            if (position == WhichPane.DRAWN) {}

            setPosition(WhichPane.FLOATING);
            Dragboard dragboard = imageView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(DRAG_IDENTIFIER);
            dragboard.setContent(content);

            AssembleUI.setIsBeeingDragged(this);

            // Handle covered vs uncovered tiles differently
            if (isCovered) {
                handleDrawTile(event);
                dragboard.setDragView(backTileImage);
            } else {
                dragboard.setDragView(frontTileImage);
            }
        });

        this.setOnDragDone(event -> {
            if (position == WhichPane.FLOATING) {
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("discard");
                });
            }
        });
    }

    /**
     * Sets the back tile image.
     * 
     * @param imagePath Path to the image
     */
    public void setBackTileImage(String imagePath) {
        this.backTileImage = AssetHandler.loadRawImage(imagePath);
        if (isCovered) {
            imageView.setImage(this.backTileImage);
        }
    }

    /**
     * Sets the size of the tile.
     * 
     * @param width Width of the tile
     * @param height Height of the tile
     */
    public void setSize(double width, double height) {
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
    }

    /**
     * Sets the border color of the tile.
     * 
     * @param color Color of the border
     * @param width Width of the border
     */
    public void setBorderColor(Color color, double width) {
        this.setBorder(new Border(
                new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(width))));
    }

    /**
     * Gets the current position of the tile.
     * 
     * @return Current position
     */
    public WhichPane getPosition() {
        return position;
    }

    /**
     * Sets the position of the tile.
     * 
     * @param position New position
     */
    public void setPosition(WhichPane position) {
        this.position = position;
    }

    /**
     * Sets the tile skeleton and updates the image.
     * 
     * @param tile Tile skeleton
     */
    public void setTile(TileSkeleton tile) {
        this.tile = tile;
        this.frontTileImage = AssetHandler.loadRawImage(tile.getTextureName());
        this.isCovered = false;
        imageView.setImage(this.frontTileImage);
    }

    /**
     * Handles the draw tile action when a covered tile is dragged.
     * 
     * @param event Mouse event that triggered the drag
     */
    public void handleDrawTile(MouseEvent event) {
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("draw");
        });
    }

    /**
     * Gets the front tile image.
     * 
     * @return Front tile image
     */
    public Image getFrontTileImage() {
        return frontTileImage;
    }

    /**
     * Gets the image view for this tile.
     * 
     * @return Tile image view
     */
    public ImageView getImageView() {
        return imageView;
    }

}

