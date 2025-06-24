package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.view.gui.UIs.AdventureUI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class LoadableContainer extends StackPane {
    private static final double CONTAINER_WIDTH = 680;
    private static final double CONTAINER_HEIGHT = 80;
    private static final double SPACING = 10;
    private static final double PADDING = 10;
    
    private HBox loadableObjectsLayout;
    
    public LoadableContainer() {
        // Set fixed size
        this.setPrefSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        this.setMinSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        this.setMaxSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        
        // Create background rectangle
        Rectangle background = new Rectangle(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.DARKGRAY);
        background.setStrokeWidth(2);
        
        // Create horizontal layout for loadable objects
        loadableObjectsLayout = new HBox(SPACING);
        loadableObjectsLayout.setAlignment(Pos.CENTER_LEFT);
        loadableObjectsLayout.setPadding(new Insets(PADDING));
        
        // Add to StackPane
        this.getChildren().addAll(background, loadableObjectsLayout);
    }
    
    /**
     * Sets the list of loadable objects to display
     * @param loadableTypes List of LoadableType to display
     */
    public void setLoadableObjects(List<LoadableType> loadableTypes) {
        loadableObjectsLayout.getChildren().clear();
        
        if (loadableTypes != null) {
            for (LoadableType type : loadableTypes) {
                LoadableObject loadableObj = new LoadableObject(AdventureUI.getDragOverlay(), type);
                loadableObjectsLayout.getChildren().add(loadableObj);
            }
        }
    }
    
    /**
     * Adds a single loadable object to the container
     * @param loadableType The LoadableType to add
     */
    public void addLoadableObject(LoadableType loadableType) {
        LoadableObject loadableObj = new LoadableObject(AdventureUI.getDragOverlay(), loadableType);
        loadableObjectsLayout.getChildren().add(loadableObj);
    }
    
    /**
     * Clears all loadable objects from the container
     */
    public void clearLoadableObjects() {
        loadableObjectsLayout.getChildren().clear();
    }
}
