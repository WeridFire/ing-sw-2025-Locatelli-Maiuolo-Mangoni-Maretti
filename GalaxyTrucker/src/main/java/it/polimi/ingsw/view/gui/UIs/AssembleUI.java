package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.*;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.sql.Time;
import java.util.Optional;

/**
 * UI component for the ship assembly phase of the game.
 * This UI is intended to be displayed full screen, with its internal components having fixed sizes.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

    private static CardsGroup watchedCardsGroup;

    public static CardsGroup getWatchedCardsGroup() {
        return watchedCardsGroup;
    }
    public static void setWatchedCardsGroup(CardsGroup watchedCardsGroup) {
        AssembleUI.watchedCardsGroup = watchedCardsGroup;
    }

    public static AssembleUI instance;

    public static AssembleUI getInstance() {
        if(instance == null) {
            instance = new AssembleUI();
        }
        return instance;
    };

    // Define fixed dimensions for components
    private static final double TOP_SCROLL_PANE_HEIGHT = 200.0; // Fixed height for the scrollable area
    private static final double MAX_TOP_SCROLL_PANE_HEIGHT = 500.0;

    private static Pane dragOverlay;
    public static Pane getDragOverlay() {
        if (dragOverlay == null) {
            dragOverlay = new Pane();
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
        int rows, cols;
        cols = switch (LobbyState.getGameLevel()) {
            case TESTFLIGHT, ONE -> {
                rows = 5;
                yield 7;
            }
            case TWO -> {
                rows = 6;
                yield 9;
            }
            // Add a default case to avoid errors if GameLevel is unexpected
            default -> {
                rows = 5; // Default values
                yield 7;
            }
        };
        return new ShipGrid(rows, cols);
    }

    /**
     * Creates the pane containing covered tiles. CoveredTilesPane is responsible for its own fixed dimensions.
     */
    private CoveredTilesPane createCoveredTilesPane() {
        return new CoveredTilesPane();
    }

    private VBox createDecksAndTimerGrid(){
        VBox decksAndTimerGrid = new VBox();

        DecksComponent decksComponent = new DecksComponent();
        TimerComponent timerComponent = TimerComponent.getInstance();

        Button flipButton = new Button("Flip Timer");
        flipButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("timerflip");
            });
        });

        Button finishButton = new Button("Finish Timer");
        finishButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("finish");
            });
        });

        decksAndTimerGrid.getChildren().addAll(timerComponent, decksComponent, flipButton, finishButton);

        decksAndTimerGrid.setSpacing(20);
        decksAndTimerGrid.setStyle("-fx-padding: 20;");

        return decksAndTimerGrid;
    }

    public void showDeckOverlay(){
        Optional<CardsGroup> cg = AssembleState.getCardGroupInHand();
        if (cg.isPresent()) {
            showDeckGrid = new ShowDeckGrid(cg.get());
            showDeckGrid.setViewOrder(-1000);
            root.getChildren().add(showDeckGrid);
        }
    }

    public void clearDeckOverlay(){
        if ((showDeckGrid != null) && getWatchedCardsGroup()==null){
            showDeckGrid.getChildren().clear();
            root.getChildren().remove(showDeckGrid);
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
            if (AssembleState.getGameData().isAssemblyTimerRunning() && !TimerComponent.getInstance().isRunning()) {
                TimerComponent.getInstance().start();
            }

            showDeckOverlay();

            if (uncoveredTilesGrid != null) {
                uncoveredTilesGrid.update();
            }
            leftGrid.update();
        });
    }
}
