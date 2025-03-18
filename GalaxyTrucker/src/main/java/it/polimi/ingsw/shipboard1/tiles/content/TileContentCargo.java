package src.main.java.it.polimi.ingsw.shipboard1.tiles.content;

import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.exceptions.NotEnoughItemsException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a cargo container within a tile, allowing storage and retrieval of cargo items.
 * This class extends {@link TileContentContainer}
 * and provides specific implementations for managing cargo items.
 */
public class TileContentCargo extends TileContentContainer {

    /**
     * Constructs a {@code TileContentCargo} with the specified allowed cargo types and maximum capacity.
     *
     * @param allowedItems the set of cargo types that can be loaded into this container.
     *                     Any {@code null} values are not considered as allowed cargo.
     * @param maxCapacity  the maximum capacity in terms of occupied space.
     */
    protected TileContentCargo(Set<CargoType> allowedItems, int maxCapacity) {
        super(allowedItems.stream()
                        .map(CargoType.class::cast)
                        .collect(Collectors.toSet()),
                maxCapacity);
    }

    /**
     * Creates a standard cargo hold that allows Blue, Green, and Yellow goods.
     *
     * @param maxCapacity the maximum capacity of the cargo hold.
     * @return a new instance of {@code TileContentCargo} configured for standard cargo.
     */
    public static TileContentCargo createCargoHoldContent(int maxCapacity) {
        return new TileContentCargo(new HashSet<>(
                List.of(
                        CargoType.BLUE_GOODS,
                        CargoType.GREEN_GOODS,
                        CargoType.YELLOW_GOODS
                )),
                maxCapacity);
    }

    /**
     * Creates a special cargo hold that allows Blue, Green, Yellow, and Red goods.
     *
     * @param maxCapacity the maximum capacity of the special cargo hold.
     * @return a new instance of {@code TileContentCargo} configured for special cargo.
     */
    public static TileContentCargo createSpecialCargoHoldContent(int maxCapacity) {
        return new TileContentCargo(new HashSet<>(
                List.of(
                        CargoType.BLUE_GOODS,
                        CargoType.GREEN_GOODS,
                        CargoType.YELLOW_GOODS,
                        CargoType.RED_GOODS
                )),
                maxCapacity);
    }

    @Override
    public void removeCargo(CargoType cargo) throws NotEnoughItemsException {
        if (!remove(cargo)) {
            throw new NotEnoughItemsException("Attempt to remove cargo " + cargo
                    + " in tile with this loaded items: " + getLoadedItems());
        }
    }
}
