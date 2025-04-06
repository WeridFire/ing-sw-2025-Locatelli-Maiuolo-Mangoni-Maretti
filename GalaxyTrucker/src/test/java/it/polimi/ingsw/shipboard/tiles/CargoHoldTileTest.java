package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CargoHoldTileTest {

    private CargoHoldTile regularDoubleCargoHoldTile;
    private CargoHoldTile regularTripleCargoHoldTile;
    private CargoHoldTile specialSingleCargoHoldTile;
    private CargoHoldTile specialDoubleCargoHoldTile;

    Set<LoadableType> allowedCargoTest = Set.of(LoadableType.BLUE_GOODS, LoadableType.GREEN_GOODS, LoadableType.YELLOW_GOODS, LoadableType.RED_GOODS);




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

        specialSingleCargoHoldTile = new CargoHoldTile(specialSingleCargo, allowedCargoTest,1);

        SideType[] specialDoubleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        specialDoubleCargoHoldTile = new CargoHoldTile(specialDoubleCargo, allowedCargoTest,2);

    }

    @Test
    void testConstructor() {
        assertEquals("2+", regularDoubleCargoHoldTile.getCLISymbol());
        assertEquals("3+", regularTripleCargoHoldTile.getCLISymbol());
        assertEquals("?", specialSingleCargoHoldTile.getCLISymbol());
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

    @Test
    void RedCantGetLoadedInRegular() {
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            regularDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        });

        assertThrows(UnsupportedLoadableItemException.class, () -> {
            regularTripleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        });
    }

    @Test
    void RedCanGetLoadedInSpecial() throws TooMuchLoadException, UnsupportedLoadableItemException {
        assertEquals(1, specialSingleCargoHoldTile.getCapacityLeft());
        assertEquals(2, specialDoubleCargoHoldTile.getCapacityLeft());

        specialSingleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        specialDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);

        assertEquals(0, specialSingleCargoHoldTile.getCapacityLeft());
        assertEquals(1, specialDoubleCargoHoldTile.getCapacityLeft());
    }

    @Test
    void getMostValuableTest() throws TooMuchLoadException, UnsupportedLoadableItemException {
            PriorityQueue testQueue = new PriorityQueue();
            testQueue.add(LoadableType.RED_GOODS);
            specialDoubleCargoHoldTile.loadItems(LoadableType.BLUE_GOODS, 1);
            specialDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
            assertEquals(testQueue.toString(), specialDoubleCargoHoldTile.getContrabandMostValuableItems(1, 2).toString());

    }
}