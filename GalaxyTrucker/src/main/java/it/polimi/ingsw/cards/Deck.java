package it.polimi.ingsw.cards;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.player.Player;

import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable {
    /**
     * The list of cards present in the deck.
     */
    final List<Card> deck = new ArrayList<>();
    private Card currentCard = null;
    /**
     * current index of the list
     */
    private int currentIndex = 0;

    /**
     * The list of cardsgroup, for the ship assembly phase.
     */
    private final List<CardsGroup> cardsGroups = new ArrayList<>();

    /**
     * Private constructor for creating an obfuscated cards group that will be sent to the player.
     * The new Deck will only have the top card and the cards group that the player is currently holding (if any)
     * @param deck The original deck
     * @param player The target player
     */
    private Deck(Deck deck, Player player){
        this.currentCard = deck.getTopCard();
        for(CardsGroup c : deck.cardsGroups){
            if(!Objects.equals(c.getHeldBy(), player.getUsername())){
                this.cardsGroups.add(new CardsGroup(null, c.isSecret()));
            }else{
                this.cardsGroups.add(c);
            }
        }
    }

    /**
     * Instance a deck with randomly selected cards, based on a level.
     * @param level The deck level.
     */
    public Deck(GameLevel level){
        List<Card> tutorialPool = null;
        List<Card> l1Pool = null;
        List<Card> l2Pool = null;
        switch(level){
            case TESTFLIGHT:
                //for the tutorial is simply puts 8 cards randomly into the deck. No cardsgroup here.
                tutorialPool = DeckFactory.createTutorialDeck();
                Collections.shuffle(tutorialPool);
                while (this.deck.size() < 8){
                    this.deck.add(tutorialPool.removeFirst());
                }
                break;
            case ONE:
                l1Pool = DeckFactory.createLevelOneDeck();
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
            case TWO:
                l2Pool = DeckFactory.createLevelTwoDeck();
                l1Pool = DeckFactory.createLevelOneDeck();
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

    public void convertGroupsToCards(){
        for(CardsGroup c : cardsGroups){
            while(!c.getGroupCards().isEmpty()){
                deck.add(c.getGroupCards().removeFirst());
            }
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
        return currentCard;
    }

    public void drawNextCard(){
        if(deck.isEmpty()){
            currentCard = null;
            return;
        }
        currentCard = deck.removeFirst();
    }

    /**
     * Creates an obfuscated copy of the deck, to be sent to the player. The deck will only display the top card
     * and the cards group if the player is holding it.
     * @param original The original deck.
     * @param target The player target to show the cards group to. It will be created ad hoc for them.
     * @return A new deck obfuscated for target.
     */
    public static Deck obfuscateDeck(Deck original, Player target){
        return new Deck(original, target);
    }
}
