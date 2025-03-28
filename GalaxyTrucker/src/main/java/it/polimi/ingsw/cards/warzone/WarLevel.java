package src.main.java.it.polimi.ingsw.cards.warzone;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WarLevel {

	/**
	 * The criteria used to select the worst player in this level.
	 */
	private final WarCriteria warCriteria;

	/**
	 * The function applied to the worst player, the punishment.
	 */
	private final BiConsumer<Player, GameData> punishmentFunction;

	/**
	 * Instances a war level. Multiple war levels build up to a war zone.
	 * @param warCriteria Use a warfactory to generate this. A criteria to decide the worst player to punish.
	 * @param punishmentFunction Use a warfactory to generate this. The punishment function to
	 *                              apply to the selected player
	 */
	public WarLevel(WarCriteria warCriteria, BiConsumer<Player, GameData> punishmentFunction) {
		this.warCriteria = warCriteria;
		this.punishmentFunction = punishmentFunction;
	}

	/**
	 * Selects the worst player out of the players in the game instance. Uses the warcriteria declared in building
	 * of the instance
	 * @return
	 */
	public Player getWorstPlayer(GameData game) {
		return game.getPlayers()
				.stream().min(warCriteria.getComparator()).orElse(null);
	}

	/**
	 * Applies the punishment associated to this war level to a player.
	 * @param p
	 */
	public void applyPunishment(Player p, GameData game) {
		punishmentFunction.accept(p, game);
	}
}
