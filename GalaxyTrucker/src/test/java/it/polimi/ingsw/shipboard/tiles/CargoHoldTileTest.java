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

            //Creates a regular double cargo tile
        SideType[] regularDoubleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.SINGLE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);


        regularDoubleCargoHoldTile = new CargoHoldTile(regularDoubleCargo, 2);

            //Creates a regular Triple cargo tile
            SideType[] regularTripleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.DOUBLE,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        regularTripleCargoHoldTile = new CargoHoldTile(regularTripleCargo, 3);

        //Creates a special cargo tile
        SideType[] specialSingleCargo = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.SMOOTH,     // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        specialSingleCargoHoldTile = new CargoHoldTile(specialSingleCargo, allowedCargoTest,1);

        //Creates a special double cargo tile
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

        //Test CLI symbols
        assertEquals("2+", regularDoubleCargoHoldTile.getCLISymbol());
        assertEquals("3+", regularTripleCargoHoldTile.getCLISymbol());
        assertEquals("?", specialSingleCargoHoldTile.getCLISymbol());
    }

    @Test
    void testCapacity() throws TooMuchLoadException, UnsupportedLoadableItemException {

        //Test list
        List<LoadableType> testList = new ArrayList<>();
        testList.add(LoadableType.GREEN_GOODS);
        testList.add(LoadableType.GREEN_GOODS);

        //Load goods on tile
        regularDoubleCargoHoldTile.loadItems(LoadableType.GREEN_GOODS, 2);

        //Capacity left should be 0
        assertEquals(0, regularDoubleCargoHoldTile.getCapacityLeft());

        //Tile content Should be equal to testList
        assertEquals(testList.toString(), regularDoubleCargoHoldTile.getLoadedItems().toString());
    }

    @Test
    void RedCantGetLoadedInRegular() {

        //Tries to load red goods on regular double tile
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            regularDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        });

        //Tries to load red goods on regular triple tile
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            regularTripleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        });
    }

    @Test
    void RedCanGetLoadedInSpecial() throws TooMuchLoadException, UnsupportedLoadableItemException {

        //Test capacity before loading
        assertEquals(1, specialSingleCargoHoldTile.getCapacityLeft());
        assertEquals(2, specialDoubleCargoHoldTile.getCapacityLeft());

        //Load 1 red goods in each tile
        specialSingleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);
        specialDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);

        //Check capacity after loading, should be 0 for single and 1 for double
        assertEquals(0, specialSingleCargoHoldTile.getCapacityLeft());
        assertEquals(1, specialDoubleCargoHoldTile.getCapacityLeft());
    }

    @Test
    void getMostValuableTest() throws TooMuchLoadException, UnsupportedLoadableItemException {

            //Creates a priority queue for testing reasons
            PriorityQueue testQueue = new PriorityQueue();
            testQueue.add(LoadableType.RED_GOODS);

            //Adds 1 blue goods and 1 red goods on the tile, in this order
            specialDoubleCargoHoldTile.loadItems(LoadableType.BLUE_GOODS, 1);
            specialDoubleCargoHoldTile.loadItems(LoadableType.RED_GOODS, 1);

            //Should return the most valuable good (red)
            assertEquals(testQueue.toString(), specialDoubleCargoHoldTile.getContrabandMostValuableItems(1, 2).toString());

    }
}