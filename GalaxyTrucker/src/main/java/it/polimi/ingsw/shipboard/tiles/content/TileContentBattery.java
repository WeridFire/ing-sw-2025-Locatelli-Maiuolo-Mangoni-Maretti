package src.main.java.it.polimi.ingsw.shipboard.tiles.content;

import src.main.java.it.polimi.ingsw.enums.BatteryType;
import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a tile content that can store battery items.
 * This container allows only specific types of batteries and has a limited capacity.
 */
public class TileContentBattery extends TileContentContainer {

    /**
     * Constructs a battery content container with the specified allowed battery types and maximum capacity.
     *
     * @param allowedItems The set of battery types that can be stored in this container.
     * @param maxCapacity  The maximum capacity in terms of occupied space.
     */
    protected TileContentBattery(Set<BatteryType> allowedItems, int maxCapacity) {
        super(allowedItems.stream()
                        .map(CargoType.class::cast)
                        .collect(Collectors.toSet()),
                maxCapacity);
    }

    /**
     * Factory method to create a battery storage component.
     * This method creates a battery container that can hold only standard batteries.
     *
     * @param maxCapacity The maximum capacity of the battery container.
     * @return A new instance of {@code TileContentBattery}.
     */
    public static TileContentBattery createBatteryComponentContent(int maxCapacity) {
        return new TileContentBattery(new HashSet<>(List.of(BatteryType.BATTERY)), maxCapacity);
    }

    @Override
    public boolean hasBattery(BatteryType batteryType) {
        return countLoaded(batteryType) > 0;
    }

    @Override
    public void removeBattery(BatteryType battery) throws NotEnoughItemsException {
        if (!remove(battery)) {
            throw new NotEnoughItemsException("Attempt to remove battery " + battery
                    + " in tile with this loaded items: " + getLoadedItems());
        }
    }
}
