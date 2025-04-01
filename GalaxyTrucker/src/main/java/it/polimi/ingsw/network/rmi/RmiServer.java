package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.exceptions.GameNotFoundException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PlayerTurnType;
import it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RmiServer implements IServer {

	final GamesHandler gamesHandler;
	final GameServer gameServer;

	/**
	 * Creates a RMI server. All the methods call on this will affect the actual model. This is the junction between
	 * normal RMI and SOCKET connections.
	 */
	public RmiServer() {
		this.gamesHandler = GamesHandler.getInstance();
		this.gameServer = GameServer.getInstance();
	}

	@Override
	public void connect(IClient client) throws RemoteException {
		UUID clientUUID = gameServer.registerClient(client);
		//Confirm connection and notify the assigned UUID.
		client.updateClient(new ClientUpdate(clientUUID));
	}

	@Override
	public void createGame(IClient client, String username) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		try {
			Game g = gamesHandler.createGame(username, connectionUUID);
			System.out.println("Created new game: " + g.getId());
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (PlayerAlreadyInGameException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}

	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		try {
			gamesHandler.addPlayerToGame(username, gameId, connectionUUID);
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (PlayerAlreadyInGameException | GameNotFoundException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void quitGame(IClient client) throws RemoteException {
		//assume we left the game
		client.updateClient(new ClientUpdate(gameServer.getUUIDbyConnection(client)));
	}

	@Override
	public void drawComponent(IClient client) {
		/*
		Game playerGame = gamesHandler.getGame("uuid del game");
		if(playerGame != null){
			// do nothing
		}
		//playerGame.getGameData().getPlayers().drawComponent(playerGame);
		client.updateClient(new ClientUpdate(gameServer.getUUIDbyConnection(client)));
		*/
	}

	@Override
	public void ping(IClient client) throws RemoteException {
		client.updateClient(new ClientUpdate(gameServer.getUUIDbyConnection(client)));
	}

	@Override
	public void activateTiles(IClient client, Set<Coordinates> tilesToActivate) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);
		//Check that we have a reference to the game & player
		if(player == null || game == null) {
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}
		//Actually try to perform the action
		try {
			game.getGameData().getCurrentPlayerTurn().activateTiles(player, tilesToActivate);
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (WrongPlayerTurnException | InputNotSupportedException | NotEnoughItemsException |
				 TileNotAvailableException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}


	}

	@Override
	public void allocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);
		if(player == null || game == null){
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}

		try {
			game.getGameData().getCurrentPlayerTurn().addLoadables(player, cargoToAdd);
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (InputNotSupportedException | WrongPlayerTurnException | TileNotAvailableException |
				 NotEnoughItemsException | UnsupportedLoadableItemException | TooMuchLoadException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void forceEndTurn(IClient client) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);
		if(player == null || game == null){
			//TODO: player or game not found.
			return;
		}
		if(!game.getGameData().getCurrentPlayerTurn().getCurrentPlayer().equals(player)){
			//TODO: it is not the player's turn.
			return;
		}
		game.getGameData().getCurrentPlayerTurn().endTurn();
		client.updateClient(new ClientUpdate(connectionUUID));
	}


}
