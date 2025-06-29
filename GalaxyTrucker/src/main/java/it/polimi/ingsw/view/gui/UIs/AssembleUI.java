package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.model.cards.CardsGroup;
import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.*;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * UI component for the ship assembly phase of the game.
 * This UI is intended to be displayed full-screen, with its internal components having fixed sizes.
 * It follows a singleton pattern to ensure a single instance throughout the application lifecycle.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

    /**
     * Defines the different views available within the AssembleUI,
     * such as the player's personal board or the shared game board.
     */
    public enum AssemblePane {
        PLAYER_BOARD, SHARED_BOARD
    }

    /**
     * The singleton instance of the AssembleUI.
     */
    public static AssembleUI instance;

    /**
     * Returns the singleton instance of the AssembleUI.
     * If the instance does not exist, it is created.
     * @return The single instance of this class.
     */
    public static AssembleUI getInstance() {
        if(instance == null) {
            instance = new AssembleUI();
        }
        return instance;
    }

    /**
     * Defines the minimum height for the scrollable area containing drawn tiles.
     */
    private static final double TOP_SCROLL_PANE_HEIGHT = 200.0;
    /**
     * Defines the maximum height for the scrollable area containing drawn tiles.
     */
    private static final double MAX_TOP_SCROLL_PANE_HEIGHT = 500.0;

    /**
     * The overlay pane used for drag-and-drop operations.
     */
    private static Pane dragOverlay;

    /**
     * Returns the singleton overlay pane used for drag-and-drop operations.
     * This pane sits on top of all other components to render dragged items.
     * @return The drag overlay pane.
     */
    public static Pane getDragOverlay() {
        if (dragOverlay == null) {
            dragOverlay = new Pane(); // Transparent pane above everything
            dragOverlay.setPickOnBounds(false); // Important: so it doesn't block mouse events
        }
        return dragOverlay;
    }

    /**
     * The root container for the entire Assemble UI.
     */
    private final StackPane root;

    /**
     * The main grid layout for the player-specific view.
     */
    private final GridPane mainGrid;
    /**
     * The scrollable pane displaying the uncovered tiles available to the player.
     */
    private ScrollPane topScrollPane;
    /**
     * The grid component containing the uncovered tiles.
     */
    private UncoveredTilesGrid uncoveredTilesGrid;
    /**
     * The component displaying the player's ship grid.
     */
    private ShipGrid leftGrid;
    /**
     * The pane displaying the face-down covered tiles.
     */
    private CoveredTilesPane rightPane;
    /**
     * The vertical box containing decks, timer, and action buttons.
     */
    private VBox topRightVBox;

    /**
     * The component that displays the shared game board.
     */
    private BoardComponent boardComponent;

    /**
     * The overlay grid for showing the contents of a drawn card group.
     */
    private ShowDeckGrid showDeckGrid;


    /**
     * Creates a new assembly UI with all required components.
     * The UI is designed for full-screen display with fixed-size internal areas.
     */
    private AssembleUI() {
        // main grid for player own board
        mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.CENTER);
        // Initialize components
        leftGrid = createShipGrid();
        rightPane = createCoveredTilesPane();
        topScrollPane = createDrawnTilesScrollPane();
        topRightVBox = createDecksAndTimerGrid();
        // set equal width
        double calculatedTopScrollPaneWidth = leftGrid.getPrefWidth();
        topScrollPane.setPrefWidth(calculatedTopScrollPaneWidth);
        topScrollPane.setMinWidth(calculatedTopScrollPaneWidth);
        topScrollPane.setMaxWidth(calculatedTopScrollPaneWidth);
        // add elements to main grid
        mainGrid.add(topScrollPane, 0, 0);
        mainGrid.add(leftGrid, 0, 1);
        mainGrid.add(rightPane, 1, 1);
        mainGrid.add(topRightVBox, 1, 0);

        // board component for shared board
        boardComponent = BoardComponent.create(LobbyState.getGameLevel());
        boardComponent.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

        // this scene root
        root = new StackPane();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(mainGrid, boardComponent, getDragOverlay());

        // set starting view
        setAssembleLayout(AssemblePane.PLAYER_BOARD);
    }

    /**
     * Switches the visible layout between the player's board and the shared game board.
     * @param paneToShow The pane to display.
     */
    public void setAssembleLayout(AssemblePane paneToShow){
        mainGrid.setVisible(paneToShow == AssemblePane.PLAYER_BOARD);
        boardComponent.setVisible(paneToShow == AssemblePane.SHARED_BOARD);

        //TODO: remove these 3 lines
        Button cheatButton = new Button("cheat");
        cheatButton.setOnMouseClicked(event -> handleCheatButton());
        root.getChildren().add(cheatButton);
    }

    /**
     * Creates the scrollable pane for displaying drawn tiles with a dynamic height.
     * @return A configured {@link ScrollPane}.
     */
    private ScrollPane createDrawnTilesScrollPane() {
        uncoveredTilesGrid = new UncoveredTilesGrid(); // Create the instance of DrawnTilesGrid
        ScrollPane scrollPane = new ScrollPane(uncoveredTilesGrid); // Insert DrawnTilesGrid into the ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Settings for dynamic height
        scrollPane.setMinHeight(TOP_SCROLL_PANE_HEIGHT);
        scrollPane.setPrefHeight(TOP_SCROLL_PANE_HEIGHT);
        scrollPane.setMaxHeight(MAX_TOP_SCROLL_PANE_HEIGHT);

        return scrollPane;
    }

    /**
     * Returns the root StackPane of this UI.
     * @return The root {@link StackPane}.
     */
    public StackPane getRoot() {
        return root;
    }

    /**
     * Returns the ship grid component.
     * @return The {@link ShipGrid}.
     */
    public ShipGrid getLeftGrid() {
        return leftGrid;
    }

    /**
     * Creates the ship grid component based on the current game level.
     * @return A new {@link ShipGrid} instance.
     */
    private ShipGrid createShipGrid() {
        return new ShipGrid(LobbyState.getGameLevel());
    }


    /**
     * Creates the pane containing the face-down covered tiles.
     * @return A new {@link CoveredTilesPane} instance.
     */
    private CoveredTilesPane createCoveredTilesPane() {
        return new CoveredTilesPane();
    }

    /**
     * Creates a VBox containing the timer, action buttons, and deck components.
     * @return A configured {@link VBox}.
     */
    private VBox createDecksAndTimerGrid(){
        VBox decksAndTimerGrid = new VBox();

        TimerComponent timerComponent = TimerComponent.getInstance();

        Button flipButton = new Button("Flip Timer");
        flipButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("timerflip");
            });
        });

        Button finishButton = new Button("Finish Assembling");
        finishButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("finish");
                getRoot().getChildren().remove(boardComponent);
                setFinished();
            });
        });

        Button boardButton = getBoardButton();

        SpectateVBox spectateVBox = new SpectateVBox();

        decksAndTimerGrid.getChildren().addAll(timerComponent, flipButton, finishButton, boardButton, spectateVBox);

        if (LobbyState.getGameLevel() != GameLevel.TESTFLIGHT){
            DecksComponent decksComponent = new DecksComponent();
            decksAndTimerGrid.getChildren().add(decksComponent);
        }

        decksAndTimerGrid.setSpacing(20);
        decksAndTimerGrid.setStyle("-fx-padding: 20;");

        // start timer
        if (AssembleState.isTimerRunning()) {
            timerComponent.start();
        }

        return decksAndTimerGrid;
    }

    /**
     * Creates and returns a button to switch to the shared board view.
     * @return A configured {@link Button}.
     */
    private Button getBoardButton() {
        Button boardButton = new Button("Board");
        boardButton.setOnMouseClicked(event -> {
            setAssembleLayout(AssemblePane.SHARED_BOARD);
            boardComponent.addPlayers();
        });
        return boardButton;
    }

    /**
     * Refreshes the overlay that shows the contents of a drawn card group.
     * It creates or removes the overlay based on whether a card group is in the player's hand.
     */
    private void refreshDeckOverlay(){
        Optional<CardsGroup> cardsGroup = AssembleState.getCardGroupInHand();
        if (cardsGroup.isPresent() == (showDeckGrid == null)) {
            cardsGroup.ifPresentOrElse(cg -> {
                showDeckGrid = new ShowDeckGrid(cg);
                showDeckGrid.setViewOrder(-1000);
                root.getChildren().add(showDeckGrid);
            }, () -> {
                showDeckGrid.getChildren().clear();
                root.getChildren().remove(showDeckGrid);
                showDeckGrid = null;
            });
        }
    }

    /**
     * Returns the main layout node for this UI.
     * This StackPane is intended to be the root of a full-screen scene.
     * @return The root {@link StackPane} of the UI.
     */
    public StackPane getLayout() {
        return root;
    }

    /**
     * Transitions the UI to the adventure phase screen.
     */
    public void setFinished(){
        Platform.runLater(() -> {
            ClientManager.getInstance().updateScene(AdventureUI.getInstance());
        });
    }

    /**
     * Updates the UI based on a new client update from the server.
     * If the game phase changes to ADVENTURE, it transitions to the AdventureUI.
     * Otherwise, it refreshes the components of the assembly screen.
     * @param update The {@link ClientUpdate} containing the new game state.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        GameData gameData = update.getCurrentGame();

        if (gameData != null && (gameData.getCurrentGamePhaseType() == GamePhaseType.ADVENTURE)) {
            Platform.runLater(() -> {
                ClientManager.getInstance().updateScene(AdventureUI.getInstance());
            });
            return; // Scene is changing, no need to update AssembleUI components
        }

        Platform.runLater(() -> {
            if (AssembleState.isTimerRunning() && TimerComponent.getInstance() != null && !TimerComponent.getInstance().isRunning()) {
                TimerComponent.getInstance().start();
            }

            refreshDeckOverlay();

            if (uncoveredTilesGrid != null) {
                uncoveredTilesGrid.update();
            }
            if (leftGrid != null) {
                leftGrid.update();
            }
        });
    }

    /**
     * Handles the action for the cheat button. For debugging purposes.
     * TODO: Remove this method and the associated button.
     */
    public void handleCheatButton() {
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("cheat", "standard");
        });
    }
}
