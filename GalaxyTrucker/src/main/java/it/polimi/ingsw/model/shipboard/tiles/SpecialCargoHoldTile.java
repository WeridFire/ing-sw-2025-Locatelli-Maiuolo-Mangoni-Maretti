package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.SideType;

import java.util.Set;
import java.util.stream.Collectors;

public class SpecialCargoHoldTile extends CargoHoldTile {

    /**
     * Constructs a Special CargoHoldTile with specified sides and capacity.
     * This special cargo hold can contain any type of goods: Blue, Yellow, Green and Red.
     *
     * @param sides An array of sides defining the structure of the tile.
     * @param capacity The maximum capacity of the special cargo hold.
     */
    public SpecialCargoHoldTile(SideType[] sides, int capacity) {
        super(sides, Set.of(LoadableType.BLUE_GOODS, LoadableType.GREEN_GOODS,
                LoadableType.YELLOW_GOODS, LoadableType.RED_GOODS), capacity);
        switch (capacity){
            case 1 -> setCLISymbol("1*");
            case 2 -> setCLISymbol("2*");
        }
    }

    @Override
    public String getName() {
        return "Special " + super.getName();
    }


}
