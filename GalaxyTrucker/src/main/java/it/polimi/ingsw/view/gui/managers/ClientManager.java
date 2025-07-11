package it.polimi.ingsw.view.gui.managers;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.gui.UIs.*;
import it.polimi.ingsw.view.gui.style.CSS;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

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

    private VBox gameLayout;

    private Consumer<ClientUpdate> refreshOnUpdate = null;

    private static final String SPACE_THEME_CSS = CSS.CLIENTMANAGER_STYLESHEET;


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

    public void updateScene(Node layout) {
        if (getLastUpdate() == null || (!CommonState.isCurrentPhase(GamePhaseType.ADVENTURE) && !CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE))){
            String cssAsDataURI = "data:text/css;base64," + Base64.getEncoder().encodeToString(SPACE_THEME_CSS.getBytes(StandardCharsets.UTF_8));
            ((Parent) layout).getStylesheets().add(cssAsDataURI);
        }
        sceneUpdater.accept(layout);
    }

    public void updateScene(INodeRefreshableOnUpdateUI refreshableNode) {
        updateScene(refreshableNode.getLayout());
        View view = gameClient.getView();
        view.unregisterOnUpdateListener(refreshOnUpdate);
        refreshOnUpdate = refreshableNode::refreshOnUpdate;
        view.registerOnUpdateListener(refreshOnUpdate);
    }

    /**
     * Retrieves the latest update from the CLI screen handler.
     *
     * @return the last ClientUpdate received
     */
    public ClientUpdate getLastUpdate() {
        return CommonState.getLastUpdate();
    }

    public GameClient getGameClient() {
        if (gameClient == null) {
            throw new IllegalStateException("Game client not initialized");
        }
        return gameClient;
    }

    /**
     * Actually process the command for the view, without the need for user to insert it manually.
     * For this reason: it MUST be a valid command for the current game state!
     * <p>
     * At the end, this functions is blocked before return to wait for the view response (refresh).
     *
     * @param command The command to execute.
     * @param args The args to pass.
     */
    public void simulateCommand(String command, String... args) {
        View view = getGameClient().getView();

        CountDownLatch latch = new CountDownLatch(1);
        Runnable onRefresh = latch::countDown;
        view.doOnceOnRefresh(onRefresh);

        view.getCommandsProcessor().processCommand(command, args);

        try {
            latch.await();  // blocks until refresh happens
            String error = getLastUpdate().popError();
            if (error != null) {
                view.showError("Server Error", error);
            }
        } catch (InterruptedException e) {
            view.showError("Interrupted Exception", e.getMessage());
        }
    }

    /**
     * Displays the login UI, allowing the user to enter their username and select connection type.
     */
    public void showLoginUI() {
        LoginUI clientLoginUI = new LoginUI();

        updateScene(clientLoginUI.getLayout());
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
     * @param host     the hostname (localhost, 192.0.0.0, ...)
     * @param port     the port of the server
     */
    public void attemptConnection(String username, boolean useRmi, String host, int port) {
        this.username = username;

        String connectionType = useRmi ? "RMI" : "Socket";
        System.out.println("Attempting login for '" + username + "' using " + connectionType);

        Task<Void> connectionTask = new Task<>() {
            @Override
            protected Void call() {
                try {

                    gameClient = GameClient.create(useRmi, host, port, true);

                    // start client on another thread
                    new Thread(() -> {
                        try {
                            GameClient.start(gameClient);
                        } catch (RemoteException e) {
                            Platform.runLater(() -> {
                                gameClient.getView().showError(e.getMessage());
                            });
                        }
                    }).start();

                    // when connection is successful, update the GUI
                    Platform.runLater(() -> createOrJoinGame(username));

                } catch (IOException | NotBoundException e) {
                    Platform.runLater(() -> AlertUtils.showError("Connection Error",
                            "No game server available on " + host + ":" + port));
                }

                return null;
            }
        };

        // execute on background thread
        new Thread(connectionTask).start();
    }

    /**
     * Presents the options to either create a new game or join an existing one.
     *
     * @param username the username of the current client
     */
    public void createOrJoinGame(String username) {
        Label headerLabel = new Label("GALAXY TRUCKER");
        headerLabel.getStyleClass().add("header-label");

        // ---- create ----
        ComboBox<MainCabinTile.Color> colorBox = new ComboBox<>(
                FXCollections.observableArrayList(MainCabinTile.Color.values()));
        colorBox.setPromptText("Ship Color");
        colorBox.setValue(MainCabinTile.Color.BLUE);
        colorBox.getStyleClass().add("combo-box");

        Button createGameButton = new Button("Establish New Convoy");
        createGameButton.setOnAction(_ -> handleCreateGame(username, colorBox.getValue()));
        createGameButton.getStyleClass().add("button");
        HBox createControls = new HBox(10, colorBox, createGameButton);
        createControls.setAlignment(Pos.CENTER);

        // ---- join ----
        Button joinButton = new Button("Join Existing Convoy");
        joinButton.setOnAction(_ -> handleJoinGame(username));
        joinButton.getStyleClass().add("button");

        // ---- resume ----
        javafx.scene.control.TextField uuidField = new javafx.scene.control.TextField();
        uuidField.setPromptText("Enter Convoy UUID");
        uuidField.getStyleClass().add("text-field");
        uuidField.setMaxWidth(200);

        Button resumeButton = new Button("Resume Mission");
        resumeButton.setOnAction(_ -> {
            String uuidText = uuidField.getText();
            try {
                UUID gameId = UUID.fromString(uuidText);
                handleResumeGame(gameId);
            } catch (IllegalArgumentException e) {
                AlertUtils.showError("Invalid UUID", "Please enter a valid Convoy UUID.");
            }
        });
        resumeButton.getStyleClass().add("button");

        HBox resumeControls = new HBox(10, uuidField, resumeButton);
        resumeControls.setAlignment(Pos.CENTER);

        // ---- layout ----
        gameLayout = new VBox(20, headerLabel, createControls, joinButton, resumeControls);
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.getStyleClass().add("root");
        VBox.setVgrow(gameLayout, Priority.ALWAYS); // <--- AGGIUNGI QUESTA LINEA

        updateScene(gameLayout);
        primaryStage.setTitle("Galaxy Trucker - Mission Control");
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


    private void handleResumeGame(UUID gameId){
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("resume", gameId.toString());
            try{
                if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE))
                    ClientManager.getInstance().updateScene(AssembleUI.getInstance());
                else
                    ClientManager.getInstance().updateScene(AdventureUI.getInstance());
            } catch (Exception e) {}
        });

    }

    /**
     * Sends a request to the server to create a new game and transitions to the lobby UI.
     *
     * @param username the username of the client creating the game
     */
    public void handleCreateGame(String username, MainCabinTile.Color color) {
        simulateCommand("create", username, "--color", color.toString());
        updateScene(new LobbyUI());
    }

    /**
     * Retrieves the list of active games from the server and displays the join game UI.
     *
     * @param username the username of the client attempting to join
     */
    public void handleJoinGame(String username) {
        updateScene(new JoinGameUI(username));
    }

    public String getUsername() {
        return username;
    }

    public VBox getGameLayout() {
        return gameLayout;
    }
}