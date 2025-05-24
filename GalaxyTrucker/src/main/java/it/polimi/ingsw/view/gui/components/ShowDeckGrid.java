package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.stream.Collectors;

public class ShowDeckGrid extends StackPane {

    private GridPane imageGrid;

    public ShowDeckGrid(CardsGroup cg) {
        this(
            cg.getGroupCards().stream()
                    .map(Card::getTextureName)
                    .collect(Collectors.toList())
        );
        AssembleUI.setWatchedCardsGroup(cg);
    }

    public ShowDeckGrid(List<String> cardsFileNames) {
        Rectangle overlayBackground = new Rectangle();
        overlayBackground.setFill(Color.rgb(0, 0, 0, 0.6));
        overlayBackground.widthProperty().bind(widthProperty());
        overlayBackground.heightProperty().bind(heightProperty());

        imageGrid = new GridPane();
        imageGrid.setHgap(10);
        imageGrid.setVgap(10);
        imageGrid.setPadding(new Insets(20));
        imageGrid.setAlignment(Pos.CENTER);

        int cols = 4;
        for (int i = 0; i < cardsFileNames.size(); i++) {
            ImageView imageView = AssetHandler.loadImage(cardsFileNames.get(i));
            imageView.setFitWidth(100);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);

            int row = i / cols;
            int col = i % cols;
            imageGrid.add(imageView, col, row);
        }

        // ScrollPane che contiene la griglia
        ScrollPane scrollPane = new ScrollPane(imageGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // close
        VBox container = getVBox(scrollPane);

        // Aggiungi tutto allo StackPane
        this.getChildren().addAll(overlayBackground, container);
        show();
    }

    private static VBox getVBox(ScrollPane scrollPane) {
        Button closeButton = new Button("Close");
        closeButton.setOnMouseClicked(event -> {
            if(AssembleUI.getWatchedCardsGroup() != null) {
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("hidecg");
                });
                AssembleUI.setWatchedCardsGroup(null);
                AssembleUI.getInstance().clearDeckOverlay();
            }
        });

        // Layout overlay
        VBox container = new VBox(10, scrollPane, closeButton);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(500);
        container.setMaxHeight(450);
        return container;
    }

    public void show() {
        this.setVisible(true);
    }

}