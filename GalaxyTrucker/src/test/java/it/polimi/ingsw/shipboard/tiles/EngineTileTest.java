package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineTileTest {

    private EngineTile singleEngineTile;
    private EngineTile doubleEngineTile;

    @BeforeEach
    void setUp() {
        //Creates a single engine
        SideType[] singleEngine = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.ENGINE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        singleEngineTile = new EngineTile(singleEngine, false);

        //Creates a double engine
        SideType[] doubleEngine = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.ENGINE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        doubleEngineTile = new EngineTile(doubleEngine, true);
    }

    @Test
    void testConstructor() {

        // Test CLI symbols
        assertEquals("1e", singleEngineTile.getCLISymbol());
        assertEquals("2E", doubleEngineTile.getCLISymbol());
    }

    @Test
    void testisDoubleEngine() {

        // Single Cannon returns false
        assertFalse(singleEngineTile.isDoubleEngine());

        // Double Cannon returns true
        assertTrue(doubleEngineTile.isDoubleEngine());
    }

    @Test
    void testCalculateThrustPower() {

        // non funza x ora
        assertEquals(1f, singleEngineTile.calculateThrustPower());
    }

    @Test
    void testPlace() {

        //Creates coords where to place the tile
        Coordinates coords = new Coordinates(4, 5);

        //Assures tile gets placed
        assertDoesNotThrow(() -> singleEngineTile.place(coords));

        //Assures that the tile is placed correctly
        try {
            assertEquals(coords, singleEngineTile.getCoordinates());
        } catch (NotFixedTileException e) {
            fail("Tile should be fixed but threw NotFixedTileException");
        }

        // Test that you can't place the tile again
        assertThrows(FixedTileException.class, () -> singleEngineTile.place(new Coordinates(5, 6)));
    }
}