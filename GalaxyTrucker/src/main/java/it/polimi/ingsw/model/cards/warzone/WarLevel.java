package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class WarLevel implements Serializable {

	/**
	 * The criteria used to select the worst player in this level.
	 */
	private final WarCriteria warCriteria;

	/**
	 * The function applied to the worst player, the punishment.
	 */
	private final WarPunishment warPunishment;

	/**
	 * Instances a war level. Multiple war levels build up to a war zone.
	 * @param warCriteria Use a warfactory to generate this. A criteria to decide the worst player to punish.
	 * @param warPunishment Use a warfactory to generate this. The punishment to apply to the selected player
	 */
	public WarLevel(WarCriteria warCriteria, WarPunishment warPunishment) {
		this.warCriteria = warCriteria;
		this.warPunishment = warPunishment;
	}

	/**
	 * Selects the worst player out of the players in the list. Uses the warcriteria declared in building
	 * of the instance
	 * @return
	 */
	public Player getWorstPlayer(GameData gameData) {
		return this.warCriteria.computeCriteria(gameData);
	}

	/**
	 * Applies the punishment associated to this war level to a player.
	 * @param p
	 */
	public void applyPunishment(Player p, GameData game) throws InterruptedException {
		warPunishment.apply(p, game);
	}

	/**
	 * Create the representation for this war level in a frame with wrapped in a max number of columns
	 */
	public CLIFrame getCLIRepresentation(int columnsLimit) {
		return new CLIFrame(new String[] {
				ANSI.BLACK + warCriteria.getName(),
				ANSI.BLACK + "> " + warPunishment.getDetails() + ANSI.RESET
		}).wrap(columnsLimit, 1, AnchorPoint.LEFT);
	}
}
