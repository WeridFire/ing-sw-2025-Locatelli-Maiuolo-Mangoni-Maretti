package src.main.java.it.polimi.ingsw.shipboard1.tiles.content;

import src.main.java.it.polimi.ingsw.enums.CrewType;

/**
 * Represents a tile content that provides life support for a specific crew type.
 * This class extends {@link TileContent} and defines the type of crew that can
 * receive life support from this tile.
 */
public class TileContentLifeSupport extends TileContent {

    /**
     * The type of crew that this tile provides life support for.
     */
    private final CrewType providedLifeSupport;

    /**
     * Constructs a {@code TileContentLifeSupport} with the specified crew type.
     *
     * @param providedLifeSupport the type of crew that receives life support from this tile.
     * @throws IllegalArgumentException if {@code providedLifeSupport} has not a supported value.
     * Supported values are:
     * <ul>
     *     <li>Purple Alien</li>
     *     <li>Brown Alien</li>
     * </ul>
     */
    public TileContentLifeSupport(CrewType providedLifeSupport) throws IllegalArgumentException {
        if (providedLifeSupport == CrewType.PURPLE_ALIEN
                || providedLifeSupport == CrewType.BROWN_ALIEN) {
            this.providedLifeSupport = providedLifeSupport;
        }
        else {
            throw new IllegalArgumentException("Provided life support ("
                    + providedLifeSupport + ") is not a valid crew type!");
        }
    }

    /**
     * Gets the type of crew that receives life support from this tile in adjacent cabins.
     *
     * @return the crew type that this tile supports.
     */
    @Override
    public CrewType getProvidedLifeSupport() {
        return providedLifeSupport;
    }
}

