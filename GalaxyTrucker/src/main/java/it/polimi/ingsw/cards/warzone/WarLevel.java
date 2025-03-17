package src.main.java.it.polimi.ingsw.cards.warzone;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.player.Player;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class WarLevel {

	private WarCriteria warCriteria;
	private Consumer<Player> punishmentFunction;

	public WarLevel(WarCriteria warCriteria, Consumer<Player> punishmentFunction) {
		this.warCriteria = warCriteria;
		this.punishmentFunction = punishmentFunction;
	}

	public Player getWorstPlayer(UUID gameId) {
		return GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()
				.stream().min(warCriteria.getComparator()).orElse(null);
	}

	public void applyPunishment(Player p) {
		punishmentFunction.accept(p);
	}
}
