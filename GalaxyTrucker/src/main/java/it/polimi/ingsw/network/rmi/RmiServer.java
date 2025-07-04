package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.cards.exceptions.CardsGroupException;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.game.Cheats;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.game.exceptions.*;
import it.polimi.ingsw.model.gamePhases.exceptions.AlreadyPickedPosition;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.model.gamePhases.exceptions.IllegalStartingPositionIndexException;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.model.player.exceptions.*;
import it.polimi.ingsw.model.shipboard.exceptions.*;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.kpf.KeepPlayerFlyingPredicate;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.playerInput.PIRs.PIRHandler;
import it.polimi.ingsw.model.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.model.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.model.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Logger;

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

	private GamesHandler gamesHandler;
	private GameServer gameServer;

	/**
	 * Creates a RMI server. All the methods call on this will affect the actual model. This is the junction between
	 * normal RMI and SOCKET connections.
	 */
	public RmiServer() { }

	/**
	 * Initialization method to call right after creating an instance of RMIServer,
	 * to avoid GameServer circular dependence.
	 */
	public void init() {
		gamesHandler = GamesHandler.getInstance();
		gameServer = GameServer.getInstance();
	}

	@Override
	public void connect(IClient client) throws RemoteException {
		UUID clientUUID = gameServer.registerClient(client);
		//Confirm connection and notify the assigned UUID.
		client.updateClient(new ClientUpdate(clientUUID));
	}

	@Override
	public void createGame(IClient client, String username, MainCabinTile.Color desiredColor)
			throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		try {
			Game game = gamesHandler.createGame(username, connectionUUID, desiredColor);
			if (game == null) {
				throw new Exception("Game creation failed");
			}
			System.out.println("Created new game: " + game.getId());
			GameServer.getInstance().broadcastUpdateAllRefreshOnlyIf((clientUUID, clientInterface) -> {
				// refresh only the clients that are not in a game yet, or this client
				return (GamesHandler.getInstance().findGameByClientUUID(clientUUID) == null);
			});
			client.updateClient(new ClientUpdate(connectionUUID));
		} catch (Exception e) {
			e.printStackTrace();
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username, MainCabinTile.Color desiredColor) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		try {
			Game game = gamesHandler.getGame(gameId);
			if(game == null) {
				throw new GameNotFoundException(gameId);
			}
			game.addPlayer(username, connectionUUID, desiredColor);
			GameServer.getInstance().broadcastUpdateAllRefreshMenuAndGame(gameId);
        } catch (PlayerAlreadyInGameException | GameNotFoundException | GameAlreadyRunningException
				 | ColorAlreadyInUseException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}
    }

	@Override
	public void quitGame(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if(pg == null) return;
		pg.game.disconnectPlayer(pg.player);
		// update both the game (that may now no longer exist aswell)
		// and the player that disconnected (since it will be in main menu)
		GameServer.getInstance().broadcastUpdateAllRefreshMenuAndGame(pg.game.getId());
	}

	@Override
	public void ping(IClient client) throws RemoteException {
		client.updateClient(new ClientUpdate(gameServer.getUUIDbyConnection(client)));
	}

	//PIR related COMMANDS

	@Override
	public void pirActivateTiles(IClient client, Set<Coordinates> tilesToActivate) throws RemoteException {
		Logger.info("Received PIR command ACTIVATE TILES.");
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			PIR activePIR = pg.game.getGameData().getPIRHandler().getPlayerPIR(pg.player);
			if(activePIR != null){
				activePIR.activateTiles(pg.player, tilesToActivate);
			}
			client.updateClient(new ClientUpdate(pg.connectionUUID));
		} catch (WrongPlayerTurnException | InputNotSupportedException | NotEnoughItemsException |
				 TileNotAvailableException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void pirAllocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException {
		Logger.info("Received PIR command ALLOCATE LOADABLES");
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			PIR activePIR = pg.game.getGameData().getPIRHandler().getPlayerPIR(pg.player);
			if(activePIR != null){
				activePIR.addLoadables(pg.player, cargoToAdd);
			}
			client.updateClient(new ClientUpdate(pg.connectionUUID));
		} catch (InputNotSupportedException | WrongPlayerTurnException | TileNotAvailableException |
				 UnsupportedLoadableItemException | TooMuchLoadException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void pirForceEndTurn(IClient client) throws RemoteException {
		Logger.info("Received PIR command END TURN");
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			PIRHandler handler = pg.game.getGameData().getPIRHandler();
			handler.endTurn(pg.player);
			// note: join interaction and not join turn to allow the ended pir to be in an atomic sequence
			handler.joinEndInteraction(pg.player);
			client.updateClient(new ClientUpdate(pg.connectionUUID));
		} catch (WrongPlayerTurnException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void pirRemoveLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException {
		Logger.info("Received PIR command REMOVE LOADABLES");
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;

		try {
			PIR activePIR = pg.game.getGameData().getPIRHandler().getPlayerPIR(pg.player);
			if(activePIR != null){
				activePIR.removeLoadables(pg.player, cargoToAdd);
			}
			client.updateClient(new ClientUpdate(pg.connectionUUID));
		} catch (InputNotSupportedException | WrongPlayerTurnException | TileNotAvailableException |
				 UnsupportedLoadableItemException | NotEnoughItemsException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void pirSelectMultipleChoice(IClient client, int selection) throws RemoteException {
		Logger.info("Received PIR command MULTIPLE CHOICE");
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;

		PIR activePIR = pg.game.getGameData().getPIRHandler().getPlayerPIR(pg.player);
		if(activePIR != null){
			try {
				activePIR.makeChoice(pg.player, selection);
			} catch (WrongPlayerTurnException | InputNotSupportedException e) {
				client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
				return;
			}
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
		} catch (DrawTileException | TooManyItemsInHandException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}

		GameServer.getInstance().broadcastUpdateRefreshOnly(pg.game, Set.of(pg.player));
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

		GameServer.getInstance().broadcastUpdateShipboardSpectators(pg.game, pg.player);
	}

	@Override
	public void pickTile(IClient client, Integer id) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try{
			pg.player.pickTile(pg.game.getGameData(), id);
		} catch (TooManyItemsInHandException | ThatTileIdDoesNotExistsException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
        }

		GameServer.getInstance().broadcastUpdate(pg.game);
    }

	@Override
	public void placeTile(IClient client, Coordinates coordinates, Rotation rotation) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;

		try {
			pg.player.placeTile(coordinates, rotation);
		} catch (NoTileInHandException | NoShipboardException | FixedTileException | TileAlreadyPresentException
				 | OutOfBuildingAreaException | TileWithoutNeighborException | AlreadyEndedAssemblyException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
        }

		GameServer.getInstance().broadcastUpdateShipboardSpectators(pg.game, pg.player);
    }

	@Override
	public void finishAssembling(IClient client, Integer preferredPosition) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.game.getGameData().endAssembly(pg.player, false, preferredPosition);
			pg.game.getGameData().getCurrentGamePhase().setAutoTimerSequence(true);
			// note: here no broadcast/update because is already managed by endAssembly
		} catch (NoShipboardException | AlreadyEndedAssemblyException | TooManyItemsInHandException |
                 AlreadyPickedPosition | IllegalStartingPositionIndexException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
    }

	@Override
	public void showCardGroup(IClient client, Integer id) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			ShipBoard shipBoard = pg.player.getShipBoard();
			if (shipBoard == null) {
				throw new NoShipboardException();  // caught below
			}
			if (shipBoard.getTiles().size() <= 1) {
				throw new CardsGroupException("You have to place a tile before taking a group of cards.");  // caught below
			}

			// attempt assigning the group of cards in hand
			Integer oldCardGroup = pg.player.getCardGroupInHand();
			pg.player.setCardGroupInHand(id);

			try {
				// attempt showing the group of cards to the player
				pg.game.getGameData().getDeck().getGroup(id).showGroup(pg.player.getUsername());
			} catch (Exception e) {
				// reset the old card group in hand
				pg.player.clearCardGroupInHand();
				if (oldCardGroup != null) {
					pg.player.setCardGroupInHand(oldCardGroup);
				}
				// then propagate the error (caught below if expected)
				throw e;
			}
		} catch (CardsGroupException | NoShipboardException | TooManyItemsInHandException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}

		GameServer.getInstance().broadcastUpdate(pg.game);
	}

	@Override
	public void hideCardGroup(IClient client) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ASSEMBLE);
		if (pg == null) return;
		// else: actually try to perform the action

		try {
			pg.game.getGameData().getDeck().getGroup(pg.player.getCardGroupInHand()).hideGroup();
			pg.player.clearCardGroupInHand();
		} catch (CardsGroupException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
			return;
		}

		GameServer.getInstance().broadcastUpdate(pg.game);
	}

	@Override
	public void requestEndFlight(IClient client, KeepPlayerFlyingPredicate saveFromEndFlight) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client, GamePhaseType.ADVENTURE);
		if (pg == null) return;

		pg.player.requestEndFlight(saveFromEndFlight);
		client.updateClient(new ClientUpdate(pg.connectionUUID, false));
	}

	@Override
	public void spectatePlayerShipboard(IClient client, String username) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if (pg == null) return;
		try {
			pg.game.getGameData().makePlayerSpectate(pg.player, username);
			client.updateClient(new ClientUpdate(pg.connectionUUID, true));
		} catch (IncorrectGamePhaseTypeException | PlayerNotInGameException e) {
			client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
		}
	}

	@Override
	public void useCheat(IClient client, String cheatName) throws RemoteException {
		PlayerGameInstance pg = PlayerGameInstance.validateClient(gamesHandler, gameServer, client);
		if(pg == null) return;

		switch (cheatName){
			case "skip" -> Cheats.skipPhase(pg.game);
			default -> {
				try {
					Cheats.cheatShipboard(cheatName, pg.game, pg.player);
					client.updateClient(new ClientUpdate(pg.connectionUUID, true));
				} catch (UninitializedShipboardException | AlreadyEndedAssemblyException | FixedTileException | TileAlreadyPresentException |
						 TileWithoutNeighborException | OutOfBuildingAreaException e) {
					client.updateClient(new ClientUpdate(pg.connectionUUID, e.getMessage()));
				}
			}
		}
	}

	@Override
	public void resumeGame(IClient client, UUID gameId) throws RemoteException {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);

		if(gamesHandler.getGame(gameId) != null) {
			client.updateClient(new ClientUpdate(connectionUUID, "The game specified is already running."));
			return;
		}

		GameData loaded = GameData.loadFromState(gameId);
		if(loaded == null){
			client.updateClient(new ClientUpdate(connectionUUID, "Could not find specified game."));
			return;
		}

		try {
			gamesHandler.resumeGame(loaded, connectionUUID);
		} catch (PlayerAlreadyInGameException | GameAlreadyRunningException e) {
			client.updateClient(new ClientUpdate(connectionUUID, e.getMessage()));
		}
	}
}
