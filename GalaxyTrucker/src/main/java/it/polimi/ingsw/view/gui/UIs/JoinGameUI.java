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
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JoinGameUI implements INodeRefreshableOnUpdateUI {
    private final VBox layout;
    private final TextField gameUUIDField;
    private final ListView<String> activeGamesList;

    private final ToggleGroup colorToggleGroup = new ToggleGroup();

    public JoinGameUI(String username) {
        Label uuidLabel = new Label("Enter game UUID:");
        gameUUIDField = new TextField();
        gameUUIDField.setPromptText("Game UUID");
        gameUUIDField.textProperty().addListener((obs, oldText, newText) -> {
            refreshColorsAvailability(getAvailableColors());
        });

        Label activeGamesLabel = new Label("Active Games:");
        activeGamesList = new ListView<>(FXCollections.observableArrayList(MenuState.getActiveGamesUUID()));
        activeGamesList.setMaxHeight(150);
        activeGamesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                gameUUIDField.setText(newVal);
            }
        });

        Label colorLabel = new Label("Choose your color:");
        HBox colorSelectionBox = new HBox(10);
        colorSelectionBox.setAlignment(Pos.CENTER);
        // create colors list
        for (MainCabinTile.Color color : MainCabinTile.Color.values()) {
            String displayName = getColorDisplayName(color);
            ToggleButton toggleButton = new ToggleButton(displayName);
            toggleButton.setToggleGroup(colorToggleGroup);
            toggleButton.setUserData(color);
            colorSelectionBox.getChildren().add(toggleButton);
        }
        // select first color as default
        if (!colorToggleGroup.getToggles().isEmpty()) {
            colorToggleGroup.selectToggle(colorToggleGroup.getToggles().getFirst());
        }

        Button joinGameButton = createJoinGameButton(username);

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            ClientManager.getInstance().updateScene(ClientManager.getInstance().getGameLayout());
        });

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                uuidLabel, gameUUIDField,
                activeGamesLabel, activeGamesList,
                colorLabel, colorSelectionBox,
                joinGameButton, backButton
        );
    }

    private String getColorDisplayName(MainCabinTile.Color color) {
        String name = color.toString().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private Button createJoinGameButton(String username) {
        Button joinGameButton = new Button("Join Game");
        joinGameButton.setOnAction(e -> {
            String uuid = gameUUIDField.getText().trim();
            if (!uuid.isEmpty()) {
                ClientManager clientManager = ClientManager.getInstance();

                clientManager.simulateCommand("join", uuid, username,
                        "--color", colorToggleGroup.getSelectedToggle().getUserData().toString());
                clientManager.updateScene(new LobbyUI(clientManager.getUsername()));

            } else {
                AlertUtils.showWarning("Empty UUID", "Please enter a valid game UUID.");
            }
        });
        return joinGameButton;
    }

    private void refreshActiveGamesList(List<String> activeGames) {
        activeGamesList.getItems().setAll(activeGames);
    }

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

    public VBox getLayout() {
        return layout;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        Platform.runLater(() -> {
            refreshActiveGamesList(MenuState.getActiveGamesUUID());
            refreshColorsAvailability(getAvailableColors());
        });
    }
}