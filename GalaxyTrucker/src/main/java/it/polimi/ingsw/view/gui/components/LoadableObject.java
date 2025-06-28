package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.gui.UIs.AdventureUI;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.Draggable;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Class representing a draggable object that can be loaded onto a ship.
 * It displays an image corresponding to its type (e.g., crew, battery, cargo).
 */
public class LoadableObject extends Draggable {
    /**
     * Base string for the drag-and-drop identifier.
     */
    public static final String BASE_ID = "LOADABLE";

    private ShipCell parentCell = null;

    private ImageView imageView;
    private double IMAGE_SIZE = 25;

    private LoadableType type;

    /**
     * Constructs a new loadable object.
     * @param dragOverlay The pane on which the object is rendered while being dragged.
     * @param type The type of the loadable object.
     * @param parentCell The initial cell containing this object, can be null.
     */
    public LoadableObject(Pane dragOverlay, LoadableType type, ShipCell parentCell) {
        super(dragOverlay);
        setType(type);
        this.parentCell = parentCell;
    }

    /**
     * Sets the type for this object and updates its visual representation.
     * @param type The new type for the object.
     */
    public void setType(LoadableType type) {
        this.type = type;
        this.imageView = AssetHandler.loadImage(Asset.fromLoadableType(type));
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        this.setViewOrder(-1);
        this.getChildren().add(imageView);
    }

    /**
     * Returns the type of the loadable object.
     * @return The {@link LoadableType}.
     */
    @Override
    public LoadableType getType(){
        return type;
    }

    /**
     * Generates a unique identifier for the drag operation, combining the base ID with the object's type.
     * @return The drag identifier string.
     */
    @Override
    protected String getDragId() {
        return BASE_ID + this.type.toString();
    }

    /**
     * Determines if the object can be dragged.
     * It can be dragged if it's not on a ship cell or if the parent cell is active for removal during the adventure phase.
     * @return True if the object can be dragged, false otherwise.
     */
    @Override
    protected boolean canBeDragged() {
        if (parentCell == null) {
            return true;
        }
        return parentCell.isActiveForAdventureRemove();
    }

    @Override
    protected void onEndDrag(boolean dropAccepted) {
        Pane parent = (Pane) this.getParent();
        if (parent == null) return;
        parent.getChildren().remove(this);
    }

    @Override
    protected void fallbackDropHandler(){
        if (parentCell != null) {
            if (parentCell.isActiveForAdventureRemove()){
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("remove",  "("+parentCell.getLogicalRow()+","+parentCell.getLogicalColumn()+")", type.toString(), "1");
                    AdventureUI.getInstance().getShipGrid().update();
                });
            }
        }
    }
}
