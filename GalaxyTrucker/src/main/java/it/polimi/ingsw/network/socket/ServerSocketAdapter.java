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
	 * This class is used to convert methods calls into socket messages. It allows the client to send outwards messages just
	 * as if they were using RMI. The only con is that no response will be passed in here, but instead a message should
	 * cause a status update for the whole client.
	 * @param output the output buffer to write messages to.
	 */
	public ServerSocketAdapter(BufferedWriter output) {
		this.output = new PrintWriter(output);
	}

	@Override
	public void connect(IClient client) {
		//A socket is always connected on creation, so we shouldn't worry about implementing this. Just for consistency,
		//this method will send a getUpdate request to the server, so that it can verify the connection. However note
		//that THE CONNECTION IS ALREADY PRESENT!
		requestUpdate(client);
	}

	@Override
	public void requestUpdate(IClient client) {
		output.println("getUpdate");
	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username) {
		output.println("joinGame|gameId|username"); //TODO: this has to be reviewed definetely
	}
}
