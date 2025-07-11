package it.polimi.ingsw.model.playerInput.PIRs;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PIRAllocateLoadablesTest {

	private Player player;
	private ShipBoard shipBoard;

	@BeforeEach
	void setUp() {
		player = new Player("TestCommander", UUID.randomUUID(), MainCabinTile.Color.BLUE);
		shipBoard = ShipBoard.create(GameLevel.TWO, MainCabinTile.Color.BLUE);
		player.setShipBoard(shipBoard);
		fillShipboardWithTiles(shipBoard, TilesFactory.createPileTiles());

		try {
			shipBoard.endAssembly();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testCLIRepresentation() {
		List<LoadableType> toAdd = new ArrayList<>();
		toAdd.add(LoadableType.BLUE_GOODS);
		toAdd.add(LoadableType.GREEN_GOODS);
		toAdd.add(LoadableType.YELLOW_GOODS);
		toAdd.add(LoadableType.GREEN_GOODS);

		PIRAddLoadables pir = new PIRAddLoadables(player, 1, toAdd);

		CLIFrame cliFrame = pir.getCLIRepresentation();

		System.out.println("=== Add Loadables CLI Representation ===");
		System.out.println(cliFrame);

		assertNotNull(cliFrame);
	}

	private void fillShipboardWithTiles(ShipBoard shipBoard, List<TileSkeleton> tilesPool) {
		int stepTotal = 1;
		int stepIters = 0;
		int step = 0;
		it.polimi.ingsw.enums.Direction spiralDirection = it.polimi.ingsw.enums.Direction.NORTH;
		Coordinates coords = it.polimi.ingsw.util.BoardCoordinates.getMainCabinCoordinates();
		int maxRow = it.polimi.ingsw.util.BoardCoordinates.getFirstCoordinateFromDirection(it.polimi.ingsw.enums.Direction.SOUTH);
		int maxCol = it.polimi.ingsw.util.BoardCoordinates.getFirstCoordinateFromDirection(it.polimi.ingsw.enums.Direction.EAST);
		TileSkeleton t = null;

		while (coords.getRow() <= maxRow || coords.getColumn() <= maxCol) {
			if (t == null) {
				Collections.shuffle(tilesPool);
				t = tilesPool.removeFirst();
			}
			try {
				shipBoard.setTile(t, coords);
				t = null;
			} catch (Exception ignored) {}

			coords = coords.getNext(spiralDirection);
			step++;
			if (step == stepTotal) {
				step = 0;
				stepIters++;
				spiralDirection = spiralDirection.getRotated(it.polimi.ingsw.enums.Rotation.CLOCKWISE);
				if (stepIters == 2) {
					stepIters = 0;
					stepTotal++;
				}
			}
		}
	}
}
