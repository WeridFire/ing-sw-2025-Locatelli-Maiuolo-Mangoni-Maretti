package src.main.java.it.polimi.ingsw.client.rmi;

import java.io.Serializable;

public class ServerUpdate implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Enum with different types of updates that the server might need to actively send to the client.  For example when
	 * a new game phase starts, or when something "passive" on the player happens.
	 * This logic is not yet "split" among gamehandler or single game, but more of a playground / testing.
	 */
	public enum UpdateType {
		GAME_START, GAME_PHASE_X, GAME_RESET
	}

	private final long timestamp;
	private final UpdateType updateType;

	public ServerUpdate(UpdateType updateType) {
		this.timestamp = System.currentTimeMillis() / 1000; // Epoch time in seconds
		this.updateType = updateType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	@Override
	public String toString() {
		return "ServerUpdate{" +
				"timestamp=" + timestamp +
				", updateType=" + updateType +
				'}';
	}
}
