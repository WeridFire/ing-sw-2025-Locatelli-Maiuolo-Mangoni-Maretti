package src.main.java.it.polimi.ingsw.network;


import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientUpdate {

	private final UUID clientUUID;
	private final Game currentGame;
	private final List<Game> availableGames;


	public ClientUpdate(UUID clientUUID){
		this.clientUUID = clientUUID;
		this.currentGame = findGameByClientUUID(clientUUID).orElse(null);
		this.availableGames = GamesHandler.getInstance().getGames();
	}

	private Optional<Game> findGameByClientUUID(UUID clientUUID) {
		return GamesHandler.getInstance().getGames()
				.stream()
				.filter(game -> {
					Set<UUID> playerUUIDs = game.getGameData().getPlayers()
							.stream()
							.map(Player::getConnectionUUID)
							.collect(Collectors.toSet());
					return playerUUIDs.contains(clientUUID);
				})
				.findFirst();
	}

	public UUID getClientUUID() {
		return clientUUID;
	}

}
