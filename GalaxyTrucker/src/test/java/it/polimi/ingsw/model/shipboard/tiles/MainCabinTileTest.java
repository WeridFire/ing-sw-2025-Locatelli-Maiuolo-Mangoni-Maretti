package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainCabinTileTest {

    @Test
    void testValidInitializationBlue() {
        //Create a MainCabinTile with the color BLUE
        MainCabinTile tile = new MainCabinTile(MainCabinTile.Color.BLUE);

        // Verify that the tile is correctly initialized
        assertNotNull(tile, "MainCabinTile should be successfully created.");
        assertEquals("Main Cabin 0/2", tile.getName(),
                "The name should indicate the capacity usage (0/2 by default).");
        assertEquals("CB", tile.getCLISymbol().replaceAll("\u001B\\[[;\\d]*m", ""),
                "The CLI symbol should be set correctly for the Main Cabin.");
    }

    @Test
    void testValidInitializationRed() {
        //Create a MainCabinTile with the color RED
        MainCabinTile tile = new MainCabinTile(MainCabinTile.Color.RED);

        // Verify that the tile is correctly initialized
        assertNotNull(tile, "MainCabinTile should be successfully created.");
        assertEquals("Main Cabin 0/2", tile.getName(),
                "The name should indicate the capacity usage (0/2 by default).");
        assertEquals("CB", tile.getCLISymbol().replaceAll("\u001B\\[[;\\d]*m", ""),
                "The CLI symbol should be set correctly for the Main Cabin.");
    }

    @Test
    void testRejectInvalidPlayerIndex() {
        // Verify that an invalid player index throws an exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            MainCabinTile.Color.fromPlayerIndex(-1); // Invalid index
        }, "An IllegalArgumentException should be thrown for an invalid player index.");

        // Verify the exception message
        assertEquals("Invalid player index: -1. Should be between 0 and 3", exception.getMessage(),
                "The exception message should indicate the valid range for player indices.");
    }

    @Test
    void testCannotLoadAliens() {
        // Create a MainCabinTile
        MainCabinTile tile = new MainCabinTile(MainCabinTile.Color.GREEN);

        // Verify that loading aliens is not allowed
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            tile.fillWith(LoadableType.PURPLE_ALIEN); // Attempt to load an alien
        }, "Loading aliens should throw an UnsupportedLoadableItemException.");

        // Verify that loading aliens is not allowed
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            tile.fillWith(LoadableType.BROWN_ALIEN); // Attempt to load an alien
        }, "Loading aliens should throw an UnsupportedLoadableItemException.");
    }

    @Test
    void testVisitMainCabin() {
        //  Create a MainCabinTile and a mock visitor
        MainCabinTile tile = new MainCabinTile(MainCabinTile.Color.YELLOW);
        MockTileVisitor visitor = new MockTileVisitor();

        //  Call the accept method
        tile.accept(visitor);

        // Verify that the visitor's visitMainCabin method was called
        assertTrue(visitor.visitedMainCabin,
                "The visitor's visitMainCabin method should be called.");
    }

    // Mock visitor class for testing the visitor pattern
    private static class MockTileVisitor implements TileVisitor {
        boolean visitedMainCabin = false;

        @Override
        public void visitMainCabin(CabinTile tile) {
            visitedMainCabin = true;
        }

        // Implement other methods with empty bodies (not relevant for this test)
        @Override public void visitStructural(StructuralTile tile) {}
        @Override public void visitLifeSupportSystem(LifeSupportSystemTile tile) {}
        @Override public void visitCargoHold(CargoHoldTile tile) {}
        @Override public void visitCabin(CabinTile tile) {}
        @Override public void visitBatteryComponent(BatteryComponentTile tile) {}
        @Override public void visitCannon(CannonTile tile) {}
        @Override public void visitEngine(EngineTile tile) {}
        @Override public void visitShieldGenerator(ShieldGeneratorTile tile) {}
    }
}