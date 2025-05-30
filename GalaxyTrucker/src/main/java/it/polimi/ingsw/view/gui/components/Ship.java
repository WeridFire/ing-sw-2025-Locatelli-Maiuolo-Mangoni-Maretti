package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.player.Player;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;

public class Ship {
    private final Shape shapeView;
    private int currentStepIndex;
    private Player player;


    public Ship(Shape shipShape, int initialStepIndex, Player player) {
        this.player = player;
        this.shapeView = shipShape;
        this.currentStepIndex = initialStepIndex;
        // Allineamento cruciale per il funzionamento di translateX/Y
        StackPane.setAlignment(this.shapeView, Pos.TOP_LEFT);
    }

    public Shape getShapeView() {
        return shapeView;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void setCurrentStepIndex(int currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }

    public double getWidth() {
        return shapeView.getBoundsInLocal().getWidth();
    }

    public double getHeight() {
        return shapeView.getBoundsInLocal().getHeight();
    }
}