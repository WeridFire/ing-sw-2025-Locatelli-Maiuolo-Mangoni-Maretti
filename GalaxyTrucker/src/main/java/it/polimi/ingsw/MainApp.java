package it.polimi.ingsw;

// Keep necessary imports
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {

    private VBox root; // The root container for the current scene content
    private Stage primaryStage;
    private ClientManager clientManager;

    /***
     * Singleton getter for ClientManager.
     *
     * @return {@link ClientManager}
     */
    public ClientManager getClientManager() {
        if (clientManager == null) {
            this.clientManager = new ClientManager(primaryStage, this::updateSceneRoot, this::showLauncherUI);
        }
        return clientManager;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Game Launcher");

        AssetHandler.preLoadTextures();

        // Initialize the root container
        root = new VBox(15);
        root.setAlignment(Pos.CENTER); // Center content

        // Create managers, passing the stage, the scene updater lambda, and a callback to show launcher
        getClientManager();

        // Set the initial scene
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);

        // Show the initial launcher UI
        showLauncherUI();

        // Set the initial close request handler for the launcher
        primaryStage.setOnCloseRequest(this::handleLauncherCloseRequest);

        primaryStage.show();
    }

    /**
     * Updates the content of the primary stage's scene.
     * @param newContent The new Node to display.
     */
    private void updateSceneRoot(Node newContent) {
        root.getChildren().setAll(newContent);
    }

    /**
     * Displays the initial launcher buttons.
     */
    private void showLauncherUI() {
       clientManager.showLoginUI();
    }


    /**
     * Handles the close request specifically for the Launcher window.
     */
    private void handleLauncherCloseRequest(WindowEvent event) {
        System.out.println("Launcher window closed. Exiting application.");
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
