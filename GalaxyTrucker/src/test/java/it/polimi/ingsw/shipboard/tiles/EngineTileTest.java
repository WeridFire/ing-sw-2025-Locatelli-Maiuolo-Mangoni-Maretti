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
        SideType[] singleEngine = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.ENGINE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        singleEngineTile = new EngineTile(singleEngine, false);

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

}