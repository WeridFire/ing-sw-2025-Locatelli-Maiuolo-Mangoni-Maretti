package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.MenuState;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User interface for joining an existing game.
 * Allows users to enter a game UUID manually or select from a list of active games.
 * Provides color selection for the player's main cabin and validates availability.
 * Implements INodeRefreshableOnUpdateUI to update the list of active games and available colors.
 */
public class JoinGameUI implements INodeRefreshableOnUpdateUI {
    /**
     * The main layout container for all UI components.
     */
    private final VBox layout;
    /**
     * Text field for manually entering a game UUID.
     */
    private final TextField gameUUIDField;
    /**
     * List view displaying currently active games.
     */
    private final ListView<String> activeGamesList;

    /**
     * Toggle group for managing color selection buttons.
     */
    private final ToggleGroup colorToggleGroup = new ToggleGroup();

    /**
     * Constructs the join game UI with all necessary components.
     * @param username The username of the player attempting to join.
     */
    public JoinGameUI(String username) {
        Label uuidLabel = new Label("Enter game UUID:");
        uuidLabel.getStyleClass().add("label");

        gameUUIDField = new TextField();
        gameUUIDField.setPromptText("Game UUID");
        gameUUIDField.getStyleClass().add("text-field");
        gameUUIDField.textProperty().addListener((obs, oldText, newText) -> {
            refreshColorsAvailability(getAvailableColors());
        });

        Label activeGamesLabel = new Label("Active Games:");
        activeGamesLabel.getStyleClass().add("label");

        activeGamesList = new ListView<>(FXCollections.observableArrayList(MenuState.getActiveGamesUUID()));
        activeGamesList.setMaxHeight(150);
        activeGamesList.getStyleClass().add("list-view");
        activeGamesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                gameUUIDField.setText(newVal);
            }
        });

        Label colorLabel = new Label("Choose your color:");
        colorLabel.getStyleClass().add("label");

        HBox colorSelectionBox = new HBox(10);
        colorSelectionBox.setAlignment(Pos.CENTER);
        // create colors list
        for (MainCabinTile.Color color : MainCabinTile.Color.values()) {
            String displayName = getColorDisplayName(color);
            ToggleButton toggleButton = new ToggleButton(displayName);
            toggleButton.setToggleGroup(colorToggleGroup);
            toggleButton.setUserData(color);
            toggleButton.getStyleClass().add("toggle-button");
            colorSelectionBox.getChildren().add(toggleButton);
        }
        // select first color as default
        if (!colorToggleGroup.getToggles().isEmpty()) {
            colorToggleGroup.selectToggle(colorToggleGroup.getToggles().getFirst());
        }

        Button joinGameButton = createJoinGameButton(username);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(event -> {
            ClientManager.getInstance().updateScene(ClientManager.getInstance().getGameLayout());
        });

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("root");
        VBox.setVgrow(layout, Priority.ALWAYS);
        layout.getChildren().addAll(
                uuidLabel, gameUUIDField,
                activeGamesLabel, activeGamesList,
                colorLabel, colorSelectionBox,
                joinGameButton, backButton
        );
    }

    /**
     * Converts a color enum value to a user-friendly display name.
     * @param color The color enum value.
     * @return A capitalized string representation of the color.
     */
    private String getColorDisplayName(MainCabinTile.Color color) {
        String name = color.toString().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Creates and configures the join game button with appropriate event handling.
     * @param username The username of the player attempting to join.
     * @return A configured Button for joining the game.
     */
    private Button createJoinGameButton(String username) {
        Button joinGameButton = new Button("Join Game");
        joinGameButton.getStyleClass().add("button");
        joinGameButton.setOnAction(_ -> {
            String uuid = gameUUIDField.getText().trim();
            if (!uuid.isEmpty()) {
                ClientManager clientManager = ClientManager.getInstance();

                try {
                    String desiredColor = colorToggleGroup.getSelectedToggle().getUserData().toString();
                    clientManager.simulateCommand("join", uuid, username,
                            "--color", desiredColor);
                } catch (NullPointerException e) {
                    clientManager.simulateCommand("join", uuid, username);
                }

                clientManager.updateScene(new LobbyUI());

            } else {
                AlertUtils.showWarning("Empty UUID", "Please enter a valid game UUID.");
            }
        });
        return joinGameButton;
    }

    /**
     * Updates the list of active games displayed in the ListView.
     * @param activeGames The updated list of active game UUIDs.
     */
    private void refreshActiveGamesList(List<String> activeGames) {
        activeGamesList.getItems().setAll(activeGames);
    }

    /**
     * Updates the availability of color selection buttons based on the current game.
     * Disables colors that are already taken by other players.
     * @param availableColors Set of colors that are still available for selection.
     */
    private void refreshColorsAvailability(Set<MainCabinTile.Color> availableColors) {
        Toggle selectedToggle = colorToggleGroup.getSelectedToggle();

        // keep enabled only the available colors
        for (Toggle toggle : colorToggleGroup.getToggles()) {
            MainCabinTile.Color color = (MainCabinTile.Color) toggle.getUserData();
            boolean isAvailable = availableColors.contains(color);
            ((Node) toggle).setDisable(!isAvailable);
        }

        // if previously selected toggle is now disabled: change selection (if available)
        if (selectedToggle != null && ((Node) selectedToggle).isDisable()) {
            for (Toggle toggle : colorToggleGroup.getToggles()) {
                if (!((Node) toggle).isDisable()) {
                    colorToggleGroup.selectToggle(toggle);
                    break;
                }
            }
        }
    }

    /**
     * Retrieves the set of available colors for the currently entered game UUID.
     * @return Set of colors available for selection, or all colors if UUID is invalid.
     */
    private Set<MainCabinTile.Color> getAvailableColors() {
        MainCabinTile.Color[] availableColors;
        try {
            UUID uuid = UUID.fromString(gameUUIDField.getText());
            availableColors = MenuState.getAvailableColorsForGame(uuid);
        } catch (IllegalArgumentException e) {
            // invalid uuid -> all colors "available" for no game
            availableColors = MainCabinTile.Color.values();
        }
        return new HashSet<>(List.of(availableColors));
    }

    /**
     * Returns the main layout container for this UI.
     * @return The VBox containing all UI components.
     */
    public VBox getLayout() {
        return layout;
    }

    /**
     * Refreshes the UI components based on a new client update from the server.
     * Updates the list of active games and refreshes color availability.
     * @param update The ClientUpdate containing the new game state.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        Platform.runLater(() -> {
            refreshActiveGamesList(MenuState.getActiveGamesUUID());
            refreshColorsAvailability(getAvailableColors());
        });
    }
}