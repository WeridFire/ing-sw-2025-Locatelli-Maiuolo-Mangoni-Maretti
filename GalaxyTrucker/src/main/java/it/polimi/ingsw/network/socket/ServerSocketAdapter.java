package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;
import src.main.java.it.polimi.ingsw.network.SocketMessage;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Base64;
import java.util.List;
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

	/**
	 * Serializes the messages to base64 and sends it over socket.
	 * @param sm The SocketMessage to send.
	 */
	private void sendSocketMessage(SocketMessage sm){
		String serialized = Base64.getEncoder().encodeToString(sm.serialize());
		output.println(serialized);
		output.flush();
	}

	@Override
	public void connect(IClient client) {
		//A socket is always connected on creation, so we shouldn't worry about implementing this. Just for consistency,
		//this method will send a getUpdate request to the server, so that it can verify the connection. However note
		//that THE CONNECTION IS ALREADY PRESENT!
		ping(client);
	}

	@Override
	public void createGame(IClient client, String username) throws RemoteException {
		SocketMessage mess = new SocketMessage(SocketMessage.MessageType.CREATE_GAME, List.of(username));
		sendSocketMessage(mess);
	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username) {
		SocketMessage mess = new SocketMessage(SocketMessage.MessageType.JOIN_GAME, List.of(gameId, username));
		sendSocketMessage(mess);
	}

	@Override
	public void quitGame(IClient client) {
		//TODO
	}

	@Override
	public void drawComponent(IClient client) {

	}

	@Override
	public void ping(IClient client) {
		SocketMessage mess = new SocketMessage(SocketMessage.MessageType.PING);
		sendSocketMessage(mess);
	}

	@Override
	public void activateTiles(IClient client, Set<Coordinates> tilesToActivate) {

	}

	@Override
	public void allocateLoadable(IClient client, LoadableType loadable, Coordinates location) {

	}

	@Override
	public void forceEndTurn(IClient client) {

	}
}
