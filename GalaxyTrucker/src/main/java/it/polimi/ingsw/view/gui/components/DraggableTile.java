package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.DragBehaviorHandler;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
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

    private ImageView imageView;
    private Image backTileImage;
    private Image frontTileImage;
    private boolean isCovered;
    private TileSkeleton tile;
    private WhichPane position;

    final double[] clickOffset = new double[2];
    final double[] clickTileOffset = new double[2];

    /**
     * Creates a new, covered draggable tile.
     * Initially, it displays the back of a tile and is positioned in the covered tiles area.
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
     * Creates a new draggable tile based on a given {@link TileSkeleton}.
     * The tile is initially uncovered, displaying its specific face.
     *
     * @param tile The {@link TileSkeleton} defining the tile's appearance and data.
     */
    public DraggableTile(TileSkeleton tile) {
        this.position = null;
        setTile(tile);
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
            if (event.getButton() != MouseButton.PRIMARY || position == WhichPane.SHIPGRID || AssembleUI.getIsBeeingDragged() != null) {
                event.consume();
                return;
            }

            AssembleUI.setIsBeeingDragged(this);
            setPosition(WhichPane.FLOATING);

            if (isCovered) {
                ClientManager.getInstance().simulateCommand("draw");
                this.setTile(AssembleState.getTileInHand());
            } else {
                ClientManager.getInstance().simulateCommand("pick", Integer.toString(tile.getTileId()));
            }

            this.setViewOrder(-100);
            this.startFullDrag();

            clickOffset[0] = event.getSceneX() - this.getLayoutX();
            clickOffset[1] = event.getSceneY() - this.getLayoutY();

            clickTileOffset[0] = event.getX();
            clickTileOffset[1] = event.getY();
        });

        this.setOnMouseDragged(event -> {
            System.out.println(event.getX() + " " + event.getY());
            if (position == WhichPane.FLOATING) {
                double newX = event.getSceneX() - clickOffset[0];
                double newY = event.getSceneY() - clickOffset[1];
                this.setLayoutX(newX + clickTileOffset[0] + 5);
                this.setLayoutY(newY + clickTileOffset[1] + 5);
            }
        });

        this.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY && position == WhichPane.FLOATING) {
                System.out.println("Clicked at" + event.getX() + " " + event.getY());
                this.setRotate(this.getRotate() - 90);
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("rotate", "left");
                });
            }
        });
    }

    /**
     * Sets the {@link TileSkeleton} for this tile, updating its appearance to be uncovered
     * and display the tile's front face. Also applies rotation if stored in the tile model.
     *
     * @param tile The {@link TileSkeleton} to associate with this draggable tile.
     */
    public void setTile(TileSkeleton tile) {
        this.tile = tile;

        if (imageView == null) {
            imageView = new ImageView();
        }

        if (tile != null) {
            this.frontTileImage = AssetHandler.loadRawImage(tile.getTextureName());
            this.isCovered = false;
            imageView.setImage(this.frontTileImage);
            this.setRotate(tile.getAppliedRotation().toDouble());
        } else {
            this.isCovered = true;
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
}

