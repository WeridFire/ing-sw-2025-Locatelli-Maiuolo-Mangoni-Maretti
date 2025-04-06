package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.SideType;
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
    void testCalculateEnginePower() {

        // non funza x ora
        assertEquals(1f, singleEngineTile.calculateThrustPower());
    }
}