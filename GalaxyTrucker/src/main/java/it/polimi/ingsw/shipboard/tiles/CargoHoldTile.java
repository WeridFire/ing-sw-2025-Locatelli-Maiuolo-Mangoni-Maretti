package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Set;

/**
 * Represents a cargo hold tile, a specific type of container tile that can store goods.
 * This tile only allows specific types of goods (blue, green, and yellow).
 */
public class CargoHoldTile extends ContainerTile {

    /**
     * Constructs a CargoHoldTile with specified sides, capacity and allowed cargo.
     *
     * @param sides An array of sides defining the structure of the tile.
     * @param allowedCargo The set of all the cargo types loadable onto this tile.
     * @param capacity The maximum capacity of the cargo hold.
     */
    protected CargoHoldTile(SideType[] sides, Set<LoadableType> allowedCargo, int capacity) {
        super(sides,
                Set.of(LoadableType.BLUE_GOODS, LoadableType.GREEN_GOODS,
                        LoadableType.YELLOW_GOODS, LoadableType.RED_GOODS),
                allowedCargo, capacity);
    }

    /**
     * Constructs a Standard CargoHoldTile with specified sides and capacity.
     * This cargo hold can contain Blue, Yellow and Green goods.
     *
     * @param sides An array of sides defining the structure of the tile.
     * @param capacity The maximum capacity of the cargo hold.
     */
    public CargoHoldTile(SideType[] sides, int capacity) {
        this(sides, Set.of(LoadableType.BLUE_GOODS, LoadableType.GREEN_GOODS, LoadableType.YELLOW_GOODS), capacity);
    }

    /**
     * Accepts a visitor for processing this tile.
     *
     * @param visitor The visitor handling this tile.
     */
    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitCargoHold(this);
    }
}
