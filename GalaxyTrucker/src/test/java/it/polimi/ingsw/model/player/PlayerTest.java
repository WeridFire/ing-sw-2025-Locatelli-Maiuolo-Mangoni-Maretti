package it.polimi.ingsw.model.player;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.game.exceptions.DrawTileException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.exceptions.*;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.model.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.model.shipboard.exceptions.ThatTileIdDoesNotExistsException;
import it.polimi.ingsw.model.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.model.shipboard.tiles.CabinTile;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.util.BoardCoordinates;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.polimi.ingsw.enums.GamePhaseType.ASSEMBLE;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class PlayerTest {

    private GameData gameData;
    private UUID gameId;
    private Player player1;
    private ShipBoard shipBoard1;
    private GamePhaseType gamePhaseType;

    SideType[] sideCannon1 = new SideType[4];
    Coordinates cabinCord1 = new Coordinates(5, 6);
    CabinTile cabin1 = new CabinTile(Direction.sortedArray(
            SideType.UNIVERSAL, SideType.DOUBLE, SideType.UNIVERSAL, SideType.SMOOTH).toArray(SideType[]::new));
    Coordinates cabinCord2 = new Coordinates(6, 6);
    CabinTile cabin2 = new CabinTile(Direction.sortedArray(
            SideType.SINGLE, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.SMOOTH).toArray(SideType[]::new));

    @BeforeEach
    void setUp() {
        player1 = new Player("SpaceTruckKing", UUID.randomUUID(), MainCabinTile.Color.BLUE);
        shipBoard1 = ShipBoard.create(GameLevel.TWO, MainCabinTile.Color.BLUE);
        gameId = UUID.randomUUID();
        gameData = new GameData(gameId);
    }

    @Test
    void testDrawTile() throws DrawTileException, TooManyItemsInHandException {

        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        assertNotNull(player1.getTileInHand());
    }

    @Test
    void testDiscardTile() throws DrawTileException, TooManyItemsInHandException, NoTileInHandException, ReservedTileException {
        List<TileSkeleton> drawnTiles = new ArrayList<>();
        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setUncoveredTiles(drawnTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        player1.discardTile(gameData);
        assertNull(player1.getTileInHand());
    }

    @Test
    void testReserveTile() throws DrawTileException, TooManyItemsInHandException, NoTileInHandException, TooManyReservedTilesException {
        List<TileSkeleton> drawnTiles = new ArrayList<>();
        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setUncoveredTiles(drawnTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        player1.reserveTile();
        assertNotNull(player1.getReservedTiles().getFirst());
    }

    @Test
    void testPickTile() throws DrawTileException, TooManyItemsInHandException, NoTileInHandException, ThatTileIdDoesNotExistsException, ReservedTileException {
        List<TileSkeleton> drawnTiles = new ArrayList<>();
        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setUncoveredTiles(drawnTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        player1.discardTile(gameData);
        player1.drawTile(gameData);
        player1.discardTile(gameData);
        player1.pickTile(gameData, 2);
        assertEquals(2, player1.getTileInHand().getTileId());
    }


    private void fillShipboardWithTiles(ShipBoard shipBoard, List<TileSkeleton> tilesPool) {
        // make a spiral starting from the center to avoid setTile without neighbors
        int stepTotal = 1;
        int stepIters = 0;
        int step = 0;
        Direction spiralDirection = Direction.NORTH;
        Coordinates coords = BoardCoordinates.getMainCabinCoordinates();
        int maxRow = BoardCoordinates.getFirstCoordinateFromDirection(Direction.SOUTH);
        int maxCol = BoardCoordinates.getFirstCoordinateFromDirection(Direction.EAST);
        TileSkeleton t = null;
        while (coords.getRow() <= maxRow || coords.getColumn() <= maxCol) {
            if (t == null) {
                Collections.shuffle(tilesPool);
                t = tilesPool.removeFirst();
            }
            try{
                shipBoard.setTile(t, coords);
                t = null;
            }catch(Exception e){}

            coords = coords.getNext(spiralDirection);
            step++;
            if (step == stepTotal) {
                step = 0;
                stepIters++;
                spiralDirection = spiralDirection.getRotated(Rotation.CLOCKWISE);
                if (stepIters == 2) {
                    stepIters = 0;
                    stepTotal++;
                }
            }
        }
    }

    @Test
    void testEndFlight(){
        player1.endFlight();

        assertTrue(player1.isEndedFlight());
        assertFalse(player1.hasRequestedEndFlight());
        assertNull(player1.getPosition());

    }


    @Test
    void testCliPrint() throws OutOfBuildingAreaException,FixedTileException, TileAlreadyPresentException {
        player1.setShipBoard(shipBoard1);
        List<TileSkeleton> tilesPool = TilesFactory.createPileTiles();

        /* DEPRECATED
        for(int i=0; i<10; i++){
            for(int j=0; j<10; j++){
                Coordinates c = new Coordinates(i, j);
                Collections.shuffle(tilesPool);
                TileSkeleton t = tilesPool.removeFirst();
                try{
                    shipBoard1.setTile(t, c);
                }catch(Exception e){}
            }
        }
        System.out.println("Test1");
        player1.printCliShipboard();
         */

        fillShipboardWithTiles(shipBoard1, tilesPool);

        System.out.println("\nTest BEFORE assembly's ended\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation());

        HashSet<Coordinates> highlightMap = new HashSet<>();
        for (int i=2; i<12; i++) {
            highlightMap.add(new Coordinates(i, i));
            highlightMap.add(new Coordinates(i, 14 - i));
        }
        System.out.println("\nTest BEFORE assembly's ended, with highlight = " + highlightMap + "\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation(highlightMap, ANSI.RED));

        assertDoesNotThrow(() -> player1.getShipBoard().endAssembly());
        System.out.println("\nTest AFTER assembly's ended\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation());
    }

    private TileSkeleton createMockTile(int id) {
        return new TileSkeleton(Direction.sortedArray(SideType.UNIVERSAL, SideType.UNIVERSAL,
                SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new)) {
            /**
             * Method to accept a visitor for this tile in the Visitor Pattern.
             *
             * @param visitor The visitor that will visit this tile.
             */
            @Override
            public void accept(TileVisitor visitor) {

            }

            @Override
            public CLIFrame getCLIRepresentation() {
                return null;
            }

            @Override
            public String getName() {
                return "";
            }

            {
                setTileId(id);
            }
        };
    }

    @Test
    void testForceEndFlight() throws AlreadyEndedAssemblyException, NoShipboardException, TileCanNotDisappearException, TooManyItemsInHandException, NoTileInHandException, ReservedTileException, ThatTileIdDoesNotExistsException {
        TileSkeleton t = createMockTile(1);
        player1.setShipBoard(shipBoard1);
        player1.setTileInHand(t);
        player1.forceEndAssembly(2);

        Player player2 = new Player("Paperino", UUID.randomUUID(), MainCabinTile.Color.RED);
        assertThrows(NoShipboardException.class, () -> {player2.forceEndAssembly(4);});
        assertNull(player1.getTileInHand());

        Player player3 = new Player("A", UUID.randomUUID(), MainCabinTile.Color.GREEN);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player3.setTileInHand(t);
        player3.setShipBoard(shipBoard1);
        player3.discardTile(gameData);
        player3.pickTile(gameData,1);
        assertEquals(t, player1.getTileInHand());
        player3.forceEndAssembly(3);
        assertEquals(player3.getLostTiles().size(), 1);
    }

    @Test
    void miscTests() throws TileCanNotDisappearException, AlreadyHaveTileInHandException, TooManyReservedTilesException, NoTileInHandException {
        TileSkeleton t = createMockTile(1);


        assertThrows(NoTileInHandException.class, () -> {player1.reserveTile();});
        assertFalse(player1.isTileInHandFromReserved());
        player1.requestEndFlight();
        assertTrue(player1.hasRequestedEndFlight());
        player1.setTileInHand(t);
        assertThrows(AlreadyHaveTileInHandException.class, () -> {player1.setTileInHand(t);});
        assertThrows(TileCanNotDisappearException.class, () -> {player1.setTileInHand(null);});
        player1.reserveTile();
        player1.setTileInHand(t);
        player1.reserveTile();
        player1.setTileInHand(t);
        assertThrows(TooManyReservedTilesException.class, () -> {player1.reserveTile();});
        player1.getColor();
        player1.addCredits(1);
        player1.getCredits();
        player1.getOrder();
        assertThrows(AlreadyHaveTileInHandException.class, () -> {player1.setCardGroupInHand(1);});
        assertThrows(AlreadyHaveTileInHandException.class, () -> {player1.pickTile(gameData,1);});
        assertThrows(TooManyItemsInHandException.class, () -> {player1.endAssembly(3);});
    }
}