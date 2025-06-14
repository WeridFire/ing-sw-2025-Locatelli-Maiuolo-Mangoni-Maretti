package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.BoardComponent;
import it.polimi.ingsw.view.gui.components.CardContainer;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class AdventureUI implements INodeRefreshableOnUpdateUI {

    private GridPane mainLayout;
    private BoardComponent boardComponent;
    private ShipGrid shipGrid;
    private CardContainer cardPane; // Modificato da customPane a cardPane come nel tuo codice

    public AdventureUI() {
        mainLayout = new GridPane();

        GameLevel gameLevel = LobbyState.getGameLevel();
        if (gameLevel == null) {
            System.err.println("AdventureUI: GameLevel is null, defaulting to TESTFLIGHT for component creation. This might indicate an issue.");
            gameLevel = GameLevel.TESTFLIGHT;
        }

        boardComponent = BoardComponent.create(gameLevel);
        shipGrid = new ShipGrid(gameLevel);

        cardPane = new CardContainer();
        updateCard();
        cardPane.setStyle("-fx-background-color: lightyellow;");

        // Add components to the grid
        mainLayout.add(boardComponent, 0, 0, 2, 1);
        mainLayout.add(shipGrid, 0, 1);
        mainLayout.add(cardPane, 1, 1);
        mainLayout.setAlignment(Pos.CENTER);

        // Utile per il debug del layout, mostra le linee della griglia:
        mainLayout.setGridLinesVisible(true);
    }

    public void updateCard() {
        cardPane.setCard(CommonState.getLastUpdate().getCurrentGame().getDeck().getCurrentCard());
    }

    @Override
    public Node getLayout() {
        return mainLayout;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
       Platform.runLater(this::updateCard);
    }

    /**
     * Provides access to the custom pane where other nodes can be added.
     * @return The custom Pane.
     */
    public Pane getCardPane() {
        return cardPane;
    }
}