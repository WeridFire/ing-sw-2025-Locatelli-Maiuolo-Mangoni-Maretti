package it.polimi.ingsw.model.game;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.shipboard.exceptions.*;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.IntStream;

import static it.polimi.ingsw.enums.Rotation.CLOCKWISE;
import static it.polimi.ingsw.enums.Rotation.COUNTERCLOCKWISE;

public class Cheats {

	private static List<TileSkeleton> validatePhaseAndGetTilesAsGfxElements(GameData gameData) {
		if (gameData.getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
			return null;
		}

		List<TileSkeleton> tileList = TilesFactory.createPileTiles();
		tileList.add(34, null);
		tileList.add(52, null);
		tileList.add(61, null);
		tileList.add(33, null);
		return tileList;
	}


	public static void shipboardMethodPrinter(ShipBoard shipBoard, GameData gameData){
		List<TileSkeleton> tileList = validatePhaseAndGetTilesAsGfxElements(gameData);
		for (TileSkeleton skeleton : shipBoard.getTiles()) {
			int tileId = IntStream.range(0, tileList.size())
					.filter(i -> tileList.get(i) != null && tileList.get(i).getTextureName().equals(skeleton.getTextureName()))
					.findFirst()
					.orElse(-1); // or throw an exception if not found
			Coordinates coords = skeleton.forceGetCoordinates();
			if(tileId != -1){
				if(skeleton.getAppliedRotation() != Rotation.NONE){
					System.out.printf("tileList.get(%s).rotateTile(%s);%n", tileId, skeleton.getAppliedRotation());
				}
				System.out.printf(
						"player.getShipBoard().forceSetTile(tileList.get(%s), new Coordinates(%s)); //%s%n%n",
						tileId,
						coords.getRow() + ", " + coords.getColumn(),
						skeleton.getName()
				);
			}
		}
	}
	public static void cheatShieldedShipboard(GameData gameData, Player player) throws FixedTileException {
		List<TileSkeleton> tileList = validatePhaseAndGetTilesAsGfxElements(gameData);
		if (tileList == null) return;

		player.getShipBoard().forceSetTile(tileList.get(77), new Coordinates(8, 7)); //Cannon with power 1.0

		player.getShipBoard().forceSetTile(tileList.get(92), new Coordinates(9, 8)); //Double Cannon with power 2.0

		player.getShipBoard().forceSetTile(tileList.get(135), new Coordinates(6, 8)); //Double Cannon with power 2.0

		tileList.get(155).rotateTile(COUNTERCLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(155), new Coordinates(7, 6)); //Shield /s

		tileList.get(151).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(151), new Coordinates(8, 6)); //Shield /s

		player.getShipBoard().forceSetTile(tileList.get(5), new Coordinates(8, 8)); //Battery Tile 0/2

		tileList.get(3).rotateTile(COUNTERCLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(3), new Coordinates(7, 8)); //Battery Tile 0/2

		tileList.get(1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(1), new Coordinates(6, 6)); //Battery Tile 0/2

		player.getShipBoard().forceSetTile(tileList.get(99), new Coordinates(9, 9)); //Double Cannon with power 2.0

		player.getShipBoard().forceSetTile(tileList.get(130), new Coordinates(6, 7)); //Double Cannon with power 2.0

		player.getShipBoard().forceSetTile(tileList.get(9), new Coordinates(9, 6)); //Battery Tile 0/2

		player.getShipBoard().forceSetTile(tileList.get(127), new Coordinates(8, 5)); //Double Cannon with power 2.0

