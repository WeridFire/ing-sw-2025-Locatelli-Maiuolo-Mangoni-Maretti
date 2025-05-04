package it.polimi.ingsw.view.gui.managers;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.cli.CLIScreenHandler;
import it.polimi.ingsw.view.gui.UIs.JoinGameUI;
import it.polimi.ingsw.view.gui.UIs.LobbyUI;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import it.polimi.ingsw.view.gui.UIs.LoginUI;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages the client-side GUI flow, including login, game creation, and game joining.
 */
public class ClientManager {

    private static ClientManager instance;

    private final Stage primaryStage;
    private final Consumer<Node> sceneUpdater;
    private final Runnable showLauncherCallback;

    private GameClient gameClient;
    private String username;


    public static ClientManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Client Manager not initialized");
        }
        return instance;
    }

    /**
     * Constructs a ClientManager with the specified primary stage, scene updater, and launcher callback.
     *
     * @param primaryStage        the JavaFX Stage to display scenes
     * @param sceneUpdater        a Consumer to update the current scene with a new Node
     * @param showLauncherCallback a Runnable to execute when returning to the launcher
     */
    public ClientManager(Stage primaryStage, Consumer<Node> sceneUpdater, Runnable showLauncherCallback) {
        this.primaryStage = primaryStage;
        this.sceneUpdater = sceneUpdater;
        this.showLauncherCallback = showLauncherCallback;

        instance = this;
    }

    /**
     * Retrieves the latest update from the CLI screen handler.
     *
     * @return the last ClientUpdate received
     */
    public ClientUpdate getLastUpdate() {
        return CLIScreenHandler.getInstance().getLastUpdate();
    }

    public Consumer<Node> getSceneUpdater() {
        return sceneUpdater;
    }

    /**
     * Displays the login UI, allowing the user to enter their username and select connection type.
     */
    public void showLoginUI() {
        LoginUI clientLoginUI = new LoginUI((username, useRmi) -> {
            if (!username.trim().isEmpty()) {
                String connectionType = useRmi ? "RMI" : "Socket";
                System.out.println("Attempting login for '" + username + "' using " + connectionType);
                attemptConnection(username, useRmi);
            }
        });

        sceneUpdater.accept(clientLoginUI.getLayout());
        primaryStage.setTitle("Client Login");
        primaryStage.setOnCloseRequest(event -> {
            showLauncherCallback.run();
            primaryStage.setTitle("Game Launcher");
            primaryStage.setOnCloseRequest(e -> {
                System.out.println("Launcher closed.");
                Platform.exit();
                System.exit(0);
            });
            event.consume();
        });
    }

    /**
     * Attempts to connect to the server with the given username and connection type.
     *
     * @param username the username provided by the user
     * @param useRmi   true to use RMI, false to use sockets
     */
    private void attemptConnection(String username, boolean useRmi) {
        this.username = username;
        String HOST = "localhost";
        int SOCKET_PORT = 1234;
        int RMI_PORT = 1111;

        try {
            gameClient = new GameClient(useRmi,
                    HOST,
                    useRmi ? RMI_PORT : SOCKET_PORT
            );

            new Thread(() -> {
                try {
                    GameClient.main(gameClient);
                } catch (IOException | NotBoundException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        } catch (Exception e) {
            AlertUtils.showError("Connection Error", "No game server available on " + HOST + ":" + (useRmi ? RMI_PORT : SOCKET_PORT));
            return;
        }

        Platform.runLater(() -> createOrJoinGame(username));
    }

    /**
     * Presents the options to either create a new game or join an existing one.
     *
     * @param username the username of the current client
     */
    private void createOrJoinGame(String username) {

        Button createButton = new Button("Create Game");
        createButton.setOnAction(_ -> handleCreateGame(username));

        Button joinButton = new Button("Join Game");
        joinButton.setOnAction(_ -> handleJoinGame(username));

        VBox gameLayout = new VBox(15, createButton, joinButton);
        gameLayout.setAlignment(Pos.CENTER);
        sceneUpdater.accept(gameLayout);
        primaryStage.setTitle("Join or Create Game");
        primaryStage.setOnCloseRequest(event -> {
            showLauncherCallback.run();
            primaryStage.setTitle("Join or Create Game");
            primaryStage.setOnCloseRequest(e -> {
                System.out.println("Launcher closed.");
                Platform.exit();
                System.exit(0);
            });
            event.consume();
        });
    }

    /**
     * Sends a request to the server to create a new game and transitions to the lobby UI.
     *
     * @param username the username of the client creating the game
     */
    public void handleCreateGame(String username) {
        try {
            gameClient.getClient().getServer().createGame(gameClient.getClient(), username);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        sceneUpdater.accept(new LobbyUI(username).getLayout());
    }

    /**
     * Retrieves the list of active games from the server and displays the join game UI.
     *
     * @param username the username of the client attempting to join
     */
    public void handleJoinGame(String username) {
        try {
            gameClient.getClient().getServer().ping(gameClient.getClient());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        List<UUID> activeGames =
                getLastUpdate().getAvailableGames().stream().map(GameData::getGameId).toList();

        System.out.println(activeGames);

        JoinGameUI joinGameUI = new JoinGameUI(uuid -> {
            System.out.println("Trying to join game with UUID: " + uuid);
            try {
                gameClient.getClient().getServer().joinGame(gameClient.getClient(), UUID.fromString(uuid), username);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        },
                activeGames.stream().map(
                        UUID::toString
                ).collect(Collectors.toList())
        );

        sceneUpdater.accept(joinGameUI.getLayout());
    }


    public String getUsername() {
        return username;
    }
}