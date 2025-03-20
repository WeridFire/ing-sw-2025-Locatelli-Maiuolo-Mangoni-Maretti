package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;

import java.util.Set;

public class SpecialCargoHoldTile extends CargoHoldTile {

    /**
     * Constructs a CargoHoldTile with specified sides and capacity.
     *
     * @param sides    An array of sides defining the structure of the tile.
     * @param capacity The maximum capacity of the cargo hold.
     */
    public SpecialCargoHoldTile(SideType[] sides, int capacity) {
        super(sides, Set.of(LoadableType.BLUE_GOODS, LoadableType.GREEN_GOODS,
                LoadableType.YELLOW_GOODS, LoadableType.RED_GOODS), capacity);
    }

}
