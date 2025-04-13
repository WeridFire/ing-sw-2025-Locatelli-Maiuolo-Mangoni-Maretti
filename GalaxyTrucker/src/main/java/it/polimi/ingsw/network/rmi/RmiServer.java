package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
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
import it.polimi.ingsw.player.exceptions.*;
import it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.exceptions.*;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.*;

public class RmiServer implements IServer {

	private static class PlayerGameInstance {
		UUID connectionUUID;
		Player player;
		Game game;
		private PlayerGameInstance(UUID connectionUUID, Player player, Game game) {
			this.connectionUUID = connectionUUID;
			this.player = player;
			this.game = game;
		}
		static PlayerGameInstance validateClient(GamesHandler gamesHandler, GameServer gameServer,
												 IClient client) throws RemoteException {
			UUID connectionUUID = gameServer.getUUIDbyConnection(client);
			Player player = gamesHandler.getPlayerByConnection(connectionUUID);
			Game game = gamesHandler.findGameByClientUUID(connectionUUID);
			//Check that we have a reference to the game & player
			if (player == null || game == null) {
				client.updateClient(new ClientUpdate(connectionUUID, "You are not in a game."));
				return null;
			}
			return new PlayerGameInstance(connectionUUID, player, game);
		}
		static PlayerGameInstance validateClient(GamesHandler gamesHandler, GameServer gameServer,
												 IClient client, GamePhaseType forceCurrentGamePhase) throws RemoteException {
			PlayerGameInstance result = validateClient(gamesHandler, gameServer, client);
			if (result == null) return null;
			GamePhaseType currentGamePhase = result.game.getGameData().getCurrentGamePhaseType();
			if (currentGamePhase != forceCurrentGamePhase) {
				client.updateClient(new ClientUpdate(result.connectionUUID, "You cannot perform this action in the "
						+ currentGamePhase + " game phase. Required game phase: " + forceCurrentGamePhase));
				return null;
			}
			return result;
		}
	}

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
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.game.getGameData().getPIRHandler().getActivateTiles().activateTiles(pg.player, tilesToActivate);
			client.updateClient(new ClientUpdate(pg.connectionUUID));
		} catch (WrongPlayerTurnException | InputNotSupportedException | NotEnoughItemsException |
				 TileNotAvailableException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void allocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.game.getGameData().getPIRHandler().getAddLoadables().addLoadables(pg.player, cargoToAdd);
			client.updateClient(new ClientUpdate(pg.connectionUUID));
		} catch (InputNotSupportedException | WrongPlayerTurnException | TileNotAvailableException |
				 UnsupportedLoadableItemException | TooMuchLoadException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void forceEndTurn(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.game.getGameData().getPIRHandler().endTurn(pg.player);
		} catch (WrongPlayerTurnException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
		client.updateClient(new ClientUpdate(pg.connectionUUID));
	}

	// LOBBY PHASE

	@Override
	public void updateGameSettings(IClient client, GameLevel level, int minPlayers) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.LOBBY);
		if (pg == null) return;
		// else: actually try to perform the action

		if(!Objects.equals(pg.game.getGameData().getGameLeader(), pg.player.getUsername())){
			client.updateClient(new ClientUpdate(pg.connectionUUID, "You must be the game leader to perform this."));
			return;
		}

		pg.game.getGameData().setLevel(level);
		pg.game.getGameData().setRequiredPlayers(minPlayers);
		GameServer.getInstance().broadcastUpdate(pg.game);
	}

	// ASSEMBLE PHASE

	@Override
	public void flipHourglass(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.game.getGameData().getCurrentGamePhase().startTimer(pg.player);
		} catch (TimerIsAlreadyRunningException | CommandNotAllowedException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}
		GameServer.getInstance().broadcastUpdate(pg.game);
	}

	@Override
	public void drawTile(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.player.drawTile(pg.game.getGameData());
		} catch (DrawTileException | AlreadyHaveTileInHandException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}

		GameServer.getInstance().broadcastUpdate(pg.game);
	}

	@Override
	public void discardTile(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.player.discardTile(pg.game.getGameData());
		} catch (NoTileInHandException | ReservedTileException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}

        GameServer.getInstance().broadcastUpdate(pg.game);
	}

	@Override
	public void reserveTile(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.player.reserveTile();
        }catch (NoTileInHandException | TooManyReservedTilesException e){
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}

		GameServer.getInstance().broadcastUpdate(pg.game);
	}

	@Override
	public void pickTile(IClient client, Integer id) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try{
			pg.player.pickTile(pg.game.getGameData(), id);
		} catch (AlreadyHaveTileInHandException | ThatTileIdDoesNotExistsException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
        }

		GameServer.getInstance().broadcastUpdate(pg.game);
    }

	@Override
	public void placeTile(IClient client, Coordinates coordinates, Rotation rotation) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.player.placeTile(coordinates, rotation);
		} catch (NoTileInHandException | NoShipboardException | FixedTileException | TileAlreadyPresentException
				 | OutOfBuildingAreaException | TileWithoutNeighborException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
        }

        GameServer.getInstance().broadcastUpdate(pg.game);
    }

	@Override
	public void finishAssembling(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.player.endAssembly();
		} catch (NoShipboardException | AlreadyEndedAssemblyException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
    }
}
