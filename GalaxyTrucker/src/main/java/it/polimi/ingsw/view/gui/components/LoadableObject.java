package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.Draggable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class LoadableObject extends Draggable {
    private ImageView imageView;
    private double IMAGE_SIZE = 50;

    private LoadableType type;

    public LoadableObject(Pane dragOverlay, LoadableType type) {
        super(dragOverlay);
        setType(type);
    }

    public void setType(LoadableType type) {
        this.type = type;
        this.imageView = AssetHandler.loadImage(Asset.fromLoadableType(type));
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        this.getChildren().add(imageView);
    }

    @Override
    protected String getDragId() {
        return this.type.toString();
    }

    @Override
    protected boolean canBeDragged() {
        return true;
    }

    @Override
    protected void onBeginDrag() {

    }

}
