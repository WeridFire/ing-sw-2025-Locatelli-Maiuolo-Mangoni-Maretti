package it.polimi.ingsw;

import it.polimi.ingsw.enums.DeviceState;
import it.polimi.ingsw.network.GameServer;

import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// This is the main GUI application for launching server and client
public class MainApp extends Application {

    private Button startServerButton;
    private Label serverStatusLabel;
    private DeviceState state;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Game Launcher");

        state = null;

        // Create "Start Server" button
        startServerButton = new Button("Start Server");
        startServerButton.setOnAction(_ -> startServer());

        // Create "Start Client" button
        Button startClientButton = new Button("Start Client");
        startClientButton.setOnAction(_ -> startClient());

        // Label to show server status (initially hidden)
        serverStatusLabel = new Label();
        serverStatusLabel.setVisible(false);

        // Layout container with spacing and center alignment
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(startServerButton, startClientButton, serverStatusLabel);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);

        // Handle close request
        primaryStage.setOnCloseRequest(this::handleCloseRequest);

        primaryStage.show();
    }

    // Starts the server in a new thread and updates the UI
    private void startServer() {
        new Thread(() -> {
            String serverStatusText;
            try {
                GameServer.start(); // Start the server (blocking)
                state = DeviceState.add(state, DeviceState.SERVER);
                serverStatusText = "You are running a server on " +
                        "localhost" + ":" +
                        "port";
            } catch (AlreadyRunningServerException e) {
                serverStatusText = e.getMessage();
            }

            final String finalServerStatusText = serverStatusText;  // make it effectively final
            // Once started, update UI on the JavaFX thread
            Platform.runLater(() -> {
                startServerButton.setVisible(false);
                serverStatusLabel.setText(finalServerStatusText);
                serverStatusLabel.setVisible(true);
            });
        }).start();
    }

    // Dummy client launcher method (replace with real client logic)
    private void startClient() {
        System.out.println("Client started!");
        state = DeviceState.add(state, DeviceState.CLIENT);
        // Add real client startup logic here
    }

    // Handle window close: check if server is active and confirm shutdown
    private void handleCloseRequest(WindowEvent event) {
        if (state == DeviceState.SERVER || state == DeviceState.HOST) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Confirmation");
            alert.setHeaderText("A server is currently running.");
            alert.setContentText("Do you really want to close the application and stop the server?");

            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yes, no);

            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    System.exit(0);
                } else {
                    event.consume();  // Cancel the window close
                }
            });
        }
        else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