		player.getShipBoard().forceSetTile(tileList.get(14), new Coordinates(9, 5)); //Battery Tile 0/3

	}
	public static void cheatb3_0200(GameData gameData, Player player) throws FixedTileException {
		List<TileSkeleton> tileList = validatePhaseAndGetTilesAsGfxElements(gameData);
		if (tileList == null) return;

		player.getShipBoard().forceSetTile(tileList.get(13), new Coordinates(8, 7));
	}

	public static void cheatShipboard(String cheatName, Game game, Player player) throws AlreadyEndedAssemblyException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, RemoteException, OutOfBuildingAreaException, UninitializedShipboardException {
		switch(cheatName){
			case "standard" -> cheatStandardShipboard(game.getGameData(), player);
			case "ipship" -> Cheats.integrityProblemShipboard(game, player);
			case "randomship" -> Cheats.randomShipboard(game, player);
			case "shielded" -> Cheats.cheatShieldedShipboard(game.getGameData(), player);
			case "b3-0200" -> Cheats.cheatb3_0200(game.getGameData(), player);
			default -> throw new UninitializedShipboardException("Cheat shipboard '" + cheatName +"' not found.");
		}

	}


	public static void cheatStandardShipboard(GameData gameData, Player player) throws AlreadyEndedAssemblyException, FixedTileException,
			TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, RemoteException {
		List<TileSkeleton> tileList = validatePhaseAndGetTilesAsGfxElements(gameData);
		if (tileList == null) return;

		player.getShipBoard().forceSetTile(tileList.get(97 - 1), new Coordinates(8, 7)); //double cannon
		player.getShipBoard().forceSetTile(tileList.get(7 - 1), new Coordinates(8, 8)); //battery

		tileList.get(25 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(25 - 1), new Coordinates(6, 7)); //cargo hold

		player.getShipBoard().forceSetTile(tileList.get(101 - 1), new Coordinates(5, 7)); //single turret

		tileList.get(142 - 1).rotateTile(CLOCKWISE); //life support
		tileList.get(142 - 1).rotateTile(CLOCKWISE); //life suppport
		player.getShipBoard().forceSetTile(tileList.get(142 - 1), new Coordinates(6, 6)); //life support

		tileList.get(43 - 1).rotateTile(COUNTERCLOCKWISE); //cabin rot
		player.getShipBoard().forceSetTile(tileList.get(43 - 1), new Coordinates(7, 6)); //cabin

		tileList.get(148 - 1).rotateTile(CLOCKWISE);
		tileList.get(148 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(148 - 1), new Coordinates(7, 5));

		tileList.get(42 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(42 - 1), new Coordinates(8, 5));

		player.getShipBoard().forceSetTile(tileList.get(66 - 1), new Coordinates(8, 6));

		player.getShipBoard().forceSetTile(tileList.get(76 - 1), new Coordinates(9, 5));

		player.getShipBoard().forceSetTile(tileList.get(14 - 1 ), new Coordinates(9, 6));

		tileList.get(26 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(26 - 1), new Coordinates(9, 8));

		tileList.get(155 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(155 - 1), new Coordinates(9, 9));

		player.getShipBoard().forceSetTile(tileList.get(48 - 1), new Coordinates(8, 9));

		tileList.get(68 - 1).rotateTile(COUNTERCLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(68 - 1), new Coordinates(7, 9));

		tileList.get(62 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(62 - 1), new Coordinates(7, 8));

		player.getShipBoard().forceSetTile(tileList.get(133 - 1), new Coordinates(6, 8));
		Game game = GamesHandler.getInstance().getGame(gameData.getGameId());
		GameServer.getInstance().broadcastUpdateShipboardSpectators(game, player);

	}

	public static void randomFillShipboard(ShipBoard playerShip, List<TileSkeleton> tilesLeft) throws RemoteException {
		TileSkeleton randomTile = null;

		// randomly fill the shipboard
		for (int r = 2; r <= 12; r++) {
			for (int c = 2; c <= 12; c++) {
				if (randomTile == null) {
					randomTile = tilesLeft.removeFirst();
					try {
						randomTile.rotateTile(Rotation.random());
					} catch (FixedTileException e) {
						throw new RuntimeException(e);  // should never happen -> runtime exception
					}
				}
				try {
					playerShip.setTile(randomTile, new Coordinates(r, c));
					randomTile = null;  // to retrieve new random tile next time
				} catch (TileWithoutNeighborException e) {
					// note: this error should be avoided when forcefully constructing the ship
					try {
						playerShip.forceSetTile(randomTile, new Coordinates(r, c));
					} catch (FixedTileException ex) {
						throw new RuntimeException(ex);  // should never happen -> runtime exception
					}
					randomTile = null;  // to retrieve new random tile next time
				} catch (Exception e) {
					// ok: ignore placing in wrong coordinates
				}
			}
		}

		if (randomTile != null) {
			try {
				randomTile.resetRotation();
			} catch (FixedTileException e) {
				throw new RuntimeException(e);  // should never happen -> runtime exception
			}
			tilesLeft.set(0, randomTile);
		}
	}

	public static void randomShipboard(Game game, Player player) throws RemoteException {
		if (game.getGameData().getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
			return;
		}

		randomFillShipboard(player.getShipBoard(), game.getGameData().getCoveredTiles());

		GameServer.getInstance().broadcastUpdateShipboardSpectators(game, player);
	}

	public static void integrityProblemShipboard(Game game, Player player) throws RemoteException, FixedTileException {
		List<TileSkeleton> tileList = validatePhaseAndGetTilesAsGfxElements(game.getGameData());
		if (tileList == null) return;

		player.getShipBoard().forceSetTile(tileList.get(3 - 1), new Coordinates(8, 7));
		player.getShipBoard().forceSetTile(tileList.get(18 - 1), new Coordinates(7, 6));

		tileList.get(136 - 1).rotateTile(CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(136 - 1), new Coordinates(8, 6));

		player.getShipBoard().forceSetTile(tileList.get(11 - 1), new Coordinates(8, 8));

		GameServer.getInstance().broadcastUpdateShipboardSpectators(game, player);
	}

	public static void skipPhase(Game game){

	}

}
