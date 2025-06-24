package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.model.cards.CardsGroup;
import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType; // Import GamePhaseType
import it.polimi.ingsw.model.game.GameData; // Import GameData
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
 * This UI is intended to be displayed full screen, with its internal components having fixed sizes.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

    public enum AssemblePane {
        PLAYER_BOARD, SHARED_BOARD
    }

    public static AssembleUI instance;

    public static AssembleUI getInstance() {
        if(instance == null) {
            instance = new AssembleUI();
        }
        return instance;
    }

    // Define fixed dimensions for components
    private static final double TOP_SCROLL_PANE_HEIGHT = 200.0; // Fixed height for the scrollable area
    private static final double MAX_TOP_SCROLL_PANE_HEIGHT = 500.0;

    private static Pane dragOverlay;
    public static Pane getDragOverlay() {
        if (dragOverlay == null) {
            dragOverlay = new Pane(); // Transparent pane above everything
            dragOverlay.setPickOnBounds(false); // Important: so it doesn't block mouse events
        }
        return dragOverlay;
    }

    private final StackPane root;

    private final GridPane mainGrid;
    private ScrollPane topScrollPane; // Changed to ScrollPane
    private UncoveredTilesGrid uncoveredTilesGrid; // Maintain a reference to DrawnTilesGrid
    private ShipGrid leftGrid;
    private CoveredTilesPane rightPane;
    private VBox topRightVBox;

    private BoardComponent boardComponent; // Field for BoardUI

    // Overlays
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

    public void setAssembleLayout(AssemblePane paneToShow){
        System.out.println("Switching to: " + paneToShow);
        System.out.println("MainGrid visible before: " + mainGrid.isVisible());
        System.out.println("BoardComponent visible before: " + boardComponent.isVisible());

        mainGrid.setVisible(paneToShow == AssemblePane.PLAYER_BOARD);
        boardComponent.setVisible(paneToShow == AssemblePane.SHARED_BOARD);

        System.out.println("MainGrid visible after: " + mainGrid.isVisible());
        System.out.println("BoardComponent visible after: " + boardComponent.isVisible());

        // Verifica anche i children del root
        System.out.println("Root children count: " + root.getChildren().size());
        for (int i = 0; i < root.getChildren().size(); i++) {
            System.out.println("Child " + i + ": " + root.getChildren().get(i).getClass().getSimpleName() +
                    " - Visible: " + root.getChildren().get(i).isVisible());
        }
    }

    /**
     * Creates the scrollable pane for displaying drawn tiles with a fixed height.
     */
    private ScrollPane createDrawnTilesScrollPane() {
        uncoveredTilesGrid = new UncoveredTilesGrid(); // Create the instance of DrawnTilesGrid
        ScrollPane scrollPane = new ScrollPane(uncoveredTilesGrid); // Insert DrawnTilesGrid into the ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Impostazioni per l'altezza dinamica
        scrollPane.setMinHeight(TOP_SCROLL_PANE_HEIGHT);
        scrollPane.setPrefHeight(TOP_SCROLL_PANE_HEIGHT);
        scrollPane.setMaxHeight(MAX_TOP_SCROLL_PANE_HEIGHT);

        return scrollPane;
    }

    public StackPane getRoot() {
        return root;
    }

    /**
     * Creates the ship grid. ShipGrid is responsible for its own fixed dimensions.
     */
    private ShipGrid createShipGrid() {
        return new ShipGrid(LobbyState.getGameLevel());
    }

    /**
     * Creates the pane containing covered tiles. CoveredTilesPane is responsible for its own fixed dimensions.
     */
    private CoveredTilesPane createCoveredTilesPane() {
        return new CoveredTilesPane();
    }

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
            });
        });

        Button boardButton = getBoardButton();

        decksAndTimerGrid.getChildren().addAll(timerComponent, flipButton, finishButton, boardButton);

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

    private Button getBoardButton() {
        Button boardButton = new Button("Board");
        boardButton.setOnMouseClicked(event -> {
            setAssembleLayout(AssemblePane.SHARED_BOARD);
            boardComponent.addPlayers();
        });
        return boardButton;
    }

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
     * Returns the main layout for this UI.
     * This GridPane is intended to be the root of a full-screen scene.
     */
    public StackPane getLayout() {
        return root;
    }

    /**
     * Updates the UI based on client updates.
     * Recreates the drawn tiles scroll pane, ensuring its fixed size is maintained.
     * Transitions to AdventureUI if the game phase changes to ADVENTURE.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        GameData gameData = update.getCurrentGame();

        if (gameData != null && gameData.getCurrentGamePhaseType() == GamePhaseType.ADVENTURE) {
            Platform.runLater(() -> {
                ClientManager.getInstance().updateScene(new AdventureUI());
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
}
