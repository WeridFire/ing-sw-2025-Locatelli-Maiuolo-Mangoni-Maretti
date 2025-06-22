package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.network.messages.SocketMessage;
import it.polimi.ingsw.player.kpf.KeepPlayerFlyingPredicate;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
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
		// A socket is always connected on creation, so we shouldn't worry about implementing this.
		// Just for consistency, this method should send a getUpdate request to the server (ping),
		// so that it can verify the connection.
		// However, noting that THE CONNECTION IS ALREADY PRESENT and that ping would another time the starting screen,
		// this method does NOTHING.

		// ping(client);
	}

	@Override
	public void createGame(IClient client, String username, MainCabinTile.Color desiredColor) {
		SocketMessage mess = SocketMessage.createGameMessage(username, desiredColor);
		sendSocketMessage(mess);
	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username, MainCabinTile.Color desiredColor) {
		SocketMessage mess = SocketMessage.joinGameMessage(gameId, username, desiredColor);
		sendSocketMessage(mess);
	}

	@Override
	public void quitGame(IClient client) {
		SocketMessage mess = SocketMessage.quitGameMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void ping(IClient client) {
		SocketMessage mess = SocketMessage.pingMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void pirActivateTiles(IClient client, Set<Coordinates> tilesToActivate) {
		SocketMessage mess = SocketMessage.pirActivateTilesMessage(tilesToActivate);
		sendSocketMessage(mess);
	}

	@Override
	public void pirAllocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) {
		SocketMessage mess = SocketMessage.pirAllocateRemoveLoadables(cargoToAdd, true);
		sendSocketMessage(mess);
	}

	@Override
	public void pirRemoveLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToRemove) {
		SocketMessage mess = SocketMessage.pirAllocateRemoveLoadables(cargoToRemove, false);
		sendSocketMessage(mess);
	}

	@Override
	public void pirRearrangeLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToRearrange) {
		SocketMessage mess = SocketMessage.pirRearrangeLoadables(cargoToRearrange);
		sendSocketMessage(mess);
	}

	@Override
	public void pirForceEndTurn(IClient client) {
		SocketMessage mess = SocketMessage.pirForceEndTurn();
		sendSocketMessage(mess);
	}

	@Override
	public void pirSelectMultipleChoice(IClient client, int selection) {
		SocketMessage mess = SocketMessage.pirSelectMultipleChoice(selection);
		sendSocketMessage(mess);
	}

	@Override
	public void updateGameSettings(IClient client, GameLevel level, int minPlayers) {
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
	public void discardTile(IClient client) {
		SocketMessage mess = SocketMessage.discardTileMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void reserveTile(IClient client) {
		SocketMessage mess = SocketMessage.reserveTileMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void pickTile(IClient client, Integer id) {
		SocketMessage mess = SocketMessage.pickTileMessage(id);
		sendSocketMessage(mess);
	}

	@Override
	public void placeTile(IClient client, Coordinates coordinates, Rotation rotation) {
		sendSocketMessage(SocketMessage.placeMessage(coordinates, rotation));
	}

	@Override
	public void finishAssembling(IClient client, Integer preferredPosition) {
		SocketMessage mess = SocketMessage.finishAssemblingMessage(preferredPosition);
		sendSocketMessage(mess);
	}

	@Override
	public void showCardGroup(IClient client, Integer id) {
		SocketMessage mess =SocketMessage.showCardGroupMessage(id);
		sendSocketMessage(mess);
	}

	@Override
	public void hideCardGroup(IClient client) {
		SocketMessage mess = SocketMessage.hideCardGroupMessage();
		sendSocketMessage(mess);
	}

	@Override
	public void requestEndFlight(IClient client, KeepPlayerFlyingPredicate saveFromEndFlight) {
		sendSocketMessage(SocketMessage.endFlightMessage(saveFromEndFlight));
	}

	@Override
	public void spectatePlayerShipboard(IClient client, String username) throws RemoteException {
		sendSocketMessage(SocketMessage.spectatePlayerShipboardMessage(username));
	}

	@Override
	public void useCheat(IClient client, String cheatName) {
		SocketMessage mess = SocketMessage.cheatMessage(cheatName);
		sendSocketMessage(mess);
	}

	@Override
	public void resumeGame(IClient client, UUID gameId) {
		SocketMessage mess = SocketMessage.resumeGameMessage(gameId);
		sendSocketMessage(mess);
	}
}
