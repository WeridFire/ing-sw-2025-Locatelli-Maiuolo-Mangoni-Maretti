package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.*;

import java.util.ArrayList;
import java.util.List;
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
     * By default, no life support is provided. Subclasses can override this method
     * to specify life support for certain crew types.
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
     * By default, this method returns 0. Subclasses should override it if the tile can hold crew.
     * </p>
     *
     * @return the number of crew members on this tile.
     */
    public int countCrew() {
        return 0;
    }

    /**
     * Ignores any loaded crew members and fills the content with the maximum number of specified crew members.
     * <p>
     * By default, this operation is not supported and throws an exception.
     * Subclasses that support crew should override this method.
     * </p>
     *
     * @param crewType the crew type of the members to fill the tile with.
     * @throws UnsupportedLoadableItemException if the tile does not support crew loading.
     */
    public void fillCrew(CrewType crewType) throws UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Crew loading!");
    }

    /**
     * Removes a specified number of crew members from this tile.
     * <p>
     * By default, this operation is not supported and throws an exception.
     * Subclasses that support crew should override this method.
     * </p>
     *
     * @param count the number of crew members to remove.
     * @throws NotEnoughItemsException if there are not enough crew members to remove.
     * @throws UnsupportedLoadableItemException if the tile does not support crew loading.
     */
    public void removeCrew(int count) throws NotEnoughItemsException, UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Crew loading!");
    }

    /**
     * Updates the allowed crew types for this tile.
     * <p>
     * By default, this operation is not supported and throws an exception.
     * Subclasses that allow crew should override this method.
     * </p>
     *
     * @param newAllowedCrew the set of allowed crew types.
     * @throws UnsupportedLoadableItemException if the tile does not support crew loading.
     */
    public void updateAllowedCrew(Set<CrewType> newAllowedCrew) throws UnsupportedLoadableItemException {
        throw new UnsupportedLoadableItemException("This tile does not support Crew loading!");
    }

    /**
     * Returns the bonus power provided by this tile for a given power type.
     * <p>
     * By default, no bonus power is provided. Subclasses tiles that give power should override this method.
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
     * By default, this method returns {@code false}. Subclasses should override this method
     * if the tile serves as the main cabin.
     * </p>
     *
     * @return {@code true} if this tile is the main cabin, {@code false} otherwise.
     */
    public boolean isMainCabin() {
        return false;
    }

    // Cargo content

    /**
     * Returns the most valuable cargo items present on this tile, up to a given limit, in descending order.
     * <p>
     * By default, this method returns an empty list. Subclasses should override it if the tile can store cargo.
     * </p>
     *
     * @param limit the maximum number of cargo items to return.
     * @return a list of the most valuable cargo items, ordered by value in descending order.
     */
    public List<CargoType> getMostValuableCargo(int limit) {
        return new ArrayList<>(0);
    }

    /**
     * Calculates the total selling price of all cargo stored on this tile.
     * <p>
     * By default, this method returns 0. Subclasses that store cargo should override it.
     * </p>
     *
     * @return the total selling price of the cargo.
     */
    public int calculateCargoSellingPrice() {
        return 0;
    }

    /**
     * Removes a specified cargo item from this tile.
     * <p>
     * By default, this operation is not supported and throws an exception.
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
}

