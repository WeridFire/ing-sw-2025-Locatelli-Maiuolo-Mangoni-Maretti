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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private static final double MAX_TOP_SCROLL_PANE_HEIGHT = 420.0;

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

        // Apply styling to grid components
        applyComponentStyles();

        // board component for shared board
        boardComponent = BoardComponent.create(LobbyState.getGameLevel());
        boardComponent.setStyle("-fx-border-color: #4dd0e1; -fx-border-width: 1; -fx-background-color: #1a1c2c;");

        // this scene root
        root = new StackPane();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1c2c; -fx-background: #1a1c2c;"); // Assicura lo sfondo completo
        root.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.valueOf("#1a1c2c"), 
                                     null, null)));
        root.getChildren().addAll(mainGrid, boardComponent, getDragOverlay());

        // Assicura che il StackPane root occupi tutto lo spazio disponibile
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // set starting view
        setAssembleLayout(AssemblePane.PLAYER_BOARD);
    }

    /**
     * Switches the visible layout between the player's board and the shared game board.
     * @param paneToShow The pane to display.
     */
    public void setAssembleLayout(AssemblePane paneToShow) {
        mainGrid.setVisible(paneToShow == AssemblePane.PLAYER_BOARD);
        boardComponent.setVisible(paneToShow == AssemblePane.SHARED_BOARD);

        // Assicura che lo sfondo sia applicato all'intera scena
        if (root.getScene() != null) {
            root.getScene().getRoot().setStyle("-fx-background-color: #1a1c2c;");
        }

        // TODO: remove these lines when no longer needed
        Button cheatButton = new Button("cheat");
        styleButton(cheatButton);
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
     * Applies futuristic space theme styling to all components
     */
    private void applyComponentStyles() {
        // Style for scroll pane
        topScrollPane.setStyle("-fx-background: #1a1c2c; -fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; -fx-border-width: 1;");
        
        // Style VBox
        topRightVBox.setStyle("-fx-padding: 10; -fx-spacing: 10; -fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; -fx-border-width: 1;");
        
        // Style the right pane
        rightPane.setStyle("-fx-background-color: rgba(26, 28, 44, 0.8); -fx-border-color: #444; -fx-border-width: 1;");
    }

    /**
     * Creates a VBox containing the timer, action buttons, and deck components.
     * @return A configured {@link VBox}.
     */
    private VBox createDecksAndTimerGrid() {
        VBox decksAndTimerGrid = new VBox();

        TimerComponent timerComponent = TimerComponent.getInstance();
        
        // Aggiunta di uno sfondo al timer per renderlo più leggibile
        timerComponent.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 14px; -fx-text-fill: #d0d0e0;");
        timerComponent.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.valueOf("#2a2e42"), // Colore più scuro ma distinguibile
                new CornerRadii(3), 
                null)));
        timerComponent.setPadding(new Insets(10, 15, 10, 15));
        
        Button flipButton = new Button("Flip Timer");
        styleButton(flipButton);
        flipButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("timerflip");
            });
        });

        Button finishButton = new Button("Finish Assembling");
        styleButton(finishButton);
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

        if (LobbyState.getGameLevel() != GameLevel.TESTFLIGHT) {
            DecksComponent decksComponent = new DecksComponent();
            styleDecksComponent(decksComponent);
            decksAndTimerGrid.getChildren().add(decksComponent);
        }

        decksAndTimerGrid.setSpacing(15);
        decksAndTimerGrid.setStyle("-fx-padding: 15; -fx-background-color: rgba(26, 28, 44, 0.8); -fx-border-color: #444; -fx-border-width: 1;");

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
        styleButton(boardButton);
        boardButton.setOnMouseClicked(event -> {
            setAssembleLayout(AssemblePane.SHARED_BOARD);
            boardComponent.addPlayers();
        });
        return boardButton;
    }

    /**
     * Applies consistent styling to a button
     * @param button The button to style
     */
    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #4dd0e1; -fx-border-color: #4dd0e1; " +
                       "-fx-border-width: 1px; -fx-padding: 5 12; -fx-font-size: 13px; " +
                       "-fx-background-radius: 0; -fx-border-radius: 0;");
        
        // Add hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4dd0e1; -fx-text-fill: #1a1c2c; " +
                                               "-fx-border-color: #4dd0e1; -fx-border-width: 1px; " +
                                               "-fx-padding: 5 12; -fx-font-size: 13px; " +
                                               "-fx-background-radius: 0; -fx-border-radius: 0;"));
        
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #4dd0e1; " +
                                              "-fx-border-color: #4dd0e1; -fx-border-width: 1px; " +
                                              "-fx-padding: 5 12; -fx-font-size: 13px; " +
                                              "-fx-background-radius: 0; -fx-border-radius: 0;"));
    }

    /**
     * Applies styling to the decks component
     * @param component The DecksComponent to style
     */
    private void styleDecksComponent(DecksComponent component) {
        component.setStyle("-fx-spacing: 10; -fx-padding: 5;");
        
        // Style all buttons in the component
        component.getChildren().forEach(node -> {
            if (node instanceof Button) {
                styleButton((Button)node);
            }
        });
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

    //TODO: Remove this method and the associated button.
    public void handleCheatButton() {
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("cheat", "standard");
        });
    }

    /**
     * Applies specific styles to certain components for improved readability and aesthetics.
     */
    private void applySpecificStyles() {
        // Aggiornamenti per il TimerComponent per migliorare la leggibilità
        TimerComponent timerComponent = TimerComponent.getInstance();
        timerComponent.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 14px; -fx-text-fill: #d0d0e0;");
        
        // Assicurati che tutto il testo sia visibile
        timerComponent.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.valueOf("#1a1c2c"), 
                new CornerRadii(5), 
                null)));
        timerComponent.setPadding(new Insets(5, 10, 5, 10));
    }
}
