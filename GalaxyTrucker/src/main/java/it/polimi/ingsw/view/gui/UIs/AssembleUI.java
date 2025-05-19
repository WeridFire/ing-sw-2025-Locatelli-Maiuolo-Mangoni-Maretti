package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.CoveredTilesPane;
import it.polimi.ingsw.view.gui.components.DraggableTile;
import it.polimi.ingsw.view.gui.components.DrawnTilesGrid;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import it.polimi.ingsw.view.gui.helpers.DragBehaviorHandler;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * UI component for the ship assembly phase of the game.
 * This UI is intended to be displayed full screen, with its internal components having fixed sizes.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

    // Static
    private static DraggableTile isBeeingDragged;

    public static void setIsBeeingDragged(DraggableTile isBeeingDragged) {
        AssembleUI.isBeeingDragged = isBeeingDragged;
    }

    public static DraggableTile getIsBeeingDragged() {
        return isBeeingDragged;
    }

    // Define fixed dimensions for components
    private static final double TOP_SCROLL_PANE_HEIGHT = 250.0; // Fixed height for the scrollable area
    private static final double MAX_TOP_SCROLL_PANE_HEIGHT = 500.0;

    private final GridPane mainGrid;
    private ScrollPane topScrollPane; // Changed to ScrollPane
    private DrawnTilesGrid drawnTilesGrid; // Maintain a reference to DrawnTilesGrid
    private ShipGrid leftGrid;
    private CoveredTilesPane rightPane;

    /**
     * Creates a new assembly UI with all required components.
     * The UI is designed for full-screen display with fixed-size internal areas.
     */
    public AssembleUI() {
        mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.CENTER);
        DragBehaviorHandler.setGeneralDropBehavior(mainGrid);

        // Initialize components
        leftGrid = createShipGrid();
        rightPane = createCoveredTilesPane();
        topScrollPane = createDrawnTilesScrollPane();

        DragBehaviorHandler.setGeneralDropBehavior(leftGrid);
        DragBehaviorHandler.setGeneralDropBehavior(rightPane);
        DragBehaviorHandler.setGeneralDropBehavior((Pane) topScrollPane.getContent());

        // Set the width of the ScrollPane based on the underlying components
        double calculatedTopScrollPaneWidth = leftGrid.getPrefWidth() + rightPane.getPrefWidth();
        if (mainGrid.getHgap() > 0 && mainGrid.getColumnCount() > 1) {
            calculatedTopScrollPaneWidth += mainGrid.getHgap() * (mainGrid.getColumnCount() - 1);
        }

        topScrollPane.setPrefWidth(calculatedTopScrollPaneWidth);
        topScrollPane.setMinWidth(calculatedTopScrollPaneWidth);
        topScrollPane.setMaxWidth(calculatedTopScrollPaneWidth);

        // Setup layout
        mainGrid.add(topScrollPane, 0, 0, 2, 1); // Add the ScrollPane
        mainGrid.add(leftGrid, 0, 1);
        mainGrid.add(rightPane, 1, 1);
    }

    /**
     * Creates the scrollable pane for displaying drawn tiles with a fixed height.
     */
    private ScrollPane createDrawnTilesScrollPane() {
        drawnTilesGrid = new DrawnTilesGrid(); // Create the instance of DrawnTilesGrid
        ScrollPane scrollPane = new ScrollPane(drawnTilesGrid); // Insert DrawnTilesGrid into the ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Impostazioni per l'altezza dinamica
        scrollPane.setMinHeight(TOP_SCROLL_PANE_HEIGHT); // Altezza minima desiderata
        scrollPane.setPrefHeight(TOP_SCROLL_PANE_HEIGHT);     // Se vuoi che parta con questa altezza preferita
        scrollPane.setMaxHeight(MAX_TOP_SCROLL_PANE_HEIGHT);  // Altezza massima consentita

        return scrollPane;
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

    /**
     * Returns the main layout for this UI.
     * This GridPane is intended to be the root of a full-screen scene.
     */
    public GridPane getLayout() {
        return mainGrid;
    }

    /**
     * Updates the UI based on client updates.
     * Recreates the drawn tiles scroll pane, ensuring its fixed size is maintained.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        Platform.runLater(() -> {
            if (drawnTilesGrid != null) {
                drawnTilesGrid.updateTilesGrid();
            }
            leftGrid.updateGridFromShipBoard();
        });
    }
}
