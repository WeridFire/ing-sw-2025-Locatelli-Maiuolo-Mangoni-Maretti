package src.main.java.it.polimi.ingsw.shipboard.tiles.content;

import src.main.java.it.polimi.ingsw.enums.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a crew container within a tile, allowing storage and retrieval of crew items.
 * This class extends {@link TileContentContainer}
 * and provides specific implementations for managing crew items.
 */
public class TileContentCrew extends TileContentContainer {

    /**
     * Field and method to store and create a map of crew type (value) needed for a specific power bonus (key)
     */
    private static final Map<PowerType, CrewType> mapPowerCrewBonus = createMapPowerCrewBonus();
    private static Map<PowerType, CrewType> createMapPowerCrewBonus() {
        Map<PowerType, CrewType> map = new HashMap<>();
        map.put(PowerType.FIRE, CrewType.PURPLE_ALIEN);
        map.put(PowerType.THRUST, CrewType.BROWN_ALIEN);
        return map;
    }

    /**
     * Since a cabin can contain only homogeneous crew members,
     * that info is stored in {@code loadedCrewType} as indicator for cabin initialization
     * and info about initialization (-> {@code fillCrew}).<br>
     * Is {@code null} iff cabin has not been initialized yet.
     */
    private CrewType loadedCrewType = null;

    /**
     * Default constructor for a cabin: Humans as allowed loadable items and capacity = 2.
     */
    public TileContentCrew() {
        super(new HashSet<>(List.of(CrewType.HUMAN)), 2);
    }

    @Override
    public int countCrew() {
        return countLoaded(loadedCrewType);
    }

    @Override
    public void fillCrew(CrewType crewType) throws AlreadyInitializedCabinException, NotAllowedLoadableTypeException {
        if (loadedCrewType != null) {
            throw new AlreadyInitializedCabinException("Cabin has already been initialized!");
        }
        if (!isAllowed(crewType)) {
            throw new NotAllowedLoadableTypeException(crewType + " not allowed here!");
        }
        // set type
        loadedCrewType = crewType;
        // load crew with the maximum amount of members
        fillWith(loadedCrewType);
    }

    @Override
    public void removeCrew(int count) throws NotEnoughItemsException, IllegalArgumentException {
        if (count <= 0) {
            throw new IllegalArgumentException("Attempt to remove crew members less than or equal to zero!");
        }
        if (count > countLoaded()) {
            throw new NotEnoughItemsException("Attempt to remove " + count
                    + " crew members in container with " + countLoaded() + " crew members!");
        }
    }

    @Override
    public List<CrewType> updateAllowedCrew(Set<CrewType> newAllowedCrew) {
        return updateAllowedItems(newAllowedCrew.stream()
                .map(ILoadableItem.class::cast)
                .collect(Collectors.toSet()))
                .stream()
                .map(CrewType.class::cast)
                .toList();
    }

    @Override
    public float getBonusPower(PowerType powerType) {
        return (countLoaded(mapPowerCrewBonus.get(powerType)) > 0) ? 2f : 0f;
    }
}
