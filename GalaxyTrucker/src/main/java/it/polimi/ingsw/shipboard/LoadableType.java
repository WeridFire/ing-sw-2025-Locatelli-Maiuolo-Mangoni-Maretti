package it.polimi.ingsw.shipboard;

import it.polimi.ingsw.view.cli.ANSI;

import java.util.Set;

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

    /** An unmodifiable set containing humans and aliens. */
    public static final Set<LoadableType> CREW_SET = Set.of(HUMAN, PURPLE_ALIEN, BROWN_ALIEN);
    public static final Set<LoadableType> CARGO_SET = Set.of(RED_GOODS, YELLOW_GOODS, GREEN_GOODS, BLUE_GOODS);
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

    public String getUnicodeColoredString() {
        return switch (this) {
            case BLUE_GOODS -> ANSI.BLUE + "■ ";
            case GREEN_GOODS -> ANSI.GREEN + "■ ";
            case YELLOW_GOODS -> ANSI.YELLOW + "■ ";
            case RED_GOODS -> ANSI.RED + "■ ";
            default -> "";
        };
    }
}

