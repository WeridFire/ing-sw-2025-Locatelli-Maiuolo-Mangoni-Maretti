package src.main.java.it.polimi.ingsw.cards.warzone;

import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class WarZoneCard extends Card {

	/**
	 * Levels of this warZone.
	 */
	private WarLevel[] warLevels;

	/**
	 * Instances a card.
	 * @param warLevels the levels for this warzone.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public WarZoneCard(WarLevel[] warLevels, String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
		this.warLevels = warLevels;
	}

	/**
	 * Iterates each single war level. For each one selects the worst player. Punishes that player.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		for(WarLevel wl : warLevels){
			Player p = wl.getWorstPlayer(gameId);
			if(p != null){
				wl.applyPunishment(p);
			}
		}
	}
}
