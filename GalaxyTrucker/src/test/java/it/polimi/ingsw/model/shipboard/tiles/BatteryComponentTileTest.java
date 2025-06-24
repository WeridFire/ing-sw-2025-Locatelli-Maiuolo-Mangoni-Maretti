package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.tiles.BatteryComponentTile;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatteryComponentTileTest {

    private BatteryComponentTile doubleBatteryTile;
    private BatteryComponentTile tripleBatteryTile;

    @BeforeEach
    void setUp() {

        //Creates a double battery tile
        SideType[] doubleBattery = Direction.sortedArray(
                SideType.UNIVERSAL,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        doubleBatteryTile = new BatteryComponentTile(doubleBattery, 2);

        //Creates a triple battery tile
        SideType[] tripleBattery = Direction.sortedArray(
                SideType.SINGLE,  // NORTH
                SideType.UNIVERSAL,  // EAST
                SideType.UNIVERSAL,  // SOUTH
                SideType.UNIVERSAL   // WEST
        ).toArray(SideType[]::new);

        tripleBatteryTile = new BatteryComponentTile(tripleBattery, 3);

    }

    @Test
    void testConstructor(){

        //Checks that both tiles are correctly instantiated
        assertNotNull(doubleBatteryTile);
        assertNotNull(tripleBatteryTile);

        assertEquals("B2", doubleBatteryTile.getCLISymbol());
        assertEquals("B3", tripleBatteryTile.getCLISymbol());

    }

    @Test
    void testFill(){

        //fills tiles, then checks that capacity is 0
        doubleBatteryTile.fill();
        assertEquals(0, doubleBatteryTile.getCapacityLeft());

        tripleBatteryTile.fill();
        assertEquals(0, tripleBatteryTile.getCapacityLeft());

    }

    @Test
    void testRemoveOneBattery() throws IllegalArgumentException, UnsupportedLoadableItemException, NotEnoughItemsException {

        //Fills tile
        doubleBatteryTile.fill();
        assertEquals(0, doubleBatteryTile.getCapacityLeft());

        //Removes 1 battery
        doubleBatteryTile.removeItems(LoadableType.BATTERY, 1);
        assertEquals(1, doubleBatteryTile.getCapacityLeft());

        //Fills tile
        tripleBatteryTile.fill();
        assertEquals(0, tripleBatteryTile.getCapacityLeft());

        //Removes 1 battery
        tripleBatteryTile.removeItems(LoadableType.BATTERY, 1);
        assertEquals(1, tripleBatteryTile.getCapacityLeft());

    }

    @Test
    void testRemoveTwoBatteries() throws IllegalArgumentException, UnsupportedLoadableItemException, NotEnoughItemsException {

        //Fills tile
        doubleBatteryTile.fill();
        assertEquals(0, doubleBatteryTile.getCapacityLeft());

        //Removes 1 battery
        doubleBatteryTile.removeItems(LoadableType.BATTERY, 2);
        assertEquals(2, doubleBatteryTile.getCapacityLeft());

        //Fills tile
        tripleBatteryTile.fill();
        assertEquals(0, tripleBatteryTile.getCapacityLeft());

        //Removes 2 batteries
        tripleBatteryTile.removeItems(LoadableType.BATTERY, 2);
        assertEquals(2, tripleBatteryTile.getCapacityLeft());

    }

    @Test
    void testRemoveThreeBatteries() throws IllegalArgumentException, UnsupportedLoadableItemException, NotEnoughItemsException {

        //Fills tile
        doubleBatteryTile.fill();
        assertEquals(0, doubleBatteryTile.getCapacityLeft());

        //Removes 3 batteries
        assertThrowsExactly(NotEnoughItemsException.class,
                () -> doubleBatteryTile.removeItems(LoadableType.BATTERY, 3));
        assertEquals(0, doubleBatteryTile.getCapacityLeft());

        //Fills tile
        tripleBatteryTile.fill();
        assertEquals(0, tripleBatteryTile.getCapacityLeft());

        //Removes 3 batteries
        tripleBatteryTile.removeItems(LoadableType.BATTERY, 3);
        assertEquals(3, tripleBatteryTile.getCapacityLeft());

    }

    @Test
    void doubleTileCantHoldMore() throws TooMuchLoadException, UnsupportedLoadableItemException {

        //Should throw
        assertThrows(TooMuchLoadException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.BATTERY, 3);
        });

    }

    @Test
    void tripleTileCantHoldMore() throws TooMuchLoadException, UnsupportedLoadableItemException {

        //Should throw
        assertThrows(TooMuchLoadException.class, () -> {
            tripleBatteryTile.loadItems(LoadableType.BATTERY, 4);
        });

    }

    @Test
    void batteryCantHoldCargo() throws TooMuchLoadException, UnsupportedLoadableItemException {

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            tripleBatteryTile.loadItems(LoadableType.BLUE_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            tripleBatteryTile.loadItems(LoadableType.GREEN_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            tripleBatteryTile.loadItems(LoadableType.YELLOW_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            tripleBatteryTile.loadItems(LoadableType.RED_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.BLUE_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.GREEN_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.YELLOW_GOODS, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.RED_GOODS, 1);
        });

    }

    @Test
    void batteryCantHoldCrew() throws TooMuchLoadException, UnsupportedLoadableItemException {

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.HUMAN, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.BROWN_ALIEN, 1);
        });

        //Should throw
        assertThrows(UnsupportedLoadableItemException.class, () -> {
            doubleBatteryTile.loadItems(LoadableType.PURPLE_ALIEN, 1);
        });

    }
}