package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Represents a draggable tile component in the UI.
 * Tiles can be covered or uncovered, displaying either a back image or a specific tile face.
 * They support drag-and-drop operations for game interactions like drawing, picking, and placing.
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
     * Creates a new, covered draggable tile.
     * Initially, it displays the back of a tile and is positioned in the covered tiles area.
     */
    public DraggableTile() {
        this.backTileImage = AssetHandler.loadRawImage(Default.BACK_TILE_PATH);
        this.frontTileImage = null; // No specific tile face initially
        this.tile = null;           // No specific tile data initially
        this.imageView = new ImageView(this.backTileImage);
        this.isCovered = true;
        this.position = WhichPane.COVERED;
        initialize();
    }

    /**
     * Creates a new draggable tile based on a given {@link TileSkeleton}.
     * The tile is initially uncovered, displaying its specific face.
     *
     * @param tile The {@link TileSkeleton} defining the tile's appearance and data.
     */
    public DraggableTile(TileSkeleton tile) {
        this.backTileImage = AssetHandler.loadRawImage(Default.BACK_TILE_PATH);
        this.frontTileImage = AssetHandler.loadRawImage(tile.getTextureName());
        this.tile = tile;
        this.position = null; // Position to be determined by placement
        this.isCovered = false;
        this.imageView = new ImageView(this.frontTileImage);
        initialize();
    }

    /**
     * Initializes the visual properties and drag behavior of the tile.
     * Sets up the image view, default size, border, and configures drag-and-drop handlers.
     */
    private void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(DEFAULT_SIZE);
        imageView.setFitHeight(DEFAULT_SIZE);
        setBorderColor(Color.WHITE, DEFAULT_BORDER_WIDTH); // Default border
        this.getChildren().add(imageView);

        configureDragBehavior();
    }

    /**
     * Configures the drag-and-drop event handlers for this tile.
     * Handles drag detection, drag view setup, and actions upon drag completion or failure.
     */
    private void configureDragBehavior() {
        this.setOnDragDetected(event -> {
            if (position == WhichPane.SHIPGRID) { // Tiles on the ship grid cannot be dragged again
                event.consume();
                return;
            }
            AssembleUI.setIsBeeingDragged(this);
            setPosition(WhichPane.FLOATING); // Mark as floating during drag

            Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(DRAG_IDENTIFIER); // Identify the dragged content as a tile

            Image dragViewImage;

            if (isCovered) {
                // Action for dragging a covered tile (drawing a new tile)
                ClientManager.getInstance().simulateCommand("draw");
                TileSkeleton drawnTile = AssembleState.getTileInHand();
                if (drawnTile != null) {
                    setTile(drawnTile); // Reveal the drawn tile
                    dragViewImage = addBorderToImage(this.frontTileImage, (int) DEFAULT_BORDER_WIDTH, Color.WHITE);
                    db.setDragView(dragViewImage);
                } else {
                    // Handle case where no tile could be drawn (e.g., pile empty)
                    event.consume(); // Cancel drag if no tile drawn
                    return;
                }
            } else {
                // Action for dragging an already revealed tile (picking from drawn tiles)
                handlePickTile(tile.getTileId());
                dragViewImage = addBorderToImage(this.frontTileImage, (int) DEFAULT_BORDER_WIDTH, Color.WHITE);
                db.setDragView(dragViewImage);
            }

            db.setContent(content);

            // Remove tile from its original parent pane during drag
            Platform.runLater(() -> {
                Pane originalParent = (Pane) this.getParent();
                if (originalParent != null) {
                    originalParent.getChildren().remove(this);
                }
            });
            event.consume();
        });

        this.setOnDragDone(event -> {
            // If tile was floating and drag was not successful (not dropped on a valid target)
            if (position == WhichPane.FLOATING) {
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("discard"); // Discard the tile
                });
            }
            event.consume();
        });
    }

    /**
     * Adds a border to a given image.
     *
     * @param image The original image.
     * @param borderThickness The thickness of the border.
     * @param borderColor The color of the border.
     * @return A new {@link WritableImage} with the added border.
     */
    private static Image addBorderToImage(Image image, int borderThickness, Color borderColor) {
        if (image == null) return null;
        int newWidth = (int) image.getWidth() + 2 * borderThickness;
        int newHeight = (int) image.getHeight() + 2 * borderThickness;
        WritableImage borderedImage = new WritableImage(newWidth, newHeight);
        PixelWriter writer = borderedImage.getPixelWriter();
        PixelReader reader = image.getPixelReader();

        // Fill border
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                if (x < borderThickness || x >= newWidth - borderThickness ||
                    y < borderThickness || y >= newHeight - borderThickness) {
                    writer.setColor(x, y, borderColor);
                }
            }
        }
        // Draw original image onto the center
        for (int y = 0; y < (int)image.getHeight(); y++) {
            for (int x = 0; x < (int)image.getWidth(); x++) {
                writer.setColor(x + borderThickness, y + borderThickness, reader.getColor(x,y));
            }
        }
        return borderedImage;
    }

    /**
     * Sets the image used for the back of the tile.
     * If the tile is currently covered, its displayed image is updated.
     *
     * @param imagePath The file path to the back tile image.
     */
    public void setBackTileImage(String imagePath) {
        this.backTileImage = AssetHandler.loadRawImage(imagePath);
        if (isCovered) {
            imageView.setImage(this.backTileImage);
        }
    }

    /**
     * Sets the display size of the tile.
     *
     * @param width The desired width of the tile.
     * @param height The desired height of the tile.
     */
    public void setSize(double width, double height) {
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
    }

    /**
     * Sets the border color and width for the tile.
     *
     * @param color The {@link Color} of the border.
     * @param width The width of the border.
     */
    public void setBorderColor(Color color, double width) {
        this.setBorder(new Border(
                new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(width))));
    }

    /**
     * Gets the current logical position of the tile within the UI (e.g., covered pile, drawn area).
     *
     * @return The {@link WhichPane} representing the tile's current position.
     */
    public WhichPane getPosition() {
        return position;
    }

    /**
     * Sets the logical position of the tile.
     *
     * @param position The new {@link WhichPane} position for the tile.
     */
    public void setPosition(WhichPane position) {
        this.position = position;
    }

    /**
     * Sets the {@link TileSkeleton} for this tile, updating its appearance to be uncovered
     * and display the tile's front face.
     *
     * @param tile The {@link TileSkeleton} to associate with this draggable tile.
     */
    public void setTile(TileSkeleton tile) {
        this.tile = tile;
        if (tile != null) {
            this.frontTileImage = AssetHandler.loadRawImage(tile.getTextureName());
            this.isCovered = false;
            imageView.setImage(this.frontTileImage);
        } else {
            // Optionally handle null tile case, e.g., revert to covered or show placeholder
            this.isCovered = true;
            imageView.setImage(this.backTileImage);
        }
    }

    /**
     * Simulates a "pick" command for the tile with the given ID.
     * This is typically called when an already revealed tile is interacted with.
     *
     * @param id The ID of the tile being picked.
     */
    public void handlePickTile(int id) {
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("pick", Integer.toString(id));
        });
    }

    /**
     * Gets the image representing the front face of the tile.
     *
     * @return The {@link Image} for the front of the tile, or null if not set.
     */
    public Image getFrontTileImage() {
        return frontTileImage;
    }

    /**
     * Gets the {@link ImageView} component used to display this tile's image.
     *
     * @return The {@link ImageView} for this tile.
     */
    public ImageView getImageView() {
        return imageView;
    }
}

