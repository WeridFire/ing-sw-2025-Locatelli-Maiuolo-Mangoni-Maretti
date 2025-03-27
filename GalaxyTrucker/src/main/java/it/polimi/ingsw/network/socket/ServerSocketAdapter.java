package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class ServerSocketAdapter implements IServer {
	final PrintWriter output;

	/**
	 * This class is used to convert methods into socket messages. It allows the client to send outwards messages just
	 * as if they were using RMI. The only con is that no response will be passed in here, but instead a message should
	 * cause a status update for the whole client.
	 * @param output the output buffer to write messages to.
	 */
	public ServerSocketAdapter(BufferedWriter output) {
		this.output = new PrintWriter(output);
	}

	@Override
	public void connect(IClient client) {

	}

	@Override
	public Set<Game> getGames() {
		output.println("getGames");
		return null;
	}

	@Override
	public Game joinGame(UUID gameId, String username) {
		output.println("joinGame|gameId|username"); //TODO: this has to be reviewed definetely
		return null;
	}
}
