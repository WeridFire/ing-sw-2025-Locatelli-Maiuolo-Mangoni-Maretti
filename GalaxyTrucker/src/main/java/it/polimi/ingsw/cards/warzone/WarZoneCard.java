package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.UUID;

public class WarZoneCard extends Card {

	/**
	 * Levels of this warZone.
	 */
	private final WarLevel[] warLevels;

	/**
	 * Instances a card.
	 * @param warLevels the levels for this warzone.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public WarZoneCard(WarLevel[] warLevels, String textureName, int level) {
		super(textureName, level);
		this.warLevels = warLevels;
	}

	/**
	 * Iterates each single war level. For each one selects the worst player. Punishes that player.
	 */
	@Override
	public void playEffect(GameData game) {
		for(WarLevel wl : warLevels){
			Player p = wl.getWorstPlayer(game);
			if(p != null){
				wl.applyPunishment(p, game);
			}
		}
	}

	/**
	 * Generates a CLI representation of the implementing object.
	 *
	 * @return A {@link CLIFrame} containing the CLI representation.
	 */
	@Override
	public CLIFrame getCLIRepresentation() {
		// TODO
		return null;
	}
}
