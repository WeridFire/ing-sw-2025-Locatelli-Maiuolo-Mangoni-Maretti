package it.polimi.ingsw;

import it.polimi.ingsw.enums.DeviceState;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.view.gui.elements.ClientUI;
import it.polimi.ingsw.view.gui.elements.ServerUI;
import it.polimi.ingsw.view.gui.elements.LauncherUI;
import it.polimi.ingsw.view.gui.elements.AlertUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {

    private VBox root;
    private DeviceState state;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.state = null;

        primaryStage.setTitle("Game Launcher");

        root = new VBox(15);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(this::handleCloseRequest);

        showLauncherUI();
        primaryStage.show();
    }

    private void showLauncherUI() {
        LauncherUI launcherUI = new LauncherUI(
                this::startServer,
                this::startClient
        );
        root.getChildren().setAll(launcherUI.getLayout());
    }

    private void startServer() {
        new Thread(() -> {
            String serverStatusText;
            try {
                GameServer.start();
                state = DeviceState.add(state, DeviceState.SERVER);
                serverStatusText = "Server running on localhost:port";
            } catch (AlreadyRunningServerException e) {
                serverStatusText = e.getMessage();
            }

            final String finalStatus = serverStatusText;
            Platform.runLater(() -> {
                ServerUI serverUI = new ServerUI(finalStatus);
                root.getChildren().setAll(serverUI.getLayout());
            });
        }).start();
    }

    private void startClient() {
        ClientUI clientUI = new ClientUI(username -> {
            if (!username.trim().isEmpty()) {
                System.out.println("Client started with username: " + username);
                state = DeviceState.add(state, DeviceState.CLIENT);
                // Proceed to next client view if needed
            }
        });
        root.getChildren().setAll(clientUI.getLayout());
    }

    private void handleCloseRequest(WindowEvent event) {
        if (state == DeviceState.SERVER || state == DeviceState.HOST) {
            boolean shouldClose = AlertUtils.showConfirmation(
                    "Exit Confirmation",
                    "A server is currently running.",
                    "Do you really want to close the application and stop the server?"
            );

            if (!shouldClose) {
                event.consume();
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
