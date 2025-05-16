package it.polimi.ingsw.game;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.exceptions.TileWithoutNeighborException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.List;

public class Cheats {

	public static void cheatShipboard(Game game, Player player) throws AlreadyEndedAssemblyException, FixedTileException,
			TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, RemoteException {
		if (game.getGameData().getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
			return;
		}

		List<TileSkeleton> tileList = TilesFactory.createPileTiles();
		tileList.add(34, null);
		tileList.add(52, null);
		tileList.add(61, null);
		tileList.add(33, null);
		player.getShipBoard().forceSetTile(tileList.get(97 - 1), new Coordinates(8, 7)); //double cannon
		player.getShipBoard().forceSetTile(tileList.get(7 - 1), new Coordinates(8, 8)); //battery

		tileList.get(25 - 1).rotateTile(Rotation.CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(25 - 1), new Coordinates(6, 7)); //cargo hold

		player.getShipBoard().forceSetTile(tileList.get(101 - 1), new Coordinates(5, 7)); //single turret

		tileList.get(142 - 1).rotateTile(Rotation.CLOCKWISE); //life support
		tileList.get(142 - 1).rotateTile(Rotation.CLOCKWISE); //life suppport
		player.getShipBoard().forceSetTile(tileList.get(142 - 1), new Coordinates(6, 6)); //life support

		tileList.get(43 - 1).rotateTile(Rotation.COUNTERCLOCKWISE); //cabin rot
		player.getShipBoard().forceSetTile(tileList.get(43 - 1), new Coordinates(7, 6)); //cabin

		tileList.get(148 - 1).rotateTile(Rotation.CLOCKWISE);
		tileList.get(148 - 1).rotateTile(Rotation.CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(148 - 1), new Coordinates(7, 5));

		tileList.get(42 - 1).rotateTile(Rotation.CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(42 - 1), new Coordinates(8, 5));

		player.getShipBoard().forceSetTile(tileList.get(66 - 1), new Coordinates(8, 6));

		player.getShipBoard().forceSetTile(tileList.get(76 - 1), new Coordinates(9, 5));

		player.getShipBoard().forceSetTile(tileList.get(14 - 1 ), new Coordinates(9, 6));

		tileList.get(26 - 1).rotateTile(Rotation.CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(26 - 1), new Coordinates(9, 8));

		tileList.get(155 - 1).rotateTile(Rotation.CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(155 - 1), new Coordinates(9, 9));

		player.getShipBoard().forceSetTile(tileList.get(48 - 1), new Coordinates(8, 9));

		tileList.get(68 - 1).rotateTile(Rotation.COUNTERCLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(68 - 1), new Coordinates(7, 9));

		tileList.get(62 - 1).rotateTile(Rotation.CLOCKWISE);
		player.getShipBoard().forceSetTile(tileList.get(62 - 1), new Coordinates(7, 8));

		player.getShipBoard().forceSetTile(tileList.get(133 - 1), new Coordinates(6, 8));

		GameServer.getInstance().broadcastUpdate(game);

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

		GameServer.getInstance().broadcastUpdate(game);
	}

	public static void skipPhase(Game game){

	}

}
