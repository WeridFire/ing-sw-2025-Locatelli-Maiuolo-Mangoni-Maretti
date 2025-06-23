package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructuralTileTest {

    private StructuralTile structuralTile;
    private TileVisitor mockVisitor;

    @BeforeEach
    void setUp() {
        // Define sides for the structural tile
        SideType[] sides = {
                SideType.UNIVERSAL,  // East
                SideType.SMOOTH,     // North
                SideType.SINGLE,     // West
                SideType.DOUBLE      // South
        };

        // Initialize the structural tile
        structuralTile = new StructuralTile(sides);

    }

    @Test
    void testConstructor() {
        // Verify that the CLI symbol is set correctly
        assertEquals("╳╳", structuralTile.getCLISymbol());

    }

    @Test
    void testGetName() {
        // Verify that the name is returned correctly
        assertEquals("Structural Tile", structuralTile.getName());
    }

    @Test
    void testAccept() {

        // Define sides for the structural tile
        SideType[] sides = {
                SideType.UNIVERSAL,  // East
                SideType.SMOOTH,     // North
                SideType.SINGLE,     // West
                SideType.DOUBLE      // South
        };

        // Initialize the structural tile
        StructuralTile tile = new StructuralTile(sides);

        // Create a mock visitor with a wasVisited method
        class MockTileVisitor implements TileVisitor {
            private boolean visited = false;

            @Override
            public void visitStructural(StructuralTile structuralTile) {
                visited = true;
                assertSame(tile, structuralTile, "The visitor should visit the correct StructuralTile.");
            }

            @Override
            public void visitLifeSupportSystem(LifeSupportSystemTile tile) {

            }

            @Override
            public void visitCargoHold(CargoHoldTile tile) {

            }

            @Override
            public void visitCabin(CabinTile tile) {

            }

            @Override
            public void visitMainCabin(CabinTile tile) {

            }

            @Override
            public void visitBatteryComponent(BatteryComponentTile tile) {

            }

            @Override
            public void visitCannon(CannonTile tile) {

            }

            @Override
            public void visitEngine(EngineTile tile) {

            }

            @Override
            public void visitShieldGenerator(ShieldGeneratorTile shieldGeneratorTile) {

            }

            public boolean wasVisited() {
                return visited;
            }
        }

        MockTileVisitor mockVisitor = new MockTileVisitor();

        // Act
        tile.accept(mockVisitor);

        // Assert
        assertTrue(mockVisitor.wasVisited(), "The visitor should visit the StructuralTile.");
    }
}