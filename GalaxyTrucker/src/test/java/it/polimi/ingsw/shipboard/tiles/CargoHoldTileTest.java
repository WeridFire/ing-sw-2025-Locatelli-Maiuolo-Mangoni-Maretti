package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CargoHoldTileTest {

    private CargoHoldTile regularDoubleCargoHoldTile;
    private CargoHoldTile regularTripleCargoHoldTile;
    private CargoHoldTile specialSingleCargoHoldTile;
    private CargoHoldTile specialDoubleCargoHoldTile;

    @BeforeEach
    void setUp() {
        SideType[] regularDoubleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.SINGLE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        regularDoubleCargoHoldTile = new CargoHoldTile(regularDoubleCargo, 2);

        SideType[] regularTripleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.DOUBLE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        regularTripleCargoHoldTile = new CargoHoldTile(regularTripleCargo, 3);

        SideType[] specialSingleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.SMOOTH,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        specialSingleCargoHoldTile = new CargoHoldTile(specialSingleCargo, 1);

        SideType[] specialDoubleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        specialDoubleCargoHoldTile = new CargoHoldTile(specialDoubleCargo, 2);

    }

    @Test
    void testConstructor() {
        assertEquals("2+", regularDoubleCargoHoldTile.getCLISymbol());
        assertEquals("3+", regularTripleCargoHoldTile.getCLISymbol());
    }

    @Test
    void testCapacity() throws TooMuchLoadException, UnsupportedLoadableItemException {
        List<LoadableType> testList = new ArrayList<>();
        testList.add(LoadableType.GREEN_GOODS);
        testList.add(LoadableType.GREEN_GOODS);
        regularDoubleCargoHoldTile.loadItems(LoadableType.GREEN_GOODS, 2);
        assertEquals(0, regularDoubleCargoHoldTile.getCapacityLeft());
        assertEquals(testList.toString(), regularDoubleCargoHoldTile.getLoadedItems().toString());
    }

}