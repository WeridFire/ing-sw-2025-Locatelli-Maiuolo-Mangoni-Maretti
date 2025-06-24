package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Deck;
import it.polimi.ingsw.model.cards.DeckFactory;
import it.polimi.ingsw.model.cards.exceptions.CardsGroupException;
import it.polimi.ingsw.enums.GameLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void testDeckFactorySize() {
        assertEquals(8, DeckFactory.createTutorialDeck().size());
        assertEquals(20, DeckFactory.createLevelOneDeck().size());
        assertEquals(20, DeckFactory.createLevelTwoDeck().size());
    }

    @Test
    void testTutorialDeck() {
        Deck deck = Deck.random(GameLevel.TESTFLIGHT);
        assertEquals(0, deck.getGroupsAvailability().size());
        deck.mixGroupsIntoCards();
        for (int i = 0; i < 8; i++) {
            assertNotNull(deck.drawNextCard());
        }
        assertNull(deck.drawNextCard());
    }

    @Test
    void testLevelOneDeck() throws CardsGroupException {
        Deck deck = Deck.random(GameLevel.ONE);
        assertArrayEquals(new Boolean[] {null, true, true, true}, deck.getGroupsAvailability().toArray());
        for (int i = 0; i < 4; i++) {
            List<Card> cards = deck.getGroup(i).getGroupCards();
            assertEquals(2, cards.size());
            assertEquals(2, cards.stream().filter(c -> c.getLevel() == GameLevel.ONE).count());
        }
        deck.mixGroupsIntoCards();
        assertEquals(0, deck.getGroupsAvailability().size());
        assertEquals(GameLevel.ONE, deck.drawNextCard().getLevel());
        for (int i = 1; i < 8; i++) {
            assertNotNull(deck.drawNextCard());
        }
        assertNull(deck.drawNextCard());
    }

    @Test
    void testLevelTwoDeck() throws CardsGroupException {
        Deck deck = Deck.random(GameLevel.TWO);
        assertArrayEquals(new Boolean[] {null, true, true, true}, deck.getGroupsAvailability().toArray());
        for (int i = 0; i < 4; i++) {
            List<Card> cards = deck.getGroup(i).getGroupCards();
            assertEquals(3, cards.size());
            assertEquals(1, cards.stream().filter(c -> c.getLevel() == GameLevel.ONE).count());
            assertEquals(2, cards.stream().filter(c -> c.getLevel() == GameLevel.TWO).count());
        }
        deck.mixGroupsIntoCards();
        assertEquals(0, deck.getGroupsAvailability().size());
        assertEquals(GameLevel.TWO, deck.drawNextCard().getLevel());
        for (int i = 1; i < 12; i++) {
            assertNotNull(deck.drawNextCard());
        }
        assertNull(deck.drawNextCard());
    }

}