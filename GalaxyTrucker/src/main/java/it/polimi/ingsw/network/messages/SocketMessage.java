package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.util.Coordinates;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class SocketMessage implements Serializable{
	// Enum to define message types
	public enum MessageType {
		PING,
		JOIN_GAME,
		CREATE_GAME,
		UPDATE_SETTINGS,
		FLIP_HOUR_GLASS,
		DRAW_TILE,
		DISCARD_TILE,
		RESERVE_TILE,
		PICK_TILE,
		PLACE_TILE,
		FINISH_ASSEMBLING,
		SHOW_CARD_GROUP
	}

	private MessageType type;
	private List<Object> args;

	/**
	 * SocketMessage is the type of message sent by client to server via SOCKET. It represents a command the player
	 * is trying to execute on the remote server. It holds information about the command to call, and the parameters
	 * to pass into the command.
	 * @param type The type of command to execute
	 * @param args The arguments, in a list.
	 */
	private SocketMessage(MessageType type, List<Object> args) {
		this.type = type;
		this.args = args;
	}

	/**
	 * SocketMessage is the type of message sent by client to server via SOCKET. It represents a command the player
	 * is trying to execute on the remote server. It holds information about the command to call, and the parameters
	 * to pass into the command.
	 * @param type The type of command to execute
	 */
	private SocketMessage(MessageType type) {
		this.type = type;
		this.args = null;
	}

	// Getter methods
	public MessageType getType() {
		return type;
	}

	public List<Object> getArgs() {
		return args;
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
	public static SocketMessage deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
		ObjectInputStream in = new ObjectInputStream(bis);
		return (SocketMessage) in.readObject();
	}


	/**
	 * Creates a socket message to tell the server to create a game and join it, using the specified username.
	 * @param username The username to use
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage createGameMessage(String username){
		return new SocketMessage(MessageType.CREATE_GAME, List.of(username));
	}

	/**
	 * Creates a socket message to tell the server to join a game identified by ID using a specified username
	 * @param username The username to use
	 * @param gameId The game ID to join
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage joinGameMessage(UUID gameId, String username){
		return new SocketMessage(MessageType.JOIN_GAME, List.of(gameId, username));
	}

	/**
	 * Creates a socket message to tell the server to update a games settings with the new values.
	 * @param gameLevel the new gameLevel.
	 * @param minPlayers the minimum players for the game to start.
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage updateSettingsMessage(GameLevel gameLevel, int minPlayers){
		return new SocketMessage(MessageType.UPDATE_SETTINGS, List.of(gameLevel, minPlayers));
	}

	/**
	 * Creates a socket message to ping the server.
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage pingMessage(){
		return new SocketMessage(MessageType.PING);
	}

	/**
	 * Creates a socket message to tell the server to flip the timer.
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage flipHourglassMessage(){
		return new SocketMessage(MessageType.FLIP_HOUR_GLASS);
	}

	/**
	 * Creates a socket message to tell the server to draw a tile.
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage drawTileMessage(){
		return new SocketMessage(MessageType.DRAW_TILE);
	}

	/**
	 * Creates a socket message to tell the server to discard the tile in hand.
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage discardTileMessage(){
		return new SocketMessage(MessageType.DISCARD_TILE);
	}

	/**
	 * Creates a socket message to tell the server to reserve the tile in hand.
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage reserveTileMessage(){
		return new SocketMessage(MessageType.RESERVE_TILE);
	}

	/**
	 * Creates a socket message to tell the server to pick the tile with a specific id.
	 *
	 * @param id : id of the tile to pick
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage pickTileMessage(Integer id){
		return new SocketMessage(MessageType.PICK_TILE, List.of(id));
	}

	/**
	 * Creates a socket message to tell the server to place the tile in hand onto the shipboard in the specified
	 * coordinates
	 *
	 * @param coordinates destination coordinates for the tile onto the shipboard
	 * @param rotation rotation to apply to the tile before placing it onto the shipboard
	 * @return The socket message containing the desired information.
	 */
	public static SocketMessage placeMessage(Coordinates coordinates, Rotation rotation){
		return new SocketMessage(MessageType.PLACE_TILE, List.of(coordinates, rotation));
	}

	public static SocketMessage finishAssemblingMessage(){

		return new SocketMessage(MessageType.FINISH_ASSEMBLING);
	}

	public static SocketMessage showCardGroupMessage(Integer id) {
		return new SocketMessage(SocketMessage.MessageType.SHOW_CARD_GROUP, List.of(id));
	}

}
