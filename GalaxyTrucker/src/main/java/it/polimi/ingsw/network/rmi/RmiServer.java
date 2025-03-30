package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.game.exceptions.GameNotFoundException;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameServer;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.PlayerTurnType;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.ContainerTile;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
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
		if(player == null || game == null) {
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}

		if(!game.getGameData().getCurrentPlayerTurn().getCurrentPlayer().equals(player)){
			client.updateClient(new ClientUpdate(connectionUUID, "It's not your turn."));
			return;
		}
		if(game.getGameData().getCurrentPlayerTurn().getPlayerTurnType() != PlayerTurnType.ACTIVATE_TILE){
			client.updateClient(new ClientUpdate(connectionUUID, "This action is not supported in this turn."));
			return;
		}
		if(!game.getGameData().getCurrentPlayerTurn().getHighlightMask().containsAll(tilesToActivate)){
			client.updateClient(new ClientUpdate(connectionUUID, "This tile is not activable in this turn."));
			return;
		}
		try {
			player.getShipBoard().activateTiles(tilesToActivate);
			game.getGameData().getCurrentPlayerTurn().checkForResult();
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (NotEnoughItemsException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}

	}

	@Override
	public void allocateLoadable(IClient client, LoadableType loadable, Coordinates location) throws RemoteException {
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
		if(game.getGameData().getCurrentPlayerTurn().getPlayerTurnType() != PlayerTurnType.ADD_CARGO){
			//TODO: The turn does not expect this type of action.
			return;
		}
		if(!game.getGameData().getCurrentPlayerTurn().getHighlightMask().contains(location)){
			//TODO: The target coordinate can not hold this type of cargo.
			return;
		}

		if(!player.getShipBoard().getFloatingLoadables().contains(loadable)){
			//TODO: This cargo is not available for allocation.
			return;
		}
		player.getShipBoard().getFloatingLoadables().remove(loadable);
		ContainerTile targetContainer = player.getShipBoard()
												.getVisitorCalculateCargoInfo()
												.getInfoAllContainers()
												.getLocations()
												.get(location);
		try {
			targetContainer.loadItems(loadable, 1);
			if(player.getShipBoard().getFloatingLoadables().isEmpty()){
				//We auto end the turn in case all the items were allocated.
				game.getGameData().getCurrentPlayerTurn().checkForResult();;
			}
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (TooMuchLoadException e) {
			//TODO: notify that the container is full and therefore can't handle it.
		} catch (UnsupportedLoadableItemException e) {
			//TODO: notify that the type is not supported.
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
		game.getGameData().getCurrentPlayerTurn().checkForResult();
		client.updateClient(new ClientUpdate(connectionUUID));
	}


}
