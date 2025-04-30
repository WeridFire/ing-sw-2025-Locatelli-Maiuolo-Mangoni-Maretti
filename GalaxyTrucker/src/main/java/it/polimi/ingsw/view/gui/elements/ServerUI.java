package it.polimi.ingsw.view.gui.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ServerUI {
    private final VBox layout;

    public ServerUI(String statusText) {
        Label statusLabel = new Label(statusText);

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(statusLabel);
    }

    public VBox getLayout() {
        return layout;
    }
}