package it.polimi.ingsw.view.gui.UIs;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LobbyUI {
    private final VBox layout;

    public LobbyUI(String username) {
        Label statusLabel = new Label(username);

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(statusLabel);
    }

    public VBox getLayout() {
        return layout;
    }
}
