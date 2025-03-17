package src.main.java.it.polimi.ingsw.cards.planets;

import src.main.java.it.polimi.ingsw.cards.Card;

import java.util.UUID;

public class PlanetsCard extends Card {
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public PlanetsCard(String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
	}

	@Override
	public void playEffect(UUID gameId) {

	}
}
