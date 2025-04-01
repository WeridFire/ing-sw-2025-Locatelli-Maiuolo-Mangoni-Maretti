package it.polimi.ingsw.shipboard.tiles;

import org.junit.jupiter.api.Test;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;

import static org.junit.jupiter.api.Assertions.*;

class CabinTileTest {


    @Test
    public void testFillHumans() {
        CabinTile cabin = new CabinTile(Direction.sortedArray(
                SideType.SINGLE, SideType.DOUBLE, SideType.SMOOTH, SideType.SMOOTH).toArray(SideType[]::new));

        assertDoesNotThrow(() -> cabin.fillWith(LoadableType.HUMAN));
        assertArrayEquals(new LoadableType[] {LoadableType.HUMAN, LoadableType.HUMAN},
                cabin.getLoadedItems().toArray());
    }

}