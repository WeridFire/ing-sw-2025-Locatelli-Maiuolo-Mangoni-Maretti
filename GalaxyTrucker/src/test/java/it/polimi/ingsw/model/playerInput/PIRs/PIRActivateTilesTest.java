package it.polimi.ingsw.model.playerInput.PIRs;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRActivateTiles;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PIRActivateTilesTest {

	private Player player;
	private ShipBoard shipBoard;

	@BeforeEach
	void setUp() {
		player = new Player("TestCommander", UUID.randomUUID(), MainCabinTile.Color.BLUE);
		shipBoard = ShipBoard.create(GameLevel.TWO, MainCabinTile.Color.BLUE);
		player.setShipBoard(shipBoard);
	}

	@Test
	void testCLIRepresentation() {
		// Fill shipboard for a more "real" state, similar to your shipboard test
		List<TileSkeleton> tilesPool = TilesFactory.createPileTiles();
		fillShipboardWithTiles(shipBoard, tilesPool);
		try {
			shipBoard.endAssembly();
		} catch (AlreadyEndedAssemblyException e) {
			throw new RuntimeException(e);
		}

		// Create a PIRActivateTiles object
		PIRActivateTiles pir = new PIRActivateTiles(player, 1, PowerType.FIRE);

		// For testing: Simulate highlight mask (mocking would be best, but for the demo you can set up valid coordinates on the shipboard)
		Set<Coordinates> fakeHighlights = new HashSet<>();
		for (int i = 2; i < 5; i++) {
			fakeHighlights.add(new Coordinates(i, i));
			fakeHighlights.add(new Coordinates(i, 10 - i));
		}

		// Directly manipulate the ship’s highlight source — or assume your ShipBoard visitor uses this kind of logic for the test.
		// (If the highlight mask is fixed inside getInfoPower, adjust the fake logic accordingly in production code.)

		// Now call getCLIRepresentation
		CLIFrame cliFrame = pir.getCLIRepresentation();

		// Print output for visual verification
		System.out.println(cliFrame);

		// Make sure it’s not null
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
