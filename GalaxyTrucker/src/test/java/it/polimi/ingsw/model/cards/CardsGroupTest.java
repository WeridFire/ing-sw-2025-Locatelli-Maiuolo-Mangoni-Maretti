package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.cards.exceptions.CardsGroupException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardsGroupTest {


    Deck deck = Deck.random(GameLevel.TWO);

    @Test
    public void miscTests() throws CardsGroupException {
        CardsGroup group = deck.getGroup(1);
        CardsGroup obfuscated = CardsGroup.obfuscateCardsGroup(group);

        group.showGroup("a");
        String playerName = group.getHeldBy();
        List<Card> cards = group.getGroupCards();
        assertThrows(CardsGroupException.class, () -> group.showGroup("b"));
        assertThrows(CardsGroupException.class, () -> deck.getGroup(0).showGroup("b"));
        assertThrows(CardsGroupException.class, () -> deck.getGroup(0).hideGroup());
        assertThrows(CardsGroupException.class, () -> deck.getGroup(2).hideGroup());
        group.hideGroup();
    }
}