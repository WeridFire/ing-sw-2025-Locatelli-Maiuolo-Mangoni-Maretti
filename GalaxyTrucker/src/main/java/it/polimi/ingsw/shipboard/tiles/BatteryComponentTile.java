package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;

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
        switch(capacity){
            case 2 -> setCLISymbol("B2");
            case 3 -> setCLISymbol("B3");
        }
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

    @Override
    public String getName() {
        return "Battery Tile " + getLoadedItems().size() + "/" + getCapacity();
    }
}
