package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.CoveredTilesPane;
import it.polimi.ingsw.view.gui.components.DraggableTile;
import it.polimi.ingsw.view.gui.components.DrawnTilesGrid;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import it.polimi.ingsw.view.gui.helpers.WhichPane;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;


/**
 * UI component for the ship assembly phase of the game.
 */
public class AssembleUI implements INodeRefreshableOnUpdateUI {

    private final GridPane mainGrid;
    private ScrollPane topScrollPane;
    private final GridPane leftGrid;
    private static Pane rightPane;

    public static DraggableTile isBeeingDragged;
    public static void setIsBeeingDragged(DraggableTile isBeeingDragged) {
        AssembleUI.isBeeingDragged = isBeeingDragged;
    }
    /**
     * Creates a new assembly UI with all required components.
     */
    public AssembleUI() {
        mainGrid = new GridPane();

        topScrollPane = createDrawnTilesScrollPane();
        leftGrid = createShipGrid();
        rightPane = createCoveredTilesPane();

        // Setup layout
        mainGrid.add(topScrollPane, 0, 0, 2, 1);
        mainGrid.add(leftGrid, 0, 1);
        mainGrid.add(rightPane, 1, 1);

        configureGridBehavior();
    }

    /**
     * Configures grid components to resize properly.
     */
    private void configureGridBehavior() {
        for (Node node : new Node[]{topScrollPane, leftGrid, rightPane}) {
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setVgrow(node, Priority.ALWAYS);
        }
    }

    /**
     * Creates the scrollable pane for displaying drawn tiles.
     */
    private ScrollPane createDrawnTilesScrollPane() {
        DrawnTilesGrid drawnTilesGrid = new DrawnTilesGrid();
        ScrollPane scrollPane = new ScrollPane(drawnTilesGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    /**
     * Creates the ship grid with dimensions based on game level.
     */
    private GridPane createShipGrid() {
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
        };

        return new ShipGrid(rows, cols);
    }

    /**
     * Creates the pane containing covered tiles.
     */
    private Pane createCoveredTilesPane() {
        rightPane = new CoveredTilesPane();
        return rightPane;
    }

    /**
     * Returns the main layout for this UI.
     */
    public GridPane getLayout() {
        return mainGrid;
    }

    /**
     * Updates the UI based on client updates.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        // Update the drawn tiles grid with fresh data
        Platform.runLater(() -> {
            ScrollPane newTopScrollPane = createDrawnTilesScrollPane();
            mainGrid.getChildren().remove(topScrollPane);
            topScrollPane = newTopScrollPane;
            mainGrid.add(topScrollPane, 0, 0, 2, 1);
            GridPane.setHgrow(topScrollPane, Priority.ALWAYS);
            GridPane.setVgrow(topScrollPane, Priority.ALWAYS);

            ((ShipGrid) leftGrid).updateGridFromShipBoard();
        });

    }
}
