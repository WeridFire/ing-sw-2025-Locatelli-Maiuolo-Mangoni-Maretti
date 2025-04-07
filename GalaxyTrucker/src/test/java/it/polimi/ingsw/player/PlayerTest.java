package it.polimi.ingsw.player;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.DrawTileException;
import it.polimi.ingsw.player.exceptions.AlreadyHaveTileInHandException;
import it.polimi.ingsw.player.exceptions.NoTileInHandException;
import it.polimi.ingsw.player.exceptions.TooManyReservedTilesException;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.exceptions.ThatTileIdDoesNotExistsException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.tiles.CabinTile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;

import static it.polimi.ingsw.enums.GamePhaseType.ASSEMBLE;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        player1 = new Player("SpaceTruckKing", UUID.randomUUID());
        shipBoard1 = new ShipBoard(GameLevel.TWO);
        gameId = UUID.randomUUID();
        gameData = new GameData(gameId);
    }

    @Test
    void testDrawTile() throws DrawTileException, AlreadyHaveTileInHandException {

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
    void testDiscardTile() throws DrawTileException, AlreadyHaveTileInHandException, NoTileInHandException {
        List<TileSkeleton> drawnTiles = new ArrayList<>();
        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setDrawnTiles(drawnTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        player1.discardTile(gameData);
        assertNull(player1.getTileInHand());
    }

    @Test
    void testReserveTile() throws DrawTileException, AlreadyHaveTileInHandException, NoTileInHandException, TooManyReservedTilesException {
        List<TileSkeleton> drawnTiles = new ArrayList<>();
        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setDrawnTiles(drawnTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        player1.setReservedTiles(player1.getTileInHand());
        assertNotNull(player1.getReservedTiles().getFirst());
    }

    @Test
    void testPickTile() throws DrawTileException, AlreadyHaveTileInHandException, NoTileInHandException, ThatTileIdDoesNotExistsException {
        List<TileSkeleton> drawnTiles = new ArrayList<>();
        List<TileSkeleton> MockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        MockTiles.add(mockTile1);
        MockTiles.add(mockTile2);
        gameData.setCoveredTiles(MockTiles);
        gameData.setDrawnTiles(drawnTiles);
        gameData.setCurrentGamePhaseType(ASSEMBLE);
        player1.drawTile(gameData);
        player1.discardTile(gameData);
        player1.drawTile(gameData);
        player1.discardTile(gameData);
        player1.pickTile(gameData, 2);
        assertEquals(2, player1.getTileInHand().getTileId());
    }


    @Test
    void testCliPrint() throws OutOfBuildingAreaException,FixedTileException, TileAlreadyPresentException {
        player1.setShipBoard(shipBoard1);
        List<TileSkeleton> tilesPool = TilesFactory.createPileTiles();
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

        /* DEPRECATED
        System.out.println("Test1");
        player1.printCliShipboard();
         */

        System.out.println("\nTest BEFORE assembly's ended\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation());

        assertDoesNotThrow(() -> player1.getShipBoard().endAssembly());
        System.out.println("\nTest AFTER assembly's ended\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation().merge(
                new CLIFrame(new String[] {"Test emoji: " + ANSI.BACKGROUND_BLUE + "☄️ 🌑 💥 🔥", " -- test -- " }), Direction.SOUTH));
    }

    private TileSkeleton createMockTile(int id) {
        return new TileSkeleton(null) {
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

            {
                setTileId(id);
            }
        };
    }
}