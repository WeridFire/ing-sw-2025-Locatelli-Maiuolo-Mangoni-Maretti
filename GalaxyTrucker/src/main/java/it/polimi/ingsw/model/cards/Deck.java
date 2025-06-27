package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.cards.exceptions.CardsGroupException;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.util.Default;

import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable {
    /**
     * The list of cards present in the deck.
     */
    private final List<Card> deck = new ArrayList<>();
    private Card currentCard = null;

    /**
     * The list of cardsgroup, for the ship assembly phase.
     */
    private final List<CardsGroup> cardsGroups = new ArrayList<>();

    /**
     * selected game level while creating this deck
     */
    private final GameLevel gameLevel;

    /**
     * Private constructor for creating an obfuscated cards group that will be sent to the player.
     * The new Deck will only have the top card and the cards group that the player is currently holding (if any)
     * @param deck The original deck
     * @param player The target player
     */
    private Deck(Deck deck, Player player){
        currentCard = deck.currentCard;
        for(CardsGroup c : deck.cardsGroups){
            if(!Objects.equals(c.getHeldBy(), player.getUsername())){
                cardsGroups.add(CardsGroup.obfuscateCardsGroup(c));
            }else{
                cardsGroups.add(c);
            }
        }
        gameLevel = deck.gameLevel;
    }

    private Deck(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
    }

    /**
     * Instance a deck with randomly selected cards, based on a level.
     * @param level The deck level.
     */
    public static Deck random(GameLevel level){
        Deck result = new Deck(level);

        List<Card> tutorialPool;
        List<Card> l1Pool;
        List<Card> l2Pool;
        switch(level){
            case TESTFLIGHT:
                //for the tutorial is simply puts 8 cards randomly into the deck. No cardsgroup here.
                tutorialPool = DeckFactory.createTutorialDeck();
                Collections.shuffle(tutorialPool);
                while (result.deck.size() < 8){
                    result.deck.add(tutorialPool.removeFirst());
                }
                break;
            case ONE:
                l1Pool = DeckFactory.createLevelOneDeck();
                Collections.shuffle(l1Pool);
                //Creates 4 cardgroups of 2 level1 cards each. The first group is the "secret" one, other 3 are the
                //flight predictions, so not secret.
                //TODO: fact check the content and dimension of the level 1 flight groups. On board these are not
                // displayed (atleast in the digital resources)
                while(result.cardsGroups.size() < 4){
                    List<Card> groupCard = new ArrayList<>();
                    groupCard.add(l1Pool.removeFirst());
                    groupCard.add(l1Pool.removeFirst());
                    result.cardsGroups.add(new CardsGroup(groupCard, result.cardsGroups.isEmpty()));
                }
                break;
            case TWO:
                l2Pool = DeckFactory.createLevelTwoDeck();
                l1Pool = DeckFactory.createLevelOneDeck();
                Collections.shuffle(l2Pool);
                Collections.shuffle(l1Pool);
                //Create 4 cardgroups each with 2 cards from level 2 and 1 from level 1. Also makes only the first
                //Cardgroup secret.
                while(result.cardsGroups.size() < 4){
                    List<Card> groupCard = new ArrayList<>();
                    groupCard.add(l1Pool.removeFirst());
                    groupCard.add(l2Pool.removeFirst());
                    groupCard.add(l2Pool.removeFirst());
                    result.cardsGroups.add(new CardsGroup(groupCard, result.cardsGroups.isEmpty()));
                }
                break;
        }

        return result;
    }

    /**
     * Creates a deck with a deterministic set of cards and (optionally) groups them into
     * {@link CardsGroup}s of fixed size.
     * <p>
     * This method is typically used for testing or predefined setups where card selection is not randomized.
     * It assigns the {@link GameLevel} of the deck based on the highest level among the provided cards,
     * unless no grouping is requested.
     *
     * @param cards the list of cards to include in the deck. Must not be {@code null}.
     * @param groupsSize if non-{@code null}, groups the cards into {@link CardsGroup}s of the given size.
     *                   Each group contains {@code groupsSize} cards, except possibly the last one.
     *                   If {@code null}, all cards are added to the deck directly with no grouping.
     *                   If {@code < 1}, it defaults to {@code 1}.
     * @return a {@link Deck} instance with cards deterministically arranged, optionally grouped.
     */
    public static Deck deterministic(List<Card> cards, Integer groupsSize) {
        GameLevel level = null;
        if (groupsSize != null) {
            for (Card card : cards) {
                level = GameLevel.max(level, card.getLevel());
            }
        }

        Deck result = new Deck(level);
        if (groupsSize == null) {
            result.deck.addAll(cards);
            return result;
        } // else
        if (groupsSize < 1) groupsSize = 1;
        int cardInGroupIndex = 0;
        List<Card> groupCard = new ArrayList<>();
        for (Card card : cards) {
            if (cardInGroupIndex == groupsSize) {
                result.cardsGroups.add(new CardsGroup(groupCard, result.cardsGroups.isEmpty()));
                cardInGroupIndex = 0;
                groupCard.clear();
            }
            groupCard.add(card);
            cardInGroupIndex++;
        }

        return result;
    }

    /**
     * Prepares the deck from the initial form of cards groups into a full deck with all the cards mixed.
     * It also handles the rule "start with one card of the played level".
     */
    public void mixGroupsIntoCards() {
        for(CardsGroup c : cardsGroups){
            while(!c.getGroupCards().isEmpty()){
                deck.add(c.getGroupCards().removeFirst());
            }
        }
        // forget about groups of cards
        cardsGroups.clear();
        // now shuffle deck
        Collections.shuffle(deck);
        // ensure the first card is one of the selected level
        while (deck.getFirst().getLevel() != gameLevel) {
            deck.add(deck.removeFirst());  // set as last card
        }

        Card firstCard = deck.stream().filter(c -> c.getTitle().equals(Default.STARTING_CARD)).findAny().orElse(null);
        if(firstCard != null){
            deck.remove(firstCard);
            deck.addFirst(firstCard);
        }
        // this loop will end thanks to the constructor, which gives the correct number of cards
    }

    /**
     * Access a specific card group, during ship-building phase.
     * @param index The card group index.
     * @return The correspondent card group.
     * @throws CardsGroupException if the provided index is not associated to any card group
     */
    public CardsGroup getGroup(int index) throws CardsGroupException {
        try {
            return cardsGroups.get(index);
        } catch(IndexOutOfBoundsException e) {
            throw new CardsGroupException("Card group with index '" + index + "' not found");
        }
    }

    /**
     * Access all the card groups in order and construct a list of booleans,
     * where for all the indices i:
     * <ul>
     *  <li>\result(i) is true <===> getGroup(i) is available to be taken by any player;</li>
     *  <li>\result(i) is false <===> getGroup(i) is already taken by a player;</li>
     *  <li>\result(i) is null <===> getGroup(i) is secret</li>
     * </ul>
     * @return The created list of booleans
     */
    public List<Boolean> getGroupsAvailability() {
        List<Boolean> availability = new ArrayList<>();
        for (CardsGroup c : cardsGroups) {
            availability.add(c.isSecret() ? null : (c.getHeldBy() == null));
        }
        return availability;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public Card drawNextCard(){
        currentCard = deck.isEmpty() ? null : deck.removeFirst();
        return currentCard;
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
