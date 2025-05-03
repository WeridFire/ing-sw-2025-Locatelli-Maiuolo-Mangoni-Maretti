package it.polimi.ingsw.view.gui.managers;

import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.view.gui.UIs.LobbyUI;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import it.polimi.ingsw.view.gui.UIs.LoginUI;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ClientManager {

    private final Stage primaryStage;
    private final Consumer<Node> sceneUpdater;
    private final Runnable showLauncherCallback;

    private GameClient client;
    private String username;

    public ClientManager(Stage primaryStage, Consumer<Node> sceneUpdater, Runnable showLauncherCallback) {
        this.primaryStage = primaryStage;
        this.sceneUpdater = sceneUpdater;
        this.showLauncherCallback = showLauncherCallback;
    }

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

    private void attemptConnection(String username, boolean useRmi) {
        this.username = username;
        String HOST = "localhost";
        int SOCKET_PORT = 1234;
        int RMI_PORT = 1111;

        try {

            this.client = new GameClient(useRmi,
                    HOST,
                    useRmi ? RMI_PORT : SOCKET_PORT
            );

        } catch (Exception e) {
            AlertUtils.showError("Connection Error", "No game server available on " + HOST + ":" + (useRmi ? RMI_PORT : SOCKET_PORT));
            return;
        }

        Platform.runLater(() -> createOrJoinGame(username));
    }

    private void createOrJoinGame(String username) {

        Button createButton = new Button("Create Game");
        //createButton.setOnAction(_ -> clientManager.showLoginUI());

        Button joinButton = new Button("Join Game");
        //joinButton.setOnAction(_ -> clientManager.showLoginUI());

        // Use the scene updater to show the buttons
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

    public void handleCreateGame(String username) {
        //create the actual game


        //change screen
        sceneUpdater.accept(new LobbyUI(username).getLayout());
    }

    public void handleJoinGame() {
    }
}