package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.io.Serializable;
import java.util.function.Consumer;

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
	 * Selects the worst player out of the players in the game instance. Uses the warcriteria declared in building
	 * of the instance
	 * @return
	 */
	public void getWorstPlayer(GameData game, Consumer<Player> onSelected) {
		warCriteria.computeCriteria(game, onSelected);
	}

	/**
	 * Applies the punishment associated to this war level to a player.
	 * @param p
	 */
	public void applyPunishment(Player p, GameData game, Consumer<Player> onFinish) {
		warPunishment.apply(p, game, onFinish);
	}
}
