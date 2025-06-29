package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.model.shipboard.integrity.IntegrityProblem;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.components.*;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


import static it.polimi.ingsw.view.gui.components.PIRContainer.formatCoordinates;


/**
 * Manages the user interface for the adventure phase of the game.
 * This class assembles and displays various components like the ship grid, game board,
 * adventure cards, and handles player input requests (PIRs).
 * It follows a singleton pattern.
 */
public class AdventureUI implements INodeRefreshableOnUpdateUI {

    /**
     * Defines the different views available within the AdventureUI,
     * such as the player's personal board or the shared game board.
     */
    public enum AdventurePane {
        PLAYER_BOARD, SHARED_BOARD
    }

    /**
     * Defines the default width for certain UI components.
     */
    private final double WIDTH = 400;
    /**
     * Defines the default height for certain UI components.
     */
    private final double HEIGHT = 400;

    /**
     * Stores the last Player Input Request received to avoid reprocessing.
     */
    private PIR lastPIR;

    /**
     * The main grid layout for the player-specific view.
     */
    private GridPane mainLayout;
    /**
     * The component that displays the shared game board.
     */
    private BoardComponent boardComponent;
    /**
     * The component displaying the player's ship grid.
     */
    private ShipGrid shipGrid;
    /**
     * The container for the current adventure card.
     */
    private CardContainer cardPane;
    /**
     * The container for displaying player's loadable items (cargo, crew, etc.).
     */
    private LoadableContainer loadableContainer;
    /**
     * The container for handling generic Player Input Requests.
     */
    private PIRContainer pirContainer = new PIRContainer();
    /**
     * The container specifically for handling PIRs of type DELAY.
     */
    private PirDelayContainer pirDelayContainer = new PirDelayContainer();
    /**
     * Button to confirm choices related to ship integrity problems.
     */
    private Button integrityButton;
    /**
     * Button to confirm the activation of ship components.
     */
    private Button confirmActivationButton;
    /**
     * Flag indicating if the current user is spectating the game.
     */
    private boolean isSpectating = false;

