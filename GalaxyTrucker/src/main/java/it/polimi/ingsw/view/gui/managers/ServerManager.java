package it.polimi.ingsw.view.gui.managers;

import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import it.polimi.ingsw.view.gui.UIs.ServerUI;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Manages the server-side GUI flow, including starting and stopping the server,
 * displaying server status, and handling UI transitions back to the launcher.
 */
public class ServerManager {

    private final Stage primaryStage;
    private final Consumer<Node> sceneUpdater;
    private final Runnable showLauncherCallback;

    /**
     * Constructs a ServerManager with the specified primary stage, scene updater, and launcher callback.
     *
     * @param primaryStage        the JavaFX Stage used to display server UI
     * @param sceneUpdater        a Consumer to update the current scene with a new Node
     * @param showLauncherCallback a Runnable to execute when returning to the launcher UI
     */
    public ServerManager(Stage primaryStage, Consumer<Node> sceneUpdater, Runnable showLauncherCallback) {
        this.primaryStage = primaryStage;
        this.sceneUpdater = sceneUpdater;
        this.showLauncherCallback = showLauncherCallback;
    }

    /**
     * Starts the game server in a background thread and updates the UI to display server status.
     * <p>
     * If the server is already running, it shows an error alert. On successful start,
     * it displays the ServerUI with the current status and adjusts the close request handler
     * to confirm stopping the server.
     */
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

    /**
     * Stops the server if it is running and returns to the launcher UI.
     * <p>
     * Invokes the server shutdown logic, then switches the scene back to the launcher
     * and resets the window title and close handler accordingly.
     */
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

    /**
     * Checks whether the server is currently running.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isServerRunning() {
        return GameServer.isRunning();
    }

    /**
     * Stops the server if it is running. Prints a log message indicating shutdown.
     * <p>
     * Note: Actual server stop logic should be implemented in GameServer.stop().
     */
    public void stopServer() {
        if (isServerRunning()) {
            System.out.println("Stopping server...");
            //GameServer.stop();
        }
    }
}