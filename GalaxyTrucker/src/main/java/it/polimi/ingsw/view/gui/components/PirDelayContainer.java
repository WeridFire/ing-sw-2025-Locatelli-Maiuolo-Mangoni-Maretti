package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.view.cli.ANSI;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PirDelayContainer extends StackPane {

    private final VBox content;

    public PirDelayContainer() {
        // Initialize content in the constructor
        this.content = new VBox();
        this.content.setSpacing(10); // Add some spacing for better layout
        this.content.setAlignment(Pos.CENTER);

        // Add a more suitable background for dialog
        this.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 5;");

        this.getChildren().add(content);
    }

    public void handlePirDelay(PIRDelay pir){
        content.getChildren().clear();
        content.getChildren().add(getLabel(pir.getMessage()));
    }

    private Label getLabel(String labelText) {
        Label labelObj = new Label(ANSI.Helper.stripAnsi(labelText)); // Using the parameter instead of the field
        labelObj.setWrapText(true);
        labelObj.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); // Changed to black text
        labelObj.setViewOrder(-1);
        return labelObj;
    }
}
