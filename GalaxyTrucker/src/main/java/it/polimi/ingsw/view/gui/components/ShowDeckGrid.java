package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.CardsGroup;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.stream.Collectors;

public class ShowDeckGrid extends StackPane {
    private static final double CARD_WIDTH = 165;
    private static final double CARD_HEIGHT = 254;

    private GridPane imageGrid;

    public ShowDeckGrid(CardsGroup cg) {
        this(
            cg.getGroupCards().stream()
                    .map(Card::getTextureName)
                    .collect(Collectors.toList())
        );
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
            imageView.setFitWidth(CARD_WIDTH);
            imageView.setFitHeight(CARD_HEIGHT);
            imageView.setPreserveRatio(true);

            int row = i / cols;
            int col = i % cols;
            imageGrid.add(imageView, col, row);
        }

        // close
        VBox container = getVBox(imageGrid);

        // Aggiungi tutto allo StackPane
        this.getChildren().addAll(overlayBackground, container);
        show();
    }

    private static VBox getVBox(Pane imagesPane) {
        Button closeButton = new Button("Close");
        closeButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("hidecg");
            });
        });

        // Layout overlay
        VBox container = new VBox(10, imagesPane, closeButton);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(CARD_WIDTH * 3 + 40);
        container.setMaxHeight(450);
        return container;
    }

    public void show() {
        this.setVisible(true);
    }

}