    /**
     * A semi-transparent background used as an overlay for modal dialogs like the PIR container.
     */
    private Rectangle overlayBackground;

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
            dragOverlay.setViewOrder(-1);
        }
        return dragOverlay;
    }

    private static StackPane root;
    /**
     * Returns the root StackPane container for the entire Adventure UI.
     * @return The root pane.
     */
    public static StackPane getRoot() {
        if (root == null) {
            root = new StackPane();
        }
        return root;
    }

    private static AdventureUI instance;
    /**
     * Returns the singleton instance of the AdventureUI.
     * @return The single instance of this class.
     */
    public static AdventureUI getInstance() {
        if (instance == null) {
            instance = new AdventureUI();
        }
        return instance;
    }

    public static void reset(){
        instance = null;
    }

    /**
     * Constructs the AdventureUI, initializing all its components and layout.
     */
    public AdventureUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #1a1c2c; -fx-background: #1a1c2c;");
        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        mainLayout = new GridPane();
        mainLayout.setStyle("-fx-background-color: #1a1c2c;");
        mainLayout.setHgap(10);
        mainLayout.setVgap(10);
        mainLayout.setPadding(new Insets(15));

        pirContainer.setMaxSize(WIDTH, HEIGHT);
        pirContainer.setMinSize(WIDTH, HEIGHT);
        pirContainer.setAlignment(Pos.CENTER);
        pirContainer.setStyle("-fx-background-color: rgba(26, 28, 44, 0.9); -fx-border-color: #4dd0e1; -fx-border-width: 1; -fx-padding: 10;");

        GameLevel gameLevel = LobbyState.getGameLevel();
        if (gameLevel == null) {
            System.err.println("AdventureUI: GameLevel is null, defaulting to TESTFLIGHT for component creation. This might indicate an issue.");
            gameLevel = GameLevel.TESTFLIGHT;
        }

        shipGrid = new ShipGrid(gameLevel);
        shipGrid.setStyle("-fx-border-color: #4dd0e1; -fx-border-width: 1;");

        cardPane = new CardContainer();
        cardPane.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; -fx-border-width: 1; -fx-padding: 10;");
        updateCard();

        // Create loadable container
        loadableContainer = new LoadableContainer();
        loadableContainer.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; -fx-border-width: 1; -fx-padding: 10;");

        // Create the top HBox
        HBox topBar = new HBox(10);
        topBar.setPrefHeight(100);
        topBar.setAlignment(Pos.CENTER);
        topBar.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; -fx-border-width: 1;");

        SpectateVBox spectateVBox = new SpectateVBox();
        Button boardButton = getBoardButton();
        styleButton(boardButton);
        
        topBar.getChildren().add(spectateVBox);
        topBar.getChildren().add(boardButton);

        boardComponent = BoardComponent.create(LobbyState.getGameLevel());
        boardComponent.setStyle("-fx-border-color: #4dd0e1; -fx-border-width: 1; -fx-background-color: #1a1c2c;");
        root.getChildren().add(boardComponent);

        pirDelayContainer.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; -fx-border-width: 1; -fx-padding: 10;");

        // Add components to the grid
        mainLayout.add(topBar, 0, 0, 2, 1); // Add topBar at row 0, spanning 2 columns
        mainLayout.add(loadableContainer, 0, 1);
        mainLayout.add(pirDelayContainer, 1, 1);
        mainLayout.add(shipGrid, 0, 2);
        mainLayout.add(cardPane, 1, 2);
        mainLayout.setAlignment(Pos.CENTER);

        root.getChildren().addAll(mainLayout, getDragOverlay());
        setAdventureLayout(AdventurePane.PLAYER_BOARD);
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
     * Checks if the current user is in spectating mode.
     * @return {@code true} if spectating, {@code false} otherwise.
     */
    public boolean isSpectating() {
        return isSpectating;
    }

    /**
     * Sets the spectating mode for the UI.
     * @param isSpectating {@code true} to enable spectating mode, {@code false} to disable it.
     */
    public void setSpectating(boolean isSpectating) {
        this.isSpectating = isSpectating;
    }

    /**
     * Transitions the UI to the score screen when the adventure phase is finished.
     */
    public void setFinished(){
        Platform.runLater(() -> {
            ClientManager.getInstance().updateScene(new ScoreUI());
        });
    }

    /**
     * Creates and returns a button to switch to the shared board view.
     * @return A configured {@link Button}.
     */
    private Button getBoardButton() {
        Button boardButton = new Button("Board");
        boardButton.setOnMouseClicked(event -> {
            setAdventureLayout(AdventureUI.AdventurePane.SHARED_BOARD);
            boardComponent.addPlayers();
        });
        return boardButton;
    }

    /**
     * Switches the visible layout between the player's board and the shared game board.
     * @param paneToShow The pane to display.
     */
    public void setAdventureLayout(AdventureUI.AdventurePane paneToShow){
        mainLayout.setVisible(paneToShow == AdventureUI.AdventurePane.PLAYER_BOARD);
        boardComponent.setVisible(paneToShow == AdventureUI.AdventurePane.SHARED_BOARD);
    }

    /**
     * Adds and displays the 'Confirm Activation' button on the card pane.
     * This button is used to finalize component activation choices.
     */
    public void addConfirmButton() {
        if (confirmActivationButton == null){
            confirmActivationButton = new Button("Confirm Activation");
            styleButton(confirmActivationButton);
            confirmActivationButton.setOnMouseClicked(e -> {
                Platform.runLater(() -> {
                    if (!shipGrid.getCellsToActivate().isEmpty()) {
                        ClientManager.getInstance().simulateCommand("activate", formatCoordinates(shipGrid.getCellsToActivate()));
                        shipGrid.getCellsToActivate().clear();
                        removeConfirmButton();
                    }
                });
            });
            AdventureUI.getInstance().getCardPane().getChildren().add(confirmActivationButton);
        }
        confirmActivationButton.setVisible(true);
    }

    /**
     * Hides the 'Confirm Activation' button.
     */
    public void removeConfirmButton() {
        if (confirmActivationButton!=null)
            confirmActivationButton.setVisible(false);
    }

    /**
     * Returns the ship grid component.
     * @return The {@link ShipGrid}.
     */
    public ShipGrid getShipGrid(){
        return shipGrid;
    }

    /**
     * Returns the container for the adventure card.
     * @return The {@link CardContainer}.
     */
    public CardContainer getCardPane(){
        return cardPane;
    }

    /**
     * Returns the container for Player Input Requests.
     * @return The {@link PIRContainer}.
     */
    public PIRContainer getPirContainer(){
        return pirContainer;
    }

    /**
     * Updates the displayed adventure card based on the current game state.
     */
    public void updateCard() {
        cardPane.setCard(CommonState.getLastUpdate().getCurrentGame().getDeck().getCurrentCard());
    }

    /**
     * Removes the integrity confirmation button from the card pane if it exists.
     */
    public void removeIntegrityButton(){
        if (this.getCardPane().getChildren().contains(integrityButton)) this.getCardPane().getChildren().remove(integrityButton);
    }
    /**
     * Returns the container for loadable items.
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

        // Style the PIR container for better visibility over the overlay
        pirContainer.setStyle("-fx-background-color: rgba(26, 28, 44, 0.95); -fx-border-color: #4dd0e1; " +
                         "-fx-border-width: 2; -fx-padding: 15; -fx-effect: dropshadow(gaussian, #4dd0e1, 5, 0, 0, 0);");

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

    /**
     * Adds the 'Confirm Integrity Choice' button to the card pane.
     * This button allows the player to confirm their selection for an integrity problem.
     */
    public void addIntegrityButton() {
        integrityButton = new Button("Confirm Integrity Choice");
        styleButton(integrityButton);
        integrityButton.setOnMouseClicked(event -> {
            getShipGrid().confirmIntegrityProblemChoice();
            integrityButton.setVisible(false);
        });
        getCardPane().getChildren().add(integrityButton);
    }

    /**
     * Returns the container for handling DELAY type PIRs.
     * @return The {@link PirDelayContainer}.
     */
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
     * Cleans up choices related to manually handled PIRs, such as integrity problems,
     * to prevent state conflicts on new updates.
     * @param lastPIR The most recently received PIR to inspect. Can be null.
     */
    private void dropManuallyHandledPIRs(PIR lastPIR) {
        if (lastPIR == null
                || !lastPIR.containsTag(IntegrityProblem.TAG)
                || (lastPIR.getPIRType() != PIRType.CHOICE)) {
            shipGrid.dropIntegrityProblemChoice();
        }
    }

    /**
     * Handles specific PIRs based on their tags before they are sent to the generic PIR container.
     * For example, it directs integrity-related PIRs to the ShipGrid.
     * @param pir The PIR to handle.
     * @return {@code true} if the PIR was handled by this method, {@code false} if it needs to be propagated to the generic PIR container.
     */
    private boolean handleTaggedPIR(PIR pir) {
        if (pir.containsTag(IntegrityProblem.TAG)) {
            // validate multiple choice
            if (pir.getPIRType() != PIRType.CHOICE) {
                return false;
            }
            // handle integrity problem
            shipGrid.handleIntegrityProblemChoice(((PIRMultipleChoice) pir).getPossibleOptions());
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
            cardPane.setEndTurnButtonVisible(newPir != null);
            if (newPir != null) {
                if (lastPIR == null || !lastPIR.getId().equals(newPir.getId())) {
                    lastPIR = newPir;
                    removeConfirmButton();
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