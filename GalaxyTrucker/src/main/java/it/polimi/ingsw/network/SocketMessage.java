package src.main.java.it.polimi.ingsw.network;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SocketMessage implements Serializable{
	// Enum to define message types
	public enum MessageType {
		PING,
		JOIN_GAME,
		CREATE_GAME,
		// Add other message types as necessary
	}

	private MessageType type;
	private List<Object> args;

	// Constructor
	public SocketMessage(MessageType type, List<Object> args) {
		this.type = type;
		this.args = args;
	}

	public SocketMessage(MessageType type) {
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


	/*
	public static SocketMessage createGameMessage(String username){
		return new SocketMessage(MessageType.CREATE_GAME, List.of(username));
	}

	 */
}
