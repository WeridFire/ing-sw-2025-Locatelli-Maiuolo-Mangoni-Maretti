package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.Path;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class CardContainer extends StackPane {

    private Card card;

    private ImageView imageView;
    private static final double FIXED_WIDTH = 220;
    private static final double FIXED_HEIGHT = 400;
    private static final double PADDING = 20;

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

    public CardContainer(Card card) {
        this(); // Call the default constructor
        setCard(card);
    }

    public void setCard(Card card) {
        this.card = card;

        this.getChildren().remove(imageView);
        this.imageView.setImage(AssetHandler.loadRawImage(card.getTextureName()));
        this.getChildren().add(imageView);
    }

}