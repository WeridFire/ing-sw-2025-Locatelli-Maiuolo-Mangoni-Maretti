package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.exceptions.DrawTileException;
import it.polimi.ingsw.game.exceptions.GameNotFoundException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.player.exceptions.AlreadyHaveTileInHandException;
import it.polimi.ingsw.player.exceptions.NoTileInHandException;
import it.polimi.ingsw.player.exceptions.TooManyReservedTilesException;
import it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.exceptions.ThatTileIdDoesNotExistsException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.*;

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
			Game result = gamesHandler.addPlayerToGame(username, gameId, connectionUUID);
			if(result != null){
				//notify everyone that a new player has joined -> refreshes their view
				GameServer.getInstance().broadcastUpdate(result);
			}
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
			game.getGameData().getPIRHandler().getActivateTiles().activateTiles(player, tilesToActivate);
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
			game.getGameData().getPIRHandler().getAddLoadables().addLoadables(player, cargoToAdd);
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (InputNotSupportedException | WrongPlayerTurnException | TileNotAvailableException |
				 UnsupportedLoadableItemException | TooMuchLoadException e) {
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
		try {
			game.getGameData().getPIRHandler().endTurn(player);
		} catch (WrongPlayerTurnException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}
		client.updateClient(new ClientUpdate(connectionUUID));
	}

	@Override
	public void updateGameSettings(IClient client, GameLevel level, int minPlayers) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);
		if(game == null || player == null){
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}
		if(!Objects.equals(game.getGameData().getGameLeader(), player.getUsername())){
			client.updateClient(new ClientUpdate(connectionUUID, "You must be the game leader to perform this."));
			return;
		}

		game.getGameData().setLevel(level);
		game.getGameData().setRequiredPlayers(minPlayers);
		GameServer.getInstance().broadcastUpdate(game);
	}

	// ASSEMBLE PHASE

	@Override
	public void flipHourglass(IClient client) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		if(game == null | player == null){
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}

		if (game.getGameData().getCurrentGamePhaseType().equals(GamePhaseType.ASSEMBLE)){
			try {
				game.getGameData().getCurrentGamePhase().startTimer(player);
			} catch (TimerIsAlreadyRunningException | CommandNotAllowedException e) {
				client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
				return;
			}
		}
		GameServer.getInstance().broadcastUpdate(game);
	}

	@Override
	public void drawTile(IClient client) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);

		if(player == null || game == null){
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}

		try {
			player.drawTile(game.getGameData());
		} catch (DrawTileException | AlreadyHaveTileInHandException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
			return;
		}

		GameServer.getInstance().broadcastUpdate(game);
	}

	@Override
	public void discardTile(IClient client) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);

		if (player == null || game == null) {
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}

		try {
			player.discardTile(game.getGameData());
		} catch (NoTileInHandException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}

		GameServer.getInstance().broadcastUpdate(game);
	}

	@Override
	public void reserveTile(IClient client) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);

		if (player == null || game == null) {
			client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
			return;
		}

		try {
			player.setReservedTiles(player.getTileInHand());
			player.discardTile(game.getGameData());
		}catch (NoTileInHandException | TooManyReservedTilesException e){
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}

		GameServer.getInstance().broadcastUpdate(game);

	}

	@Override
	public void pickTile(IClient client, Integer id) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		Player player = gamesHandler.getPlayerByConnection(connectionUUID);
		Game game = gamesHandler.findGameByClientUUID(connectionUUID);

		try{
			player.pickTile(game.getGameData(), id);
		} catch (AlreadyHaveTileInHandException | ThatTileIdDoesNotExistsException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
        }

		GameServer.getInstance().broadcastUpdate(game);
    }
}
