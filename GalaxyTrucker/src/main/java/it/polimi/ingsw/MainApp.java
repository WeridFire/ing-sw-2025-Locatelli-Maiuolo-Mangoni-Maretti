package it.polimi.ingsw;

// Keep necessary imports
import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.managers.ServerManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {

    private VBox root; // The root container for the current scene content
    private Stage primaryStage;
    private ServerManager serverManager;
    private ClientManager clientManager;

    /***
     * Singleton getter for ServerManager.
     *
     * @return {@link ServerManager}
     */
    public ServerManager getServerManager() {
        if (serverManager == null) {
            this.serverManager = new ServerManager(primaryStage, this::updateSceneRoot, this::showLauncherUI);
        }
        return serverManager;
    }

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

        // Initialize the root container
        root = new VBox(20);
        root.setAlignment(Pos.CENTER); // Center content

        // Create managers, passing the stage, the scene updater lambda, and a callback to show launcher
        getClientManager();
        getServerManager();

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
        // Ensure alignment is maintained if needed, though VBox alignment might suffice
        // root.setAlignment(Pos.CENTER);
    }

    /**
     * Displays the initial launcher buttons.
     */
    private void showLauncherUI() {
        Button startServerButton = new Button("Start Server");
        startServerButton.setFont(Font.font("Orbitron", FontWeight.BOLD, 14));
        startServerButton.setTextFill(Color.WHITE);
        startServerButton.setStyle("-fx-background-color: linear-gradient(to right, #2193b0, #6dd5ed);"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 20 10 20;");
        startServerButton.setEffect(new DropShadow(5, Color.BLACK));

        if (serverManager != null && serverManager.isServerRunning()) {
            startServerButton.setDisable(true);
            startServerButton.setText("Server Running");
        } else {
            startServerButton.setOnAction(_ -> serverManager.startServerAndShowUI());
        }

        Button startClientButton = new Button("Start Client");
        startClientButton.setFont(Font.font("Orbitron", FontWeight.BOLD, 14));
        startClientButton.setTextFill(Color.WHITE);
        startClientButton.setStyle("-fx-background-color: linear-gradient(to right, #3f2b96, #a8c0ff);"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 20 10 20;");
        startClientButton.setEffect(new DropShadow(5, Color.BLACK));
        startClientButton.setOnAction(_ -> clientManager.showLoginUI());

        VBox launcherLayout = new VBox(20, startServerButton, startClientButton);
        launcherLayout.setAlignment(Pos.CENTER);
        launcherLayout.setPrefSize(500, 400);
        launcherLayout.setBackground(new Background(new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0F0C29")),
                        new Stop(0.5, Color.web("#302B63")),
                        new Stop(1, Color.web("#24243E"))
                ),
                CornerRadii.EMPTY, null)));

        updateSceneRoot(launcherLayout);

        primaryStage.setTitle("Game Launcher");
        primaryStage.setOnCloseRequest(this::handleLauncherCloseRequest);
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
