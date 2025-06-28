package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.shipboard.integrity.IntegrityProblem;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * Manages the user interface for the adventure phase of the game.
 * This class assembles and displays various components like the ship grid, game board,
 * adventure cards, and handles player input requests (PIRs).
 * It follows a singleton pattern.
 */
public class AdventureUI implements INodeRefreshableOnUpdateUI {

    private final double WIDTH = 400;
    private final double HEIGHT = 400;

    private PIR lastPIR;

    private GridPane mainLayout;
    private BoardComponent boardComponent;
    private ShipGrid shipGrid;
    private CardContainer cardPane;
    private LoadableContainer loadableContainer;
    private PIRContainer pirContainer = new PIRContainer();
    private PirDelayContainer pirDelayContainer = new PirDelayContainer();
    private Button integrityButton;

    private Rectangle overlayBackground;

    private static Pane dragOverlay;
    /**
     * Gets the overlay pane used for drag-and-drop operations.
     * This pane sits on top of all other components to render dragged items.
     * @return The singleton drag overlay pane.
     */
    public static Pane getDragOverlay() {
        if (dragOverlay == null) {
            dragOverlay = new Pane(); // Transparent pane above everything
            dragOverlay.setPickOnBounds(false); // Important: so it doesn't block mouse events
            dragOverlay.setViewOrder(-1);
        }
        return dragOverlay;
    }

    private static StackPane root;
    /**
     * Gets the root container for the entire Adventure UI.
     * @return The singleton root StackPane.
     */
    public static StackPane getRoot() {
        if (root == null) {
            root = new StackPane();
        }
        return root;
    }

    private static AdventureUI instance;
    /**
     * Gets the singleton instance of the AdventureUI.
     * @return The single instance of this class.
     */
    public static AdventureUI getInstance() {
        if (instance == null) {
            instance = new AdventureUI();
        }
        return instance;
    }

    /**
     * Constructs the AdventureUI, initializing all its components and layout.
     */
    public AdventureUI() {
        root = new StackPane();

        mainLayout = new GridPane();

        pirContainer.setMaxSize(WIDTH, HEIGHT);
        pirContainer.setMinSize(WIDTH, HEIGHT);
        pirContainer.setAlignment(Pos.CENTER);

        GameLevel gameLevel = LobbyState.getGameLevel();
        if (gameLevel == null) {
            System.err.println("AdventureUI: GameLevel is null, defaulting to TESTFLIGHT for component creation. This might indicate an issue.");
            gameLevel = GameLevel.TESTFLIGHT;
        }

        shipGrid = new ShipGrid(gameLevel);

        cardPane = new CardContainer();
        updateCard();

        // Create loadable container
        loadableContainer = new LoadableContainer();

        // Add components to the grid
        mainLayout.add(loadableContainer, 0, 0);
        mainLayout.add(pirDelayContainer, 1, 0);
        mainLayout.add(shipGrid, 0, 2);
        mainLayout.add(cardPane, 1, 2);
        mainLayout.setAlignment(Pos.CENTER);


        root.getChildren().addAll(mainLayout, getDragOverlay());
    }

    /**
     * Provides access to the ship grid component.
     * @return The {@link ShipGrid}.
     */
    public ShipGrid getShipGrid(){
        return shipGrid;
    }


    public CardContainer getCardPane(){
        return cardPane;
    }

    public PIRContainer getPirContainer(){
        return pirContainer;
    }

    /**
     * Updates the displayed adventure card based on the current game state.
     */
    public void updateCard() {
        cardPane.setCard(CommonState.getLastUpdate().getCurrentGame().getDeck().getCurrentCard());
    }

    public void removeIntegrityButton(){
        if (this.getCardPane().getChildren().contains(integrityButton)) this.getCardPane().getChildren().remove(integrityButton);
    }
    /**
     * Provides access to the loadable container.
     * @return The {@link LoadableContainer}.
     */
    public LoadableContainer getLoadableContainer() {
        return loadableContainer;
    }

    /**
     * Displays the Player Input Request (PIR) container with a semi-transparent overlay.
     * This effectively creates a modal dialog for player interaction.
     */
    public void showPirContainer() {
        // Remove any existing instances first to avoid duplicates
        hidePirContainer();

        overlayBackground = new Rectangle();
        overlayBackground.setFill(Color.rgb(0, 0, 0, 0.6));
        overlayBackground.widthProperty().bind(getRoot().widthProperty());
        overlayBackground.heightProperty().bind(getRoot().heightProperty());

        // Center the PIR container both horizontally and vertically
        StackPane.setAlignment(pirContainer, Pos.CENTER);

        // Add elements in the correct order (background first, then PIR container)
        root.getChildren().add(overlayBackground);
        root.getChildren().add(pirContainer);
    }


    /**
     * Hides the Player Input Request (PIR) container and its overlay.
     */
    public void hidePirContainer() {
        getRoot().getChildren().remove(overlayBackground);
        getRoot().getChildren().remove(pirContainer);
    }

    public void addIntegrityButton() {
        integrityButton = new Button("Confirm Integrity Choice");
        integrityButton.setOnMouseClicked(event -> {
            getShipGrid().confirmIntegrityProblemChoice();
            integrityButton.setVisible(false);
        });
        getCardPane().getChildren().add(integrityButton);
    }

    public PirDelayContainer getPirDelayContainer() {
        return pirDelayContainer;
    }

    /**
     * Returns the root node of the UI layout.
     * @return The root {@link Node} for this UI.
     */
    @Override
    public Node getLayout() {
        return root;
    }

    /**
     * Handles cleanup of any previously handled PIR.
     * <p>
     * This prevents the same PIR from being re-processed or conflicting with new updates.
     *
     * @param lastPIR the most recently received PIR to inspect. can be null.
     */
    private void dropManuallyHandledPIRs(PIR lastPIR) {
        if (lastPIR == null
                || !lastPIR.containsTag(IntegrityProblem.TAG)
                || (lastPIR.getPIRType() != PIRType.CHOICE)) {
            shipGrid.dropIntegrityProblemChoice();
        }
    }

    /**
     * @return {@code true} if the pir has already been handled with this function thanks to its tags,
     * {@code false} otherwise (it needs propagation to pirContainer)
     */
    private boolean handleTaggedPIR(PIR pir) {
        if (pir.containsTag(IntegrityProblem.TAG)) {
            // validate multiple choice
            if (pir.getPIRType() != PIRType.CHOICE) {
                return false;
            }
            // handle integrity problem
            shipGrid.handleIntegrityProblemChoice();
            return true;
        }

        return false;
    }

    /**
     * Refreshes the UI components based on a new client update from the server.
     * This method is called whenever the game state changes. It updates the adventure card
     * and shows or hides the PIR container as needed.
     * @param update The {@link ClientUpdate} containing the new game state.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        Platform.runLater(() -> {
            // Aggiorna la carta
            updateCard();
            shipGrid.update();
            // Gestisci il PIR
            PIR newPir = PIRState.getActivePIR();
            dropManuallyHandledPIRs(newPir);
            if (newPir != null) {
                if (lastPIR == null || !lastPIR.getId().equals(newPir.getId())) {
                    lastPIR = newPir;
                    try {
                        System.out.println("Setting pir to " + lastPIR.getPIRType());
                        if (!handleTaggedPIR(lastPIR)) {
                            pirContainer.setPir(lastPIR);
                            if (!root.getChildren().contains(pirContainer)
                                    && !newPir.getPIRType().equals(PIRType.DELAY)
                                    && !newPir.getPIRType().equals(PIRType.ADD_CARGO)
                            ) {
                                showPirContainer();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}