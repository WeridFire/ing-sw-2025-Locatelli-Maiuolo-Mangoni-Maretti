package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Represents a container for a card.
 */
public class CardContainer extends StackPane {

    private Card card;

    private ImageView imageView;
    private static final double FIXED_WIDTH = 220;
    private static final double FIXED_HEIGHT = 400;
    private static final double PADDING = 20;

    /**
     * Constructs a new CardContainer.
     */
    public CardContainer() {
        // Set fixed size for the CardContainer
        this.setPrefSize(FIXED_WIDTH, FIXED_HEIGHT);
        this.setMinSize(FIXED_WIDTH, FIXED_HEIGHT);
        this.setMaxSize(FIXED_WIDTH, FIXED_HEIGHT);

        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);
        this.imageView.setFitWidth(FIXED_WIDTH - PADDING);
        this.imageView.setFitHeight(FIXED_HEIGHT - PADDING);

        this.getChildren().add(imageView);

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
        this.card = card;

        this.getChildren().remove(imageView);
        this.imageView.setImage(AssetHandler.loadRawImage(card.getTextureName()));
        this.getChildren().add(imageView);
    }

}