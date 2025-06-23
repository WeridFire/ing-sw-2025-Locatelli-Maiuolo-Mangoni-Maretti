package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class ShieldGeneratorTileTest {


    @Test
    void testConstructorAndCliSymbol_WestNorth() {
        // Arrange
        SideType[] sides = new SideType[]{SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL};
        Boolean[] protectedSides = new Boolean[]{true, true, false, false};

        // Act
        ShieldGeneratorTile tile = new ShieldGeneratorTile(sides, protectedSides);

        // Assert
        assertEquals("/s", tile.getCLISymbol(), "The CLI Symbol for West and North should be '/s'.");
    }

    @Test
    void testConstructorAndCliSymbol_NorthEast() {
        // Arrange
        SideType[] sides = new SideType[]{SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL};
        Boolean[] protectedSides = new Boolean[]{false, true, true, false};

        // Act
        ShieldGeneratorTile tile = new ShieldGeneratorTile(sides, protectedSides);

        // Assert
        assertEquals("s\\", tile.getCLISymbol(), "The CLI Symbol for North and East should be 's\\'.");
    }

    @Test
    void testConstructorAndCliSymbol_EastSouth() {
        // Arrange
        SideType[] sides = new SideType[]{SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL};
        Boolean[] protectedSides = new Boolean[]{false, false, true, true};

        // Act
        ShieldGeneratorTile tile = new ShieldGeneratorTile(sides, protectedSides);

        // Assert
        assertEquals("s/", tile.getCLISymbol(), "The CLI Symbol for East and South should be 's/'.");
    }

    @Test
    void testConstructorAndCliSymbol_SouthWest() {
        // Arrange
        SideType[] sides = new SideType[]{SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL};
        Boolean[] protectedSides = new Boolean[]{true, false, false, true};

        // Act
        ShieldGeneratorTile tile = new ShieldGeneratorTile(sides, protectedSides);

        // Assert
        assertEquals("\\s", tile.getCLISymbol(), "The CLI Symbol for South and West should be '\\s'.");
    }

    @Test
    void testGetName() {
        // Arrange
        SideType[] sides = new SideType[]{SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL};
        Boolean[] protectedSides = new Boolean[]{true, true, false, false};
        ShieldGeneratorTile tile = new ShieldGeneratorTile(sides, protectedSides);

        // Act
        String name = tile.getName();

        // Assert
        assertEquals("Shield /s", name, "The name should include the CLI symbol, e.g., 'Shield /s'.");
    }

    @Test
    void testAccept() {
        // Arrange
        SideType[] sides = new SideType[]{SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.UNIVERSAL};
        Boolean[] protectedSides = new Boolean[]{true, false, false, true};
        ShieldGeneratorTile tile = new ShieldGeneratorTile(sides, protectedSides);

        // Create a mock visitor with a wasVisited method
        class MockTileVisitor implements TileVisitor {
            private boolean visited = false;

            @Override
            public void visitStructural(StructuralTile tile) {

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
                visited = true;
                assertSame(tile, shieldGeneratorTile, "The visitor should visit the correct ShieldGeneratorTile.");
            }

            public boolean wasVisited() {
                return visited;
            }
        }

        MockTileVisitor mockVisitor = new MockTileVisitor();

        // Act
        tile.accept(mockVisitor);

        // Assert
        assertTrue(mockVisitor.wasVisited(), "The visitor should visit the ShieldGeneratorTile.");
    }
}