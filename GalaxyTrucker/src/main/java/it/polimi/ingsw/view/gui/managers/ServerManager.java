package it.polimi.ingsw.view.gui.managers;

import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import it.polimi.ingsw.view.gui.UIs.ServerUI;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ServerManager {

    private final Stage primaryStage;
    private final Consumer<Node> sceneUpdater;
    private final Runnable showLauncherCallback;

    public ServerManager(Stage primaryStage, Consumer<Node> sceneUpdater, Runnable showLauncherCallback) {
        this.primaryStage = primaryStage;
        this.sceneUpdater = sceneUpdater;
        this.showLauncherCallback = showLauncherCallback;
    }

    public void startServerAndShowUI() {
        new Thread(() -> {
            String serverStatusText;
            boolean serverStarted = false;
            try {
                GameServer.start();
                serverStatusText = "Server running on localhost:port"; // Update port later
                serverStarted = true;
            } catch (AlreadyRunningServerException e) {
                Platform.runLater(() -> AlertUtils.showError("Server Error", e.getMessage()));
                return;
            } catch (Exception e) {
                serverStatusText = "An unexpected error occurred: " + e.getMessage();
                e.printStackTrace();
                String finalServerStatusText = serverStatusText;
                Platform.runLater(() -> AlertUtils.showError("Server Error", finalServerStatusText));
                return;
            }

            final String finalStatus = serverStatusText;
            final boolean finalServerStarted = serverStarted;

            Platform.runLater(() -> {
                if (finalServerStarted) {
                    ServerUI serverUI = new ServerUI(finalStatus);
                    sceneUpdater.accept(serverUI.getLayout());
                    primaryStage.setTitle("Server Status");
                    // Adjust close request for when Server UI is shown
                    primaryStage.setOnCloseRequest(event -> {
                        boolean confirmed = AlertUtils.showConfirmation(
                                "Stop Server",
                                "Server Confirmation",
                                "Do you want to stop the server and close the application?"
                        );
                        if (confirmed) {
                            stopServer();
                            Platform.exit();
                            System.exit(0);
                        } else {
                            event.consume();
                        }
                    });
                }
            });
        }).start();
    }

    // if we want to stop server and return to launcher
    private void stopServerAndShowLauncher() {
        stopServer();
        Platform.runLater(() -> {
            showLauncherCallback.run();
            primaryStage.setTitle("Game Launcher");
            // Reset close request handler for the launcher
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Launcher closed.");
                Platform.exit();
                System.exit(0);
            });
        });
    }


    public boolean isServerRunning() {
        return GameServer.isRunning();
    }

    public void stopServer() {
        if (isServerRunning()) {
            System.out.println("Stopping server...");
            //GameServer.stop();
        }
    }
}
