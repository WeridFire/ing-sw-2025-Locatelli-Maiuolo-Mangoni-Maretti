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

public class MainApp extends Application {

    private Button startServerButton;
    private Button startClientButton;
    private Label serverStatusLabel;
    private VBox root;
    private DeviceState state;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Game Launcher");

        state = null;

        // Crea pulsante "Start Server"
        startServerButton = new Button("Start Server");
        startServerButton.setOnAction(_ -> startServer());

        // Crea pulsante "Start Client"
        startClientButton = new Button("Start Client");
        startClientButton.setOnAction(_ -> startClient());

        // Etichetta per stato server
        serverStatusLabel = new Label();
        serverStatusLabel.setVisible(false);

        // Layout
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(startServerButton, startClientButton, serverStatusLabel);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(this::handleCloseRequest);
        primaryStage.show();
    }

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

    private void startClient() {
        //hide butt
        startServerButton.setVisible(false);
        startClientButton.setVisible(false);

        // Username
        Label promptLabel = new Label("Enter your username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        Button confirmButton = getConfirmationButton(usernameField);

        //change layout
        root.getChildren().setAll(promptLabel, usernameField, confirmButton);
    }

    private Button getConfirmationButton(TextField usernameField) {
        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(_ -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                System.out.println("Client started with username: " + username);
                state = DeviceState.add(state, DeviceState.CLIENT);

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a valid username.");
                alert.showAndWait();
            }
        });
        return confirmButton;
    }

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
