package src.main.java.it.polimi.ingsw.cards;

import java.util.ArrayList;
import java.util.List;

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
     */
    public Deck(int level){

        switch(level){
            case 0:

                break;
            case 1:
                break;
            case 2:
                break;
        }
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
