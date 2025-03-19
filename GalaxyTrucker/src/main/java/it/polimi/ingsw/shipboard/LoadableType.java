package src.main.java.it.polimi.ingsw.shipboard;

/**
 * Represents different types of loadable items that can be stored in a container (onto a tile).
 */
public enum LoadableType {
    /** A battery. */
    BATTERY,

    /** Blue goods. */
    BLUE_GOODS,
    /** Green goods. */
    GREEN_GOODS,
    /** Yellow goods. */
    YELLOW_GOODS,
    /** Red goods. Need special cargo hold to be stored. */
    RED_GOODS,

    /** A human crew member. */
    HUMAN,

    /** A purple alien crew member, for firepower bonus. */
    PURPLE_ALIEN,

    /** A brown alien crew member, for thrust power bonus. */
    BROWN_ALIEN;

    /**
     * Retrieves the required capacity for storing this item.
     *
     * @return The amount of capacity needed to store one unit of this item.
     */
    public int getRequiredCapacity() {
        return switch (this) {
            case PURPLE_ALIEN, BROWN_ALIEN -> 2;
            default -> 1;
        };
    }
}

