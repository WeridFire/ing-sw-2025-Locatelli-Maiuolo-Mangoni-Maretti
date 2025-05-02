package it.polimi.ingsw.view.gui.managers;

import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.view.gui.elements.LoginUI;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ClientManager {

    private final Stage primaryStage;
    private final Consumer<Node> sceneUpdater;
    private final Runnable showLauncherCallback;
    private GameClient client;

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
        try {
            String HOST = "localhost";
            int SOCKET_PORT = 1234;
            int RMI_PORT = 1111;
            this.client = new GameClient(useRmi,
                    HOST,
                    useRmi ? RMI_PORT : SOCKET_PORT
            );

        } catch (Exception e) {
            throw new RuntimeException(e); //shouldn't happen
        }

        Platform.runLater(() -> showGameView(username));
    }

    private void showGameView(String username) {
        VBox gameLayout = new VBox(new Label("Welcome, " + username + "!"));
        sceneUpdater.accept(gameLayout);
        primaryStage.setTitle("Galaxy Trucker - " + username);
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Client window closed.");
            Platform.exit();
            System.exit(0);
        });
    }
}