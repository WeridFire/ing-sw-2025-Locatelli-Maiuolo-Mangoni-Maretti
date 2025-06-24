package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CannonTileTest {

    private CannonTile singleCannon;
    private CannonTile doubleCannon;
    private CannonTile multiDirectionalCannon;

    @BeforeEach
    void setUp() {
        // Create a cannon pointing south (single)
        SideType[] southCannon = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.CANNON,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);
        singleCannon = new CannonTile(southCannon, false);

        // Create a double cannon pointing south
        doubleCannon = new CannonTile(southCannon, true);

        // Create a cannon pointing in multiple directions
        SideType[] multiDirectional = Direction.sortedArray(
                SideType.CANNON,     // NORTH
                SideType.CANNON,     // EAST
                SideType.CANNON,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);
        multiDirectionalCannon = new CannonTile(multiDirectional, false);
    }

    @Test
    void testConstructor() {
        // Test CLI symbols
        assertEquals("1t", singleCannon.getCLISymbol());
        assertEquals("2T", doubleCannon.getCLISymbol());
    }

    @Test
    void testIsDoubleCannon() {
        // Single cannon should return false
        assertFalse(singleCannon.isDoubleCannon());

        // Double cannon should return true
        assertTrue(doubleCannon.isDoubleCannon());
    }

    @Test
    void testCalculateFirePowerSingleDirection() {
        // Single cannon pointing south should have firepower 0.5f
        assertEquals(0.5f, singleCannon.calculateFirePower(), 0.001);

        // Double cannon pointing south should have firepower 2.0f
        assertEquals(1.0f, doubleCannon.calculateFirePower(), 0.001);
    }

    @Test
    void testCalculateFirePowerMultipleDirections() {
        // Cannon pointing NORTH (0.5), EAST (0.5), SOUTH (1.0) = 2.0 total firepower
        assertEquals(2.0f, multiDirectionalCannon.calculateFirePower(), 0.001);

        // Now create a double version of the multi-directional cannon (should be 4.0 firepower)
        SideType[] multiDirectional = Direction.sortedArray(
                SideType.CANNON,     // NORTH
                SideType.CANNON,     // EAST
                SideType.CANNON,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);
        CannonTile doubleMultiDirectional = new CannonTile(multiDirectional, true);

        assertEquals(4.0f, doubleMultiDirectional.calculateFirePower(), 0.001);
    }

    @Test
    void testCalculateFirePowerNoCannonSides() {
        // Create a cannon with no CANNON sides
        SideType[] noCannonSides = Direction.sortedArray(
                SideType.UNIVERSAL,
                SideType.UNIVERSAL,
                SideType.UNIVERSAL,
                SideType.UNIVERSAL
        ).toArray(SideType[]::new);

        CannonTile noCannon = new CannonTile(noCannonSides, false);

        // Should have no firepower
        assertEquals(0.0f, noCannon.calculateFirePower(), 0.001);
    }

    @Test
    void testAccept() {
        // Create a stub for TileVisitor
        TestTileVisitor visitor = new TestTileVisitor();

        // Call accept on the cannon tile
        singleCannon.accept(visitor);

        // Verify that visitCannon was called
        assertTrue(visitor.visitCannonCalled);
        assertEquals(singleCannon, visitor.lastCannonVisited);
    }

    @Test
    void testPlace() throws FixedTileException {
        Coordinates coords = new Coordinates(3, 4);

        // Test that the tile can be placed
        assertDoesNotThrow(() -> singleCannon.place(coords));

        // Test that the tile's coordinates are set correctly
        try {
            assertEquals(coords, singleCannon.getCoordinates());
        } catch (NotFixedTileException e) {
            fail("Tile should be fixed but threw NotFixedTileException");
        }

        // Test that you can't place the tile again
        assertThrows(FixedTileException.class, () -> singleCannon.place(new Coordinates(5, 6)));
    }

    @Test
    void testGetCoordinates() {
        // Test that getCoordinates throws NotFixedTileException if the tile has not been placed
        assertThrows(NotFixedTileException.class, () -> singleCannon.getCoordinates());
    }

    @Test
    void testCLIRepresentation() {
        // Test that the tile returns a non-null CLI representation
        assertNotNull(singleCannon.getCLIRepresentation());
        assertNotNull(doubleCannon.getCLIRepresentation());
    }

    @Test
    void isDoubleCannon() {
        assertEquals("Double Cannon with power 1.0", doubleCannon.getName());
        assertEquals("Cannon with power 0.5", singleCannon.getName());
    }

    // Helper class to test the visitor pattern
    private static class TestTileVisitor implements TileVisitor {
        boolean visitCannonCalled = false;
        CannonTile lastCannonVisited = null;

        @Override
        public void visitCannon(CannonTile tile) {
            visitCannonCalled = true;
            lastCannonVisited = tile;
        }

        // Implement other required methods with empty bodies
        @Override public void visitStructural(StructuralTile tile) {}

        /**
         * What to do when visiting the provided life support system tile.
         * To be implemented in each visitor.
         *
         * @param tile The visited life support system tile.
         */
        @Override
        public void visitLifeSupportSystem(LifeSupportSystemTile tile) {}

        @Override
        public void visitCabin(CabinTile tile) {}

        @Override
        public void visitMainCabin(CabinTile tile) {}

        @Override
        public void visitBatteryComponent(BatteryComponentTile tile) {}

        @Override
        public void visitEngine(EngineTile tile) {}

        @Override
        public void visitShieldGenerator(ShieldGeneratorTile tile) {}

        @Override
        public void visitCargoHold(CargoHoldTile tile) {}

    }
}