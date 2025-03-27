package src.main.java.it.polimi.ingsw.client.socket;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.Game;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

public class RmiJunction {

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final GamesHandler gamesHandler;

	public RmiJunction() {
		this.gamesHandler = GamesHandler.getInstance();
	}

	/**
	 * All websockets message should be sent through this. It will parse the message (we can't use reflection, so this
	 * is the only way), and executes the method on a new thread, exactly the same way RMI would.
	 * @param message The message received from the websocket.
	 * @param playerSocket The websocket sending the message.
	 */
	public void processSocket(SocketMessage message, PlayerSocketHandler playerSocket) {
		String type = message.getType();
		List<String> args = message.getArgs();
		int argsAmount = message.getArgsAmount();

		switch (type) {
			case "getGames":
				executor.submit(() -> {
					if(argsAmount != 0){
						playerSocket.reportError("Invalid arguments.");
					}
					//TODO: Send to player list of games.
				});
				break;
			case "joinGame":
				executor.submit(() -> {
					if(argsAmount != 2){
						playerSocket.reportError("Invalid arguments amount.");
					}
					String username = deserialize(args.get(0), String.class);
					UUID lobbyUUID = deserialize(args.get(1), UUID.class);
					if(username == null || lobbyUUID == null){
						playerSocket.reportError("Invalid arguments type.");
					}
					Game joinedGame = gamesHandler.joinGame(username, lobbyUUID);
					//TODO: send result of game join to player.
				});
				break;
			default:
				executor.submit(() -> playerSocket.reportError("Unknown message type."));
		}
	}

	private <T> T deserialize(String data, Class<T> clazz) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
			 ObjectInputStream ois = new ObjectInputStream(bis)) {
			return clazz.cast(ois.readObject());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}