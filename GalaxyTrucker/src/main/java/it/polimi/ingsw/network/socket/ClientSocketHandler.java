package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.network.*;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.messages.SocketMessage;
import it.polimi.ingsw.player.kpf.KeepPlayerFlyingPredicate;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.Coordinates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ClientSocketHandler implements IClient {

	final GameServer gameServer;
	final BufferedReader input;
	final PrintWriter output;

	/**
	 * This adapter handles all socket connections on the server. It handles both INCOMING MESSAGES (parsing client-made
	 * messages on the server, and then running them) and OUTCOMING MESSAGES (serializing outgoing messages and sending
	 * them to the client). Messages arriving here are simply parsed and forwarded to the RMI on the corresponding
	 * method call.
	 * @param input The input buffer, for the incoming channel
	 * @param output The output buffer, for the outgoing channel.
	 */
	public ClientSocketHandler(BufferedReader input, PrintWriter output) {
		this.gameServer = GameServer.getInstance();
		this.input = input;
		this.output = output;
	}

	/**
	 * Blocking function to receive and parse messages. Based on the message content, it will call the RMI
	 * server to execute as necessary the method. It will also pass a reference to itself to the RMI, so that it will
	 * be able to send messages using sockets by accessing it.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void runVirtualView() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			SocketMessage message = null;
			try{
				byte[] decodedMessage = Base64.getDecoder().decode(line);
				message = SocketMessage.deserialize(decodedMessage);
			}catch(ClassNotFoundException | IOException e){
				System.err.println("Could not deserialize message: " + line);
				e.printStackTrace();
			}
			if(message != null){
				System.out.println("Received new command: " + message.getType() + " args: " + message.getArgs());
				try{
					switch (message.getType()) {
						case PING -> getServer().ping(this);
						case JOIN_GAME -> getServer().joinGame(
								this,
								(UUID) message.getArgs().getFirst(),
								(String) message.getArgs().get(1),
								(MainCabinTile.Color) message.getArgs().get(2)
						);
						case CREATE_GAME -> getServer().createGame(
								this,
								(String) message.getArgs().getFirst(),
								(MainCabinTile.Color) message.getArgs().get(1)
						);
						case UPDATE_SETTINGS -> getServer().updateGameSettings(
								this,
								(GameLevel) message.getArgs().getFirst(),
								(int) message.getArgs().get(1)
						);
						case FLIP_HOUR_GLASS -> getServer().flipHourglass(this);
						case DRAW_TILE -> getServer().drawTile(this);
						case DISCARD_TILE -> getServer().discardTile(this);
						case RESERVE_TILE -> getServer().reserveTile(this);
						case PICK_TILE -> getServer().pickTile(this,
								(Integer) message.getArgs().getFirst());
						case PLACE_TILE -> getServer().placeTile(this,
								(Coordinates) message.getArgs().getFirst(),
								(Rotation) message.getArgs().get(1));
						case FINISH_ASSEMBLING -> getServer().finishAssembling(this, (Integer) message.getArgs().getFirst());
						case SHOW_CARD_GROUP -> getServer().showCardGroup(this, (Integer) message.getArgs().getFirst());
						case HIDE_CARD_GROUP -> getServer().hideCardGroup(this);
						case REQUEST_END_FLIGHT -> getServer().requestEndFlight(this,
								(KeepPlayerFlyingPredicate) message.getArgs().getFirst());
						case PIR_ACTIVATE_TILES -> getServer().pirActivateTiles(this,
								(Set<Coordinates>) message.getArgs().getFirst());
						case PIR_ALLOCATE_REMOVE_LOADABLES -> {
							boolean adding = (Boolean) message.getArgs().get(1);
							if(adding){
								getServer().pirAllocateLoadables(this,
										(Map<Coordinates, List<LoadableType>>) message.getArgs().getFirst());
							}else{
								getServer().pirRemoveLoadables(this,
										(Map<Coordinates, List<LoadableType>>) message.getArgs().getFirst());
							}
						}
						case PIR_SELECT_MULTIPLE_CHOICE -> getServer().pirSelectMultipleChoice(this,
								(Integer) message.getArgs().getFirst());
						case PIR_FORCE_END_TURN -> getServer().pirForceEndTurn(this);
						case CHEAT -> getServer().useCheat(this, (String) message.getArgs().getFirst());
						case RESUME_GAME -> getServer().resumeGame(this, (UUID) message.getArgs().getFirst());
						case QUIT_GAME -> getServer().quitGame(this);
					}
				}catch(IllegalArgumentException e){
					System.err.println("ERROR WHILE PARSING MESSAGE! Message:");
					System.err.println("cmd: " + message.getType() + "args: " + message.getArgs());
					System.err.println("Make sure the arguments are passed in the correct order by the client!");
					e.printStackTrace();
				}
            }

		}
	}

	@Override
	public IServer getServer() {
		//In here we shouldn't worry about this function. It is present only because the class extends IClient, but
		//the server will never actually call getServer() on a ClientSocketToRMIAdapter. If it does, we're fucked :).
		return gameServer.getRmiServer();
	}

	/**
	 * Sends a ClientUpdate over the SOCKET protocol, with serialization.
	 * @param clientUpdate The client update.
	 */
	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		String encodedMessage = Base64.getEncoder().encodeToString(clientUpdate.serialize());
		output.println(encodedMessage);
		output.flush();
	}
}
