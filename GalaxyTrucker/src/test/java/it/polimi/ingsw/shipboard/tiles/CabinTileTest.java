package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.shipboard.tiles.exceptions.AlreadyInitializedCabinException;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CabinTileTest {

    CabinTile cabin = new CabinTile(Direction.sortedArray(
            SideType.SINGLE, SideType.DOUBLE, SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new));
    CabinTile mainCabin = TilesFactory.createMainCabinTile();
    CabinTile cabinBrown = new CabinTile(Direction.sortedArray(
            SideType.SINGLE, SideType.DOUBLE, SideType.SMOOTH, SideType.SMOOTH).toArray(SideType[]::new));
    CabinTile cabinPurple = new CabinTile(Direction.sortedArray(
            SideType.SINGLE, SideType.DOUBLE, SideType.SMOOTH, SideType.SMOOTH).toArray(SideType[]::new));


    @Test
    public void testFillHumans() {
        assertDoesNotThrow(() -> cabin.fillWith(LoadableType.HUMAN));
        assertArrayEquals(new LoadableType[] {LoadableType.HUMAN, LoadableType.HUMAN},
                cabin.getLoadedItems().toArray());
    }

    @Test
    public void testInvalidDoubleFill() {
        assertDoesNotThrow(() -> cabin.fillWith(LoadableType.HUMAN));
        assertThrowsExactly(AlreadyInitializedCabinException.class,
                () -> cabin.fillWith(LoadableType.HUMAN));
        assertThrowsExactly(AlreadyInitializedCabinException.class,
                () -> cabin.fillWith(LoadableType.PURPLE_ALIEN));
        assertThrowsExactly(AlreadyInitializedCabinException.class,
                () -> cabin.fillWith(LoadableType.BROWN_ALIEN));
    }

    @Test
    public void testFillPurpleAlien() {
        // invalid fill if no life support set
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabinPurple.fillWith(LoadableType.PURPLE_ALIEN));
        // set purple alien as allowed
        Set<LoadableType> cabinPurpleAllowedCrew = cabinPurple.getAllowedItems();
        cabinPurpleAllowedCrew.add(LoadableType.PURPLE_ALIEN);
        assertDoesNotThrow(() -> cabinPurple.setAllowedItems(cabinPurpleAllowedCrew));
        // not invalid anymore
        assertDoesNotThrow(() -> cabinPurple.fillWith(LoadableType.PURPLE_ALIEN));
        assertArrayEquals(new LoadableType[] {LoadableType.PURPLE_ALIEN},
                cabinPurple.getLoadedItems().toArray());
    }

    @Test
    public void testFillBrownAlien() {
        // invalid fill if no life support set
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabinBrown.fillWith(LoadableType.BROWN_ALIEN));
        // set brown alien as allowed
        Set<LoadableType> cabinBrownAllowedCrew = cabinBrown.getAllowedItems();
        cabinBrownAllowedCrew.add(LoadableType.BROWN_ALIEN);
        assertDoesNotThrow(() -> cabinBrown.setAllowedItems(cabinBrownAllowedCrew));
        // not invalid anymore
        assertDoesNotThrow(() -> cabinBrown.fillWith(LoadableType.BROWN_ALIEN));
        assertArrayEquals(new LoadableType[] {LoadableType.BROWN_ALIEN},
                cabinBrown.getLoadedItems().toArray());
    }

    @Test
    public void testNoAliensInMainCabin() {
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.fillWith(LoadableType.PURPLE_ALIEN));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.fillWith(LoadableType.BROWN_ALIEN));
        assertDoesNotThrow(() -> mainCabin.fillWith(LoadableType.HUMAN));
    }

    @Test
    public void testInvalidFills() {
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.fillWith(LoadableType.BLUE_GOODS));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.fillWith(LoadableType.GREEN_GOODS));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.fillWith(LoadableType.YELLOW_GOODS));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.fillWith(LoadableType.RED_GOODS));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.fillWith(LoadableType.BATTERY));
        assertThrowsExactly(NullPointerException.class,
                () -> cabin.fillWith(null));
    }

    @Test
    public void testChangesOfAllowedTypes() {
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.setAllowedItems(Set.of(LoadableType.BLUE_GOODS)));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> cabin.setAllowedItems(Set.of(LoadableType.HUMAN, LoadableType.BLUE_GOODS)));
        assertDoesNotThrow(() -> cabin.setAllowedItems(Set.of(LoadableType.HUMAN)));

        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.setAllowedItems(Set.of(LoadableType.BLUE_GOODS)));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.setAllowedItems(Set.of(LoadableType.BLUE_GOODS, LoadableType.HUMAN)));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.setAllowedItems(Set.of(LoadableType.PURPLE_ALIEN, LoadableType.HUMAN)));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.setAllowedItems(Set.of(LoadableType.PURPLE_ALIEN)));
        assertThrowsExactly(UnsupportedLoadableItemException.class,
                () -> mainCabin.setAllowedItems(Set.of(LoadableType.BROWN_ALIEN)));
        assertDoesNotThrow(() -> cabin.setAllowedItems(Set.of(LoadableType.HUMAN)));
    }

    @Test
    public void testContraband() {
        assertEquals(2, cabin.getCapacityLeft());
        assertDoesNotThrow(() -> cabin.fillWith(LoadableType.HUMAN));
        assertEquals(0, cabin.getCapacityLeft());
        assertEquals(0, cabin.getContrabandMostValuableItems(10, 0).size());
    }

    @Test
    public void testFixTile() {
        assertEquals(2, cabin.getCapacityLeft());
        assertDoesNotThrow(() -> cabin.setAllowedItems(LoadableType.CREW_SET));
        assertDoesNotThrow(() -> cabin.fillWith(LoadableType.PURPLE_ALIEN));
        assertEquals(0, cabin.getCapacityLeft());
        assertThrowsExactly(NotFixedTileException.class, () -> cabin.getCoordinates());
        assertThrowsExactly(NullPointerException.class, () -> cabin.place(null));
        assertThrowsExactly(NotFixedTileException.class, () -> cabin.getCoordinates());
        assertDoesNotThrow(() -> cabin.place(new Coordinates(4, 20)));
        assertDoesNotThrow(() -> assertEquals(new Coordinates(4, 20), cabin.getCoordinates()));
        assertThrowsExactly(FixedTileException.class, () -> cabin.place(new Coordinates(6, 9)));
        assertThrowsExactly(FixedTileException.class, () -> cabin.place(new Coordinates(4, 20)));
    }

    @Test
    public void testRemoveAll() {
        assertEquals(2, cabin.getCapacityLeft());
        assertThrowsExactly(IllegalArgumentException.class, () ->
                cabin.removeAny(Arrays.stream(LoadableType.values()).collect(Collectors.toSet()),
                        cabin.getLoadedItems().size()));
        assertDoesNotThrow(() -> cabin.fillWith(LoadableType.HUMAN));
        assertEquals(0, cabin.getCapacityLeft());
        assertEquals(2, cabin.removeAny(Arrays.stream(LoadableType.values()).collect(Collectors.toSet()),
                cabin.getLoadedItems().size()));
        assertEquals(2, cabin.getCapacityLeft());
    }

    @Test
    public void test() {

    }

}