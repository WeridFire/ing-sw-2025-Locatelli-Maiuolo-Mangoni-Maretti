package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

public class AdventureUI implements INodeRefreshableOnUpdateUI {

    private PIR lastPIR;

    private GridPane mainLayout;
    private BoardComponent boardComponent;
    private ShipGrid shipGrid;
    private CardContainer cardPane;
    private LoadableContainer loadableContainer;
    private PIRContainer pirContainer = new PIRContainer();

    private Rectangle overlayBackground;

    private static Pane dragOverlay;
    public static Pane getDragOverlay() {
        if (dragOverlay == null) {
            dragOverlay = new Pane(); // Transparent pane above everything
            dragOverlay.setPickOnBounds(false); // Important: so it doesn't block mouse events
        }
        return dragOverlay;
    }

    private static StackPane root;
    public static StackPane getRoot() {
        if (root == null) {
            root = new StackPane();
        }
        return root;
    }

    private static AdventureUI instance;
    public static AdventureUI getInstance() {
        if (instance == null) {
            instance = new AdventureUI();
        }
        return instance;
    }

    public AdventureUI() {
        root = new StackPane();

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

        root.getChildren().add(mainLayout);
    }

    public ShipGrid getShipGrid(){
        return shipGrid;
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

    public void showPirContainer() {
        overlayBackground = new Rectangle();
        overlayBackground.setFill(Color.rgb(0, 0, 0, 0.6));
        overlayBackground.widthProperty().bind(getRoot().widthProperty());
        overlayBackground.heightProperty().bind(getRoot().heightProperty());

        this.pirContainer.setAlignment(Pos.CENTER);
        root.getChildren().addAll(pirContainer, overlayBackground);
    }

    public void hidePirContainer() {
        getRoot().getChildren().remove(overlayBackground);
        getRoot().getChildren().remove(pirContainer);
    }

    @Override
    public Node getLayout() {
        return root;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        Platform.runLater(() -> {
            // Aggiorna la carta
            updateCard();

            // Gestisci il PIR
            lastPIR = PIRState.getActivePIR();
            if (lastPIR != null) {
                try {
                    System.out.println("Setting pir to " + lastPIR.getPIRType());
                    pirContainer.setPir(lastPIR);
                    if (!root.getChildren().contains(pirContainer)) {
                        showPirContainer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                hidePirContainer();
            }
        });
    }
}