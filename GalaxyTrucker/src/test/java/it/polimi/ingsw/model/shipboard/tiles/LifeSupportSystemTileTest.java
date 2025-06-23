package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.tiles.LifeSupportSystemTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class LifeSupportSystemTileTest {

    private LifeSupportSystemTile purpleLifeSupport;
    private LifeSupportSystemTile brownLifeSupport;

    @BeforeEach
    public void setUp() {

        SideType[] purple = Direction.sortedArray(
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        ).toArray(SideType[]::new);

        // Initialize the LifeSupportSystemTile object for Purple Alien before each test
        purpleLifeSupport = new LifeSupportSystemTile(purple, LoadableType.PURPLE_ALIEN);

        SideType[] brown = Direction.sortedArray(
                SideType.SINGLE,    // EAST
                SideType.UNIVERSAL, // NORTH
                SideType.UNIVERSAL, // WEST
                SideType.UNIVERSAL  // SOUTH
        ).toArray(SideType[]::new);

        // Initialize the LifeSupportSystemTile object for Brown Alien before each test
        brownLifeSupport = new LifeSupportSystemTile(brown, LoadableType.BROWN_ALIEN);
    }

    @Test
    void testValidPurpleInitialization() {

        // Create a valid side array for the tile
        SideType[] validSides = new SideType[] {
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        };

        // Instantiate LifeSupportSystemTile with valid sides and LoadableType
        LifeSupportSystemTile tile = new LifeSupportSystemTile(validSides, LoadableType.PURPLE_ALIEN);

        // Verify that the tile is correctly initialized
        assertNotNull(tile, "The LifeSupportSystemTile should be successfully created.");
        assertEquals(LoadableType.PURPLE_ALIEN, tile.getProvidedLifeSupport(),
                "The provided life support type should match the one passed to the constructor.");
        assertEquals("HP", tile.getCLISymbol(),
                "The CLI symbol should be set to 'HP' for PURPLE_ALIEN.");
    }

    @Test
    void testValidBrownInitialization() {

        // Create a valid side array for the tile
        SideType[] validSides = new SideType[] {
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        };

        // Instantiate LifeSupportSystemTile with valid sides and LoadableType
        LifeSupportSystemTile tile = new LifeSupportSystemTile(validSides, LoadableType.BROWN_ALIEN);

        // Verify that the tile is correctly initialized
        assertNotNull(tile, "The LifeSupportSystemTile should be successfully created.");
        assertEquals(LoadableType.BROWN_ALIEN, tile.getProvidedLifeSupport(),
                "The provided life support type should match the one passed to the constructor.");
        assertEquals("HB", tile.getCLISymbol(),
                "The CLI symbol should be set to 'HP' for PURPLE_ALIEN.");
    }

    @Test
    void testInvalidInitialization() {

        // Create a valid side array for the tile
        SideType[] validSides = new SideType[] {
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        };

        // Attempt to create a LifeSupportSystemTile with an invalid LoadableType
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new LifeSupportSystemTile(validSides, LoadableType.HUMAN); // Invalid type
        }, "An IllegalArgumentException should be thrown for invalid LoadableType.");

        // Verify the exception message
        assertEquals("The life support should be for either a brown or a purple alien.",
                exception.getMessage(),
                "The exception message should indicate that the LoadableType is invalid.");
    }

    @Test
    void testNullSidesArray() {

        // Attempt to create a LifeSupportSystemTile with a null sides array
        assertThrows(IllegalArgumentException.class, () -> {
            new LifeSupportSystemTile(null, LoadableType.PURPLE_ALIEN);
        }, "A NullPointerException should be thrown for a null sides array.");
    }

    @Test
    void testNullProvidedLifeSupport() {

        // Create a valid side array for the tile
        SideType[] validSides = new SideType[] {
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        };

        // Attempt to create a LifeSupportSystemTile with a null LoadableType
        assertThrows(IllegalArgumentException.class, () -> {
            new LifeSupportSystemTile(validSides, null);
        }, "A NullPointerException should be thrown for a null LoadableType.");
    }



}