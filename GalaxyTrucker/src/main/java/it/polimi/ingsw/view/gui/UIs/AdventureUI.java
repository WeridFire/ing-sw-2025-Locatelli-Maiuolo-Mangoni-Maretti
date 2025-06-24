package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.BoardComponent;
import it.polimi.ingsw.view.gui.components.CardContainer;
import it.polimi.ingsw.view.gui.components.LoadableContainer;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class AdventureUI implements INodeRefreshableOnUpdateUI {

    private PIR lastPIR;

    private GridPane mainLayout;
    private BoardComponent boardComponent;
    private ShipGrid shipGrid;
    private CardContainer cardPane;
    private LoadableContainer loadableContainer;
    private PIRState pirContainer;

    private static Pane dragOverlay;
    public static Pane getDragOverlay() {
        if (dragOverlay == null) {
            dragOverlay = new Pane(); // Transparent pane above everything
            dragOverlay.setPickOnBounds(false); // Important: so it doesn't block mouse events
        }
        return dragOverlay;
    }

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

        // Create loadable container
        loadableContainer = new LoadableContainer();

        // Add components to the grid
        mainLayout.add(loadableContainer, 0, 0, 2, 1);  // Spanning across both columns at top
        mainLayout.add(boardComponent, 0, 1, 2, 1);
        mainLayout.add(shipGrid, 0, 2);
        mainLayout.add(cardPane, 1, 2);
        mainLayout.setAlignment(Pos.CENTER);

        // Utile per il debug del layout, mostra le linee della griglia:
        mainLayout.setGridLinesVisible(true);
    }

    public void updateCard() {
        cardPane.setCard(CommonState.getLastUpdate().getCurrentGame().getDeck().getCurrentCard());
    }

    /**
     * Provides access to the loadable container
     * @return The LoadableContainer.
     */
    public LoadableContainer getLoadableContainer() {
        return loadableContainer;
    }

    @Override
    public Node getLayout() {
        return mainLayout;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
       Platform.runLater(this::updateCard);
       lastPIR = PIRState.getActivePIR();

    }
}