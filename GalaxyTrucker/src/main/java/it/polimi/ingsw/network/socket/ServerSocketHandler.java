package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.network.messages.SocketMessage;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.util.Coordinates;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.*;

public class ServerSocketHandler implements IServer {
	final PrintWriter output;

	/**
	 * This class is used to convert methods calls into socket messages. It allows the client to send outwards messages just
	 * as if they were using RMI. The only con is that no response will be passed in here, but instead a message should
	 * cause a status update for the whole client.
	 * @param output the output buffer to write messages to.
	 */
	public ServerSocketHandler(BufferedWriter output) {
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
		SocketMessage mess = SocketMessage.createGameMessage(username);
		sendSocketMessage(mess);
	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username) {
		SocketMessage mess = SocketMessage.joinGameMessage(gameId, username);
		sendSocketMessage(mess);
	}

	@Override
	public void quitGame(IClient client) {
		//TODO quit game socket function
	}

	@Override
	public void ping(IClient client) {
		SocketMessage mess = SocketMessage.pingMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void activateTiles(IClient client, Set<Coordinates> tilesToActivate) {
		//TODO activate tiles socket function
	}

	@Override
	public void allocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException {
		//TODO allocate loadables socket function
	}


	@Override
	public void forceEndTurn(IClient client) {
		//TODO force end turn socket function
	}

	@Override
	public void updateGameSettings(IClient client, GameLevel level, int minPlayers) throws RemoteException {
		SocketMessage mess = SocketMessage.updateSettingsMessage(level, minPlayers);
		sendSocketMessage(mess);
	}

	// ASSEMBLE PHASE

	@Override
	public void flipHourglass(IClient client) {
		SocketMessage mess = SocketMessage.flipHourglassMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void drawTile(IClient client) {
		SocketMessage mess = SocketMessage.drawTileMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void discardTile(IClient client) throws RemoteException {
		SocketMessage mess = SocketMessage.discardTileMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void reserveTile(IClient client) throws RemoteException {
		SocketMessage mess = SocketMessage.reserveTileMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void pickTile(IClient client, Integer id) throws RemoteException {
		SocketMessage mess = SocketMessage.pickTileMessage(id);
		sendSocketMessage(mess);
	}

	@Override
	public void placeTile(IClient client, Coordinates coordinates, Rotation rotation) throws RemoteException {
		sendSocketMessage(SocketMessage.placeMessage(coordinates, rotation));
	}

	@Override
	public void finishAssembling(IClient client) throws RemoteException {
		SocketMessage mess = SocketMessage.finishAssemblingMessage();
		sendSocketMessage(mess);
	}
}
