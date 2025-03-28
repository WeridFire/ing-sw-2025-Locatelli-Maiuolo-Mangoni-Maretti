package src.main.java.it.polimi.ingsw.network;


import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Deck;
import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientUpdate implements Serializable {

	private final UUID clientUUID;
	private final GameData currentGame;
	private final List<Game> availableGames;

	public ClientUpdate(UUID clientUUID){
		this.clientUUID = clientUUID;
		Game game = findGameByClientUUID(clientUUID).orElse(null);
		if(game != null){
			this.currentGame = obfuscateGame(game.getGameData(), game.getPlayerByConnection(clientUUID));
		}else{
			currentGame = null;
		}
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

	private GameData obfuscateGame(GameData game, Player target){
		try {
			// Write object to a byte stream
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(game);
			out.flush();

			// Read object from byte stream
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream in = new ObjectInputStream(bis);
			GameData clone = (GameData) in.readObject();

			//Check for performance issues, since this will be executed ALOT! In case this is heavy performance wise
			clone.setCoveredTiles(null);
			clone.setDeck(Deck.obfuscateDeck(clone.getDeck(), target));

			return clone;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Error during deep copy", e);
		}
	}

	public GameData getCurrentGame() {
		return currentGame;
	}

	public List<Game> getAvailableGames() {
		return availableGames;
	}

	public byte[] serialize() {
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(this);
			out.flush();
			return bos.toByteArray();
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;

	}

	public static ClientUpdate deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
		ObjectInputStream in = new ObjectInputStream(bis);
		return (ClientUpdate) in.readObject();
	}
}
