package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;


public class DecksComponent extends HBox {

    public DecksComponent() {
        Button deck1Button = new Button("Deck 1");
        deck1Button.setOnMouseClicked(event -> {handleGetDeck(1);});

        Button deck2Button = new Button("Deck 2");
        deck1Button.setOnMouseClicked(event -> {handleGetDeck(2);});

        Button deck3Button = new Button("Deck 3");
        deck1Button.setOnMouseClicked(event -> {handleGetDeck(3);});


        this.getChildren().addAll(deck1Button, deck2Button, deck3Button);

        this.setSpacing(10);
    }

    public void handleGetDeck(int deckNumber) {
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("showcg", Integer.toString(deckNumber));
        });

    }
}