package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.components.*;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * UI component for the ship assembly phase of the game.
 * This UI is intended to be displayed full screen, with its internal components having fixed sizes.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

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
    public AssembleUI() {
        mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.CENTER);

        root = new StackPane();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(mainGrid, getDragOverlay());

        // Initialize components
        leftGrid = createShipGrid();
        rightPane = createCoveredTilesPane();
        topScrollPane = createDrawnTilesScrollPane();
        topRightVBox = createDecksAndTimerGrid();

        double calculatedTopScrollPaneWidth = leftGrid.getPrefWidth();

        topScrollPane.setPrefWidth(calculatedTopScrollPaneWidth);
        topScrollPane.setMinWidth(calculatedTopScrollPaneWidth);
        topScrollPane.setMaxWidth(calculatedTopScrollPaneWidth);

        mainGrid.add(topScrollPane, 0, 0);
        mainGrid.add(leftGrid, 0, 1);
        mainGrid.add(rightPane, 1, 1);
        mainGrid.add(topRightVBox, 1, 0);
    }

    public void setAssembleLayout(){
        for (Node node : mainGrid.getChildren()) {
            node.setVisible(true);
        }
    }

    public void hideAssembleLayout(){
        for (Node node : mainGrid.getChildren()) {
            node.setVisible(false);
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
            if (boardComponent == null) {
                boardComponent = new BoardComponent(
                        Default.BOARD_ELLIPSE_RX,
                        Default.BOARD_ELLIPSE_RY,
                        LobbyState.getGameLevel() == GameLevel.TESTFLIGHT ?
                                Default.ELLIPSE_TESTFLIGHT_STEPS : Default.ELLIPSE_ONE_STEPS
                );
                boardComponent.setPrefSize(getRoot().getWidth(), getRoot().getHeight());
                boardComponent.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
                boardComponent.addPlayers();
                this.hideAssembleLayout();
                root.getChildren().add(boardComponent);
                StackPane.setAlignment(boardComponent, Pos.CENTER);
            }else{
                boardComponent.setVisible(true);
            }

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
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        Platform.runLater(() -> {
            if (AssembleState.isTimerRunning() && !TimerComponent.getInstance().isRunning()) {
                TimerComponent.getInstance().start();
            } else if (!AssembleState.isTimerRunning() && TimerComponent.getInstance().isRunning()) {
                TimerComponent.getInstance().stop();
            }

            refreshDeckOverlay();

            if (uncoveredTilesGrid != null) {
                uncoveredTilesGrid.update();
            }
            leftGrid.update();
        });
    }
}
