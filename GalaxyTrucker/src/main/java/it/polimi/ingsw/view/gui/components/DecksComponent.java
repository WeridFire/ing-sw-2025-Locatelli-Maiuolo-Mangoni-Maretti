package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;


public class DecksComponent extends HBox {

    public DecksComponent() {
        Button deck1Button = new Button("Deck 1");
        deck1Button.setOnMouseClicked(event -> {handleGetDeck(1);});
        deck1Button.setFocusTraversable(false);

        Button deck2Button = new Button("Deck 2");
        deck2Button.setOnMouseClicked(event -> {handleGetDeck(2);});
        deck2Button.setFocusTraversable(false);

        Button deck3Button = new Button("Deck 3");
        deck3Button.setOnMouseClicked(event -> {handleGetDeck(3);});
        deck3Button.setFocusTraversable(false);

        this.getChildren().addAll(deck1Button, deck2Button, deck3Button);

        this.setSpacing(10);
    }

    public void handleGetDeck(int deckNumber) {
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("showcg", Integer.toString(deckNumber));
        });
    }
}