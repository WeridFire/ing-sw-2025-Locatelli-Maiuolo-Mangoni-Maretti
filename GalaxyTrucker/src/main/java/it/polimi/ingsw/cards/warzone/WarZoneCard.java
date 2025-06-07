package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.view.cli.CLIFrame;

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
		super("WAR ZONE", textureName, level);
		this.warLevels = warLevels;
	}

	@Override
	public void startCardBehaviour(GameData game) {
		playWarZonePunishment(game, 0);
	}


	public void playWarZonePunishment(GameData game, int idx){
		WarLevel wl = null;
		if(idx >= 0 && idx < warLevels.length){
			wl = warLevels[idx];
		}
		if(wl == null){
			//we have finished all the war zones.
			game.getCurrentGamePhase().endPhase();
			return;
		}
		WarLevel finalWarLevel = wl;
		finalWarLevel.getWorstPlayer(
				game,
				(worstPlayer) -> {
					finalWarLevel.applyPunishment(
							worstPlayer,
							game,
							(p) -> {
								//proceed with the next warzone punishment.
								playWarZonePunishment(game, idx+1);
							});
				}
			);

	}


	/**
	 * Generates a CLI representation of the implementing object.
	 *
	 * @return A {@link CLIFrame} containing the CLI representation.
	 */
	@Override
	public CLIFrame getCLIRepresentation() {
		// TODO
		return super.getCLIRepresentation();
	}
}
