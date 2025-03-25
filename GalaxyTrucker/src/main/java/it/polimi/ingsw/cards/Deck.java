package src.main.java.it.polimi.ingsw.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Deck {
    /**
     * The list of cards present in the deck.
     */
    private List<Card> deck;

    /**
     * The list of cardsgroup, for the ship assembly phase.
     */
    private List<CardsGroup> cardsGroups;

    /**
     * Instance a deck with randomly selected cards, based on a level.
     * @param level The deck level.
     * @param gameId The id of the associated game.
     */
    public Deck(int level, UUID gameId){
        switch(level){
            case 0:
                this.deck = DeckFactory.createTutorialDeck(gameId);
                break;
            case 1:
                this.deck = DeckFactory.createLevelOneDeck(gameId);
                break;
            case 2:
                this.deck = DeckFactory.createLevelTwoDeck(gameId);
                break;
        }
        //TODO: implement extracting cards from pool and splitting them in subgroups
    }

    /**
     * Access a specific card group, during ship-building phase.
     * @param index The card group index.
     * @return The correspondent card group.
     */
    public CardsGroup getGroup(int index) {
        return cardsGroups.get(index);
    }


}
