package it.polimi.ingsw.view.gui.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LauncherUI {
    private final VBox layout;

    public LauncherUI(Runnable onServerStart, Runnable onClientStart) {
        Button startServerButton = new Button("Start Server");
        startServerButton.setOnAction(_ -> onServerStart.run());

        Button startClientButton = new Button("Start Client");
        startClientButton.setOnAction(_ -> onClientStart.run());

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(startServerButton, startClientButton);
    }

    public VBox getLayout() {
        return layout;
    }
}
