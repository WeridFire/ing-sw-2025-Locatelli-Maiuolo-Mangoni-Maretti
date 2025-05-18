package it.polimi.ingsw.shipboard;

import it.polimi.ingsw.enums.Direction;

import java.util.List;

/**
 * Represents different types of sides that a tile can have.
 */
public enum SideType {
    /** A smooth side (non-connector). */
    SMOOTH,

    /** A side with a single connector. */
    SINGLE,

    /** A side with a double connector. */
    DOUBLE,

    /** A side with a universal connector (can connect to all the connectors). */
    UNIVERSAL,

    /** A side equipped with a cannon (no connectors). */
    CANNON,

    /** A side equipped with an engine (no connectors). */
    ENGINE;


    public static boolean areWeldable(SideType s1, SideType s2) {
        if (s1 == SINGLE) {
            return (s2 == SINGLE || s2 == UNIVERSAL);
        }
        else if (s1 == DOUBLE) {
            return (s2 == DOUBLE || s2 == UNIVERSAL);
        }
        else if (s1 == UNIVERSAL) {
            return s2.isConnector();
        }
        else {
            return false;
        }
    }

    public static boolean areCompatible(SideType s1, SideType s2) {
        if (areWeldable(s1, s2)) return true;  // weldable ==> compatible
        // else: not weldable but still have chance to be compatible
        return (s1 == SMOOTH && s2 == SMOOTH);
    }

    public static String getCLIRepresentation(SideType side, Direction pointing) {
        List<String> reps = switch (side) {
            case UNIVERSAL -> Direction.sortedArray("⁅", "╨┴", "⁆", "╥┬");
            case DOUBLE -> Direction.sortedArray("╞", "╨─", "╡", "╥─");
            case SINGLE -> Direction.sortedArray("├", "─┴", "┤", "─┬");
            case SMOOTH -> Direction.sortedArray("│", "──", "│", "──");
            case CANNON, ENGINE -> Direction.sortedArray(">", "/\\", "<", "\\/");
        };
        return reps.get(pointing.getValue());
    }

    public boolean isConnector() {
        return this == SINGLE || this == DOUBLE || this == UNIVERSAL;
    }
}
