package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.gamePhases.exceptions.NoMoreCardsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Deck {
    /**
     * The list of cards present in the deck.
     */
    final List<Card> deck = new ArrayList<>();

    /**
     * current index of the list
     */
    private int currentIndex = 0;

    /**
     * The list of cardsgroup, for the ship assembly phase.
     */
    private final List<CardsGroup> cardsGroups = new ArrayList<>();

    /**
     * Instance a deck with randomly selected cards, based on a level.
     * @param level The deck level.
     * @param gameId The id of the associated game.
     */
    public Deck(int level, UUID gameId){
        List<Card> tutorialPool = null;
        List<Card> l1Pool = null;
        List<Card> l2Pool = null;
        switch(level){
            case 0:
                //for the tutorial is simply puts 8 cards randomly into the deck. No cardsgroup here.
                tutorialPool = DeckFactory.createTutorialDeck(gameId);
                Collections.shuffle(tutorialPool);
                while (this.deck.size() < 8){
                    this.deck.add(tutorialPool.removeFirst());
                }
                break;
            case 1:
                l1Pool = DeckFactory.createLevelOneDeck(gameId);
                Collections.shuffle(l1Pool);
                //Creates 4 cardsgroup of 2 level1 cards each. The first group is the "secret" one, other 3 are the
                //flight predictions, so not secret.
                //TODO: fact check the content and dimension of the level 1 flight groups. On board these are not
                // displayed (atleast in the digital resources)
                while(this.cardsGroups.size() < 4){
                    List<Card> groupCard = new ArrayList<>();
                    groupCard.add(l1Pool.removeFirst());
                    groupCard.add(l1Pool.removeFirst());
                    this.cardsGroups.add(new CardsGroup(groupCard, cardsGroups.isEmpty()));
                }
                break;
            case 2:
                l2Pool = DeckFactory.createLevelTwoDeck(gameId);
                l1Pool = DeckFactory.createLevelOneDeck(gameId);
                Collections.shuffle(l2Pool);
                Collections.shuffle(l1Pool);
                //Create 4 cardgroups each with 2 cards from level 2 and 1 from level 1. Also makes only the first
                //Cardgroup secret.
                while(this.cardsGroups.size() < 4){
                    List<Card> groupCard = new ArrayList<>();
                    groupCard.add(l1Pool.removeFirst());
                    groupCard.add(l2Pool.removeFirst());
                    groupCard.add(l2Pool.removeFirst());
                    cardsGroups.add(new CardsGroup(groupCard, cardsGroups.isEmpty()));
                }
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

    public Card getTopCard() {
        currentIndex++;
        try{
            return deck.get(currentIndex-1);
        } catch (IndexOutOfBoundsException e){
            throw new NoMoreCardsException("No more cards");
        }
    }

    public void shuffle(){
        Collections.shuffle(deck);
    }
}
