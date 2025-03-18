package src.main.java.it.polimi.ingsw.shipboard1.tiles.content;

import src.main.java.it.polimi.ingsw.enums.*;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.exceptions.*;
import src.main.java.it.polimi.ingsw.util.ContrabandCalculator;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Represents the default content of a tile in the game.
 * <p>
 * This base class provides default implementations for various tile functionalities,
 * assuming the tile does not contain any specific crew, cargo, or structural attributes.
 * Other classes can extend this class and override methods to implement specialized behavior.
 * </p>
 */
public class TileContent {

    // Structural content

    /**
     * Returns the type of crew that receives life support from this tile when placed in an adjacent cabin.
     * <p>
     * By default, no life support is provided (return {@code null}).
     * Subclasses can override this method to specify life support for certain crew types.
     * </p>
     *
     * @return the {@link CrewType} that receives life support, or {@code null} if none.
     */
    public CrewType getProvidedLifeSupport() {
        return null;
    }

    // Crew content

    /**
     * Returns the number of crew members present on this tile.
     * <p>
     * By default, this method returns {@code 0}.
     * Subclasses should override it if the tile can hold crew.
     * </p>
     *
     * @return the number of crew members on this tile.
     */
    public int countCrew() {
        return 0;
    }

    /**
     * Fills the content with the maximum number of specified crew members.
     * <p>
     * By default, this operation is not supported and throws an exception: {@link UnsupportedLoadableItemException}.
     * Subclasses that support crew should override this method.
     * </p>
     *
     * @param crewType the crew type of the members to fill the tile with.
     * @throws UnsupportedLoadableItemException if the tile does not support crew loading.
     * @throws AlreadyInitializedCabinException if the tile does support crew loading
     * but has already been initialized.
     * @throws NotAllowedLoadableTypeException if the tile does support crew loading
     * does not allow {@code crewType} crew members.
     */
    public void fillCrew(CrewType crewType) throws UnsupportedLoadableItemException, AlreadyInitializedCabinException,
            NotAllowedLoadableTypeException {
        throw new UnsupportedLoadableItemException("This tile does not support Crew loading!");
    }

    /**
     * Removes a specified number of crew members from this tile.
     * <p>
     * By default, this operation is not supported and throws an exception: {@link UnsupportedLoadableItemException}.
     * Subclasses that support crew should override this method.
     * </p>
     *
     * @param count the number of crew members to remove.
     * @throws NotEnoughItemsException if there are not enough crew members to remove.
     * @throws UnsupportedLoadableItemException if the tile does not support crew loading.
     * @throws IllegalArgumentException if {@code count} is less than or equal to zero.
     */
    public void removeCrew(int count) throws NotEnoughItemsException, UnsupportedLoadableItemException,
            IllegalArgumentException {
        throw new UnsupportedLoadableItemException("This tile does not support Crew loading!");
    }

