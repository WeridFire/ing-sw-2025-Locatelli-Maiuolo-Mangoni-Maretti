package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Set;

public class BatteryComponentTile extends ContainerTile {

    /**
     * Constructs a BatteryComponentTile with specified sides and capacity.
     * This battery component can store up to {@code capacity} battery charges.
     *
     * @param sides An array of sides defining the structure of the tile.
     * @param capacity The maximum capacity of the battery component.
     */
    public BatteryComponentTile(SideType[] sides, int capacity) {
        super(sides, Set.of(LoadableType.BATTERY), Set.of(LoadableType.BATTERY), capacity);
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitBatteryComponent(this);
    }

    /**
     * Initialize the battery component and fills it with {@link LoadableType#BATTERY} based on its capacity.
     */
    public void fill() {
        int quantity = getCapacityLeft() / LoadableType.BATTERY.getRequiredCapacity();
        try {
            loadItems(LoadableType.BATTERY, quantity);
        } catch (TooMuchLoadException | UnsupportedLoadableItemException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
    }
}
