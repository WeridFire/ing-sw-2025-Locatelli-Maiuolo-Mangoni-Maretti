package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.view.cli.ANSI;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PirDelayContainer extends VBox {

    public PirDelayContainer() {
        this.setPadding(new Insets(10));
    }

    public void setText(String text) {
        this.getChildren().clear();
        this.getChildren().add(getLabel(text));
        this.getChildren().add(TurnContainer.getInstance());
    }

    private Label getLabel(String labelText) {
        Label labelObj = new Label(ANSI.Helper.stripAnsi(labelText)); // Using the parameter instead of the field
        labelObj.setWrapText(true);
        labelObj.setMaxWidth(CardContainer.FIXED_WIDTH);
        labelObj.setMaxHeight(LoadableContainer.CONTAINER_HEIGHT);
        labelObj.setStyle("-fx-font-weight: bold; -fx-text-fill: lightgrey;");
        labelObj.setViewOrder(-1);
        return labelObj;
    }
}