    /**
     * Updates the allowed crew types for this tile.
     * <p>
     * By default, this operation is not supported and throws an exception: {@link UnsupportedLoadableItemException}.
     * Subclasses that allow crew should override this method.
     * </p>
     *
     * @param newAllowedCrew the set of allowed crew types.
     * @return a list of all the crew members in this tile removed by this method.
     * @throws UnsupportedLoadableItemException if the tile does not support crew loading.
     */
    public List<CrewType> updateAllowedCrew(Set<CrewType> newAllowedCrew) throws UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Crew loading!");
    }

    /**
     * Returns the bonus power provided by this tile for a given power type.
     * <p>
     * By default, no bonus power is provided (return {@code 0}).
     * Subclasses tiles that give power should override this method.
     * </p>
     *
     * @param powerType the type of power.
     * @return the amount of bonus power provided.
     */
    public float getBonusPower(PowerType powerType) {
        return 0;
    }

    /**
     * Checks if this tile represents the main cabin of the ship.
     * <p>
     * By default, this method returns {@code false}.
     * Subclasses should override this method if the tile serves as the main cabin.
     * </p>
     *
     * @return {@code true} if this tile is the main cabin, {@code false} otherwise.
     */
    public boolean isMainCabin() {
        return false;
    }

    // Cargo content

    /**
     * @see #getContrabandMostValuableItems(int limit)
     * @param minimumContrabandValueExclusive the minimum contraband value (exclusive) to get items.
     *        Any item with contraband value below or equal to
     *        {@code minimumContrabandValueExclusive} is not considered.
     * @throws IllegalArgumentException if {@code minimumContrabandValueExclusive < 0}
     */
    public PriorityQueue<ILoadableItem> getContrabandMostValuableItems(int limit, int minimumContrabandValueExclusive)
            throws IllegalArgumentException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        if (minimumContrabandValueExclusive < 0) {
            throw new IllegalArgumentException("Minimum contraband value (exclusive) must be greater or equal to 0");
        }
        return new PriorityQueue<>(0, ContrabandCalculator.descendingContrabandComparator);
    }

    /**
     * Returns the most valuable loaded items present on this tile,
     * for smugglers, up to a given limit, in descending order.
     * <p>
     * By default, this method returns an empty queue.
     * Subclasses that store contraband items should override it.
     * </p>
     *
     * @param limit the maximum number of items to return.
     * @return a priority queue of the most valuable loaded items, ordered by value in descending order.
     * @throws IllegalArgumentException if {@code limit <= 0}
     */
    public PriorityQueue<ILoadableItem> getContrabandMostValuableItems(int limit) throws IllegalArgumentException {
        return getContrabandMostValuableItems(limit, 0);
    }

    /**
     * Calculates the total selling price of all the loaded items present on this tile.
     * <p>
     * By default, this method returns 0.
     * Subclasses that store saleable items should override it.
     * </p>
     *
     * @return the total selling price of the loaded items.
     */
    public int calculateItemsSellingPrice() {
        return 0;
    }

    /**
     * Attempts to add cargo to this tile.
     * <p>
     * By default, this operation is not supported and throws an exception: {@link UnsupportedLoadableItemException}.
     * Subclasses that support cargo should override this method.
     * </p>
     *
     * @param cargo The list of cargo items to be added.
     * @throws TooMuchLoadException If the cargo exceeds the allowed load capacity (only applicable in overriding implementations).
     * @throws NotAllowedLoadableTypeException If the cargo type is not allowed (only applicable in overriding implementations).
     * @throws UnsupportedLoadableItemException If this tile does not support cargo loading.
     */
    public void addCargo(List<CargoType> cargo) throws TooMuchLoadException, NotAllowedLoadableTypeException,
            UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Cargo loading!");
    }

    /**
     * Removes a specified cargo item from this tile.
     * <p>
     * By default, this operation is not supported and throws an exception: {@link UnsupportedLoadableItemException}.
     * Subclasses that support cargo should override this method.
     * </p>
     *
     * @param cargo the cargo type to remove.
     * @throws NotEnoughItemsException if the specified cargo is not present.
     * @throws UnsupportedLoadableItemException if the tile does not support cargo loading.
     */
    public void removeCargo(CargoType cargo) throws NotEnoughItemsException, UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Cargo loading!");
    }

    /**
     * Checks if there are batteries of the specified type stored in the container.
     * <p>
     * By default, this method returns {@code 0}.
     * Subclasses should override this method if the tile can contain some battery type.
     * </p>
     *
     * @param batteryType the type of battery to check.
     * @return The number of batteries of the specified type stored in the container.
     */
    public int countBatteries(BatteryType batteryType) {
        return 0;
    }

    /**
     * Removes one unit of the specified battery type from the container.
     * <p>
     * By default, this operation is not supported and throws an exception: {@link UnsupportedLoadableItemException}.
     * Subclasses that support battery should override this method.
     * </p>
     *
     * @param battery the type of battery to remove.
     * @throws NotEnoughItemsException if there are no batteries of the specified type available for removal.
     * @throws UnsupportedLoadableItemException if the tile does not support battery loading.
     */
    public void removeBattery(BatteryType battery) throws NotEnoughItemsException, UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Battery loading!");
    }


}

