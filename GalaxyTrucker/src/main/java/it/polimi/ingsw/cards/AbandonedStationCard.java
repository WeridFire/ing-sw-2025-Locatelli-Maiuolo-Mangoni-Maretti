package src.main.java.it.polimi.ingsw.cards;

import java.util.UUID;

public class AbandonedStationCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public AbandonedStationCard(String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
	}

	@Override
	public void playEffect(UUID gameId) {

	}
}
