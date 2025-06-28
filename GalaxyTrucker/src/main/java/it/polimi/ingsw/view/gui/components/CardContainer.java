package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Represents a container for a card.
 */
public class CardContainer extends VBox {

    private Card card;

    private ImageView imageView;
    public static final double FIXED_WIDTH = 220;
    private static final double FIXED_HEIGHT = 400;
    private static final double PADDING = 10;

    private Button endTurnButton;

    /**
     * Constructs a new CardContainer.
     */
    public CardContainer() {
        // Set fixed size for the CardContainer
        this.setPrefSize(FIXED_WIDTH, FIXED_HEIGHT);
        this.setMinSize(FIXED_WIDTH, FIXED_HEIGHT);
        this.setMaxSize(FIXED_WIDTH, FIXED_HEIGHT);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(FIXED_WIDTH - PADDING);
        imageView.setFitHeight(FIXED_HEIGHT - PADDING);

        endTurnButton = new Button("End Turn");
        endTurnButton.setOnMouseClicked(e -> Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("endTurn");
        }));

        VBox container = new VBox(10); // Optional spacing
        container.getChildren().addAll(imageView, endTurnButton);
        container.setAlignment(Pos.CENTER);
        this.getChildren().add(container);
    }

    /**
     * Constructs a new CardContainer with a given card.
     * @param card the card to be displayed
     */
    public CardContainer(Card card) {
        this(); // Call the default constructor
        setCard(card);
    }

    /**
     * Sets the card to be displayed in the container.
     * @param card the card to be displayed
     */
    public void setCard(Card card) {
        if (card == null) return;

        this.card = card;

        imageView.setImage(AssetHandler.loadRawImage(card.getTextureName()));
    }
}
