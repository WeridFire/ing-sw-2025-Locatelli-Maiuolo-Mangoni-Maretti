package src.main.java.it.polimi.ingsw.network;


import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Deck;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientUpdate implements Serializable {

	private final UUID clientUUID;
	private final GameData currentGame;
	private final List<GameData> availableGames;
	private String error;

	/**
	 * A ClientUpdate is the type of message the server sends to the client. It sends the client all the information about
	 * the client is currently in (which may be null), and also the information about available games, in lobby phase.
	 * Based on the client the server needs to update, this class will automatically build the correct message for the
	 * client.
	 * @param clientUUID The UUID of the client the message is directed to.
	 */
	public ClientUpdate(UUID clientUUID){
		this.clientUUID = clientUUID;
		GamesHandler gamesHandler = GamesHandler.getInstance();
		Game game = gamesHandler.findGameByClientUUID(clientUUID);
		if(game != null){
			Player player = gamesHandler.getPlayerByConnection(clientUUID);
			this.currentGame = obfuscateGame(game.getGameData(), player);
		}else{
			currentGame = null;
		}
		this.availableGames = GamesHandler.getInstance().getGames().stream().map(Game::getGameData
		).collect(Collectors.toList());
	}

	/**
	 * A ClientUpdate is the type of message the server sends to the client. It sends the client all the information about
	 * the client is currently in (which may be null), and also the information about available games, in lobby phase.
	 * Based on the client the server needs to update, this class will automatically build the correct message for the
	 * client.
	 * @param clientUUID The client the message is directed to.
	 * @param error The error to display on the client.
	 */
	public ClientUpdate(UUID clientUUID, String error){
		this(clientUUID);
		this.error = error;
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
			if(clone.getCurrentGamePhaseType() == GamePhaseType.LOBBY){
				return clone;
			}
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

	public List<GameData> getAvailableGames() {
		return availableGames;
	}


	/**
	 * Serializes the object instance into an UTF-8 encoded string.
	 * @return
	 */
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

	/**
	 * Creates an instance based on a serialized string, encoded in UTF-8.
	 * @param serialized The serialized string
	 * @return The deserialized instance.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static ClientUpdate deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
		ObjectInputStream in = new ObjectInputStream(bis);
		return (ClientUpdate) in.readObject();
	}

	public String getError() {
		return error;
	}
}
