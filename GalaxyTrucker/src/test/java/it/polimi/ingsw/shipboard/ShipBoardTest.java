package it.polimi.ingsw.shipboard;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.game.Cheats;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.exceptions.TileWithoutNeighborException;
import it.polimi.ingsw.shipboard.tiles.CabinTile;
import it.polimi.ingsw.shipboard.tiles.Tile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShipBoardTest {

    private Game game1;
    private Player player1;
    private ShipBoard shipBoard1;
    private List<TileSkeleton> tiles;

    @BeforeEach
    void setup() throws PlayerAlreadyInGameException, AlreadyEndedAssemblyException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, RemoteException, OutOfBuildingAreaException, GameAlreadyRunningException {
        game1 = new Game();
        UUID uuid1 = UUID.randomUUID();
        player1 = new Player("player1", uuid1);
        game1.addPlayer(player1.getUsername(), uuid1);
        shipBoard1 = new ShipBoard(GameLevel.TESTFLIGHT);
        player1.setShipBoard(shipBoard1);

        tiles = TilesFactory.createPileTiles();

        // Initialize a ShipBoard
        Cheats.cheatShipboard(game1, player1);
    }

    @Test
    void testInitialization() {
        // Ensure the ShipBoard is properly initialized
        assertNotNull(player1.getShipBoard(), "ShipBoard should not be null after initialization");
        assertEquals(0, shipBoard1.getTiles().size(), "ShipBoard should initially contain no tiles");
    }

    //non va?
    @Test
    void testAddTileToShipBoard() throws FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, AlreadyEndedAssemblyException {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        // Add a tile to the ShipBoard and verify its addition
        Coordinates coord = new Coordinates(8, 7);
        shipBoard1.setTile(cabin, coord);
        assertTrue(shipBoard1.getTiles().contains(cabin), "Tile should be present in the ShipBoard after being added");
    }

}