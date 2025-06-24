package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.tiles.SpecialCargoHoldTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecialCargoHoldTileTest {



    private SpecialCargoHoldTile specialSingleCargoHoldTile;
    private SpecialCargoHoldTile specialDoubleCargoHoldTile;

    @BeforeEach
    void setUp() {
        // Creates a special single cargo tile
        SideType[] specialSingleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // EAST
                SideType.SMOOTH,     // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        ).toArray(SideType[]::new);

        specialSingleCargoHoldTile = new SpecialCargoHoldTile(specialSingleCargo, 1);

        // Creates a special double cargo tile
        SideType[] specialDoubleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // WEST
                SideType.UNIVERSAL   // SOUTH
        ).toArray(SideType[]::new);

        specialDoubleCargoHoldTile = new SpecialCargoHoldTile(specialDoubleCargo, 2);
    }

    @Test
    void testConstructor() {
        // Verify CLI symbols
        assertEquals("1*", specialSingleCargoHoldTile.getCLISymbol());
        assertEquals("2*", specialDoubleCargoHoldTile.getCLISymbol());
    }


    @Test
    void testCapacityReduction() throws Exception {
        // Load 1 red good and verify capacity decrease
        specialDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        assertEquals(1, specialDoubleCargoHoldTile.getCapacityLeft());

        // Load another good and verify capacity reaches zero
        specialDoubleCargoHoldTile.loadItems(LoadableType.BLUE_GOODS, 1);
        assertEquals(0, specialDoubleCargoHoldTile.getCapacityLeft());
    }

    @Test
    void testOverCapacityLoad() {
        // Attempt to load more goods than the capacity and verify exception
        assertThrows(Exception.class, () -> {
            specialSingleCargoHoldTile.loadItems(LoadableType.GREEN_GOODS, 2);
        });
    }

    @Test
    void testGetName() {
        // Verify the name generation for the cargo hold
        assertEquals("Special Cargo Hold [] / 1", specialSingleCargoHoldTile.getName());
        assertEquals("Special Cargo Hold [] / 2", specialDoubleCargoHoldTile.getName());
    }
}