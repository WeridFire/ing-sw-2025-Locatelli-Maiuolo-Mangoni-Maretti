package it.polimi.ingsw.model.shipboard;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.ProtectionType;
import it.polimi.ingsw.model.game.Cheats;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.exceptions.ColorAlreadyInUseException;
import it.polimi.ingsw.model.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.model.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.exceptions.NoTileInHandException;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.exceptions.*;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.model.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShipBoardTest {

    private Game game1;
    private Player player1;
    private ShipBoard shipBoard1;
    private List<TileSkeleton> tiles;
    private MainCabinTile mainCabinTile;

    @BeforeEach
    void setup() throws PlayerAlreadyInGameException, AlreadyEndedAssemblyException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, RemoteException, OutOfBuildingAreaException, GameAlreadyRunningException, ColorAlreadyInUseException {
        game1 = new Game();
        UUID uuid1 = UUID.randomUUID();
        player1 = new Player("player1", uuid1, MainCabinTile.Color.BLUE);
        game1.addPlayer(player1.getUsername(), uuid1, MainCabinTile.Color.BLUE);
        shipBoard1 = new ShipBoard(GameLevel.TESTFLIGHT);
        player1.setShipBoard(shipBoard1);
        mainCabinTile = new MainCabinTile(MainCabinTile.Color.RED);
        shipBoard1.forceSetTile(mainCabinTile, new Coordinates(7, 7));
        tiles = TilesFactory.createPileTiles();


        // Initialize a ShipBoard
        Cheats.cheatShipboard(game1.getGameData(), player1);
    }

    @Test
    void testInitialization() {
        // Ensure the ShipBoard is properly initialized
        assertNotNull(player1.getShipBoard(), "ShipBoard should not be null after initialization");
        assertEquals(1, shipBoard1.getTiles().size(), "ShipBoard should initially contain one tile");
    }

    @Test
    void testAddTileToShipBoard() throws FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, AlreadyEndedAssemblyException {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        // Add a tile to the ShipBoard and verify its addition
        Coordinates coord = new Coordinates(8, 7);
        shipBoard1.setTile(cabin, coord);
        assertTrue(shipBoard1.getTiles().contains(cabin), "Tile should be present in the ShipBoard after being added");
    }

    @Test
    void testAddTileWithoutNeighborThrows() {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        Coordinates coord = new Coordinates(6, 6); //no neighbours
        assertThrows(TileWithoutNeighborException.class, () -> {
            shipBoard1.setTile(cabin, coord);
        }, "Should throw TileWithoutNeighborException when adding a tile without a neighbor");
    }

    @Test
    void testAddTileOutOfBuildingAreaThrows() {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        Coordinates coord = new Coordinates(5, 5); //no neighbours
        assertThrows(OutOfBuildingAreaException.class, () -> {
            shipBoard1.setTile(cabin, coord);
        }, "Should throw TileWithoutNeighborException when adding a tile without a neighbor");
    }

    @Test
    void testAlreadyPresentTileThrows() {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        Coordinates coord = new Coordinates(7, 7); //no neighbours
        assertThrows(TileAlreadyPresentException.class, () -> {
            shipBoard1.setTile(cabin, coord);
        }, "Should throw TileWithoutNeighborException when adding a tile without a neighbor");
    }

    @Test
    void testGetTileAtCoordinates() throws FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, AlreadyEndedAssemblyException, NoTileFoundException {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        Coordinates coord = new Coordinates(8, 7);
        shipBoard1.setTile(cabin, coord);
        assertEquals(cabin, shipBoard1.getTile(coord), "Should return the correct tile at given coordinates");
    }

    @Test
    void testGetTilesReturnsAllTiles() throws FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, AlreadyEndedAssemblyException {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
        Coordinates coord = new Coordinates(8, 7);
        shipBoard1.setTile(cabin, coord);
        assertEquals(2, shipBoard1.getTiles().size(), "ShipBoard should contain both the main cabin and the new tile");
    }

    @Test
    void miscTests() throws UninitializedShipboardException, AlreadyEndedAssemblyException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException, NoTileFoundException {
        Coordinates coord = new Coordinates(1, 1);
        Coordinates coord2 = new Coordinates(6, 6);
        Coordinates coord3 = new Coordinates(6, 7);
        Coordinates coord4 = new Coordinates(7, 8);
        Coordinates coord5 = new Coordinates(7, 6);
        Coordinates coord6 = new Coordinates(8, 7);

        SideType[] northCannon = Direction.sortedArray(
                SideType.CANNON,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);
        CannonTile northC = new CannonTile(northCannon, false);
        SideType[] eastCannon = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.CANNON,  // EAST
                SideType.UNIVERSAL,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);
        CannonTile eastC = new CannonTile(eastCannon, false);
        SideType[] westCannon = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,     // SOUTH
                SideType.CANNON   // WEST
        ).toArray(SideType[]::new);
        CannonTile westC = new CannonTile(westCannon, false);
        SideType[] southCannon = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.CANNON,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);
        CannonTile southC = new CannonTile(southCannon, false);

        Set<TileSkeleton> tiles =  shipBoard1.getTiles();
        assertThrows(OutOfBuildingAreaException.class, () -> {shipBoard1.getTile(coord);});
        assertThrows(NoTileFoundException.class, () -> {shipBoard1.getTile(coord2);});
        assertThrows(UninitializedShipboardException.class, () -> {shipBoard1.getColor();});


        Boolean b = shipBoard1.isFilled();
        GameLevel l = shipBoard1.getLevel();
        Map<Coordinates, TileSkeleton> map = shipBoard1.getBoard();
        shipBoard1.setTile(northC, coord3);
        shipBoard1.setTile(eastC, coord4);
        shipBoard1.setTile(westC, coord5);
        shipBoard1.setTile(southC, coord6);

        //TODO: fix getCannonProtection method in shipboard
        shipBoard1.resetVisitors();
        //assertEquals(shipBoard1.getCannonProtection(Direction.NORTH, 7), ProtectionType.SINGLE_CANNON);
        //assertEquals(shipBoard1.getCannonProtection(Direction.EAST, 7), ProtectionType.SINGLE_CANNON);
        //assertEquals(shipBoard1.getCannonProtection(Direction.WEST, 7), ProtectionType.SINGLE_CANNON);
        //assertEquals(shipBoard1.getCannonProtection(Direction.SOUTH, 7), ProtectionType.SINGLE_CANNON);

        shipBoard1.endAssembly();
        assertThrows(AlreadyEndedAssemblyException.class, () -> {shipBoard1.endAssembly();});

        shipBoard1.hit(Direction.NORTH, 7);
        assertThrows(NoTileFoundException.class, () -> {shipBoard1.getTile(coord3);});

        assertNotNull(shipBoard1.getCLIRepresentation());
    }



}