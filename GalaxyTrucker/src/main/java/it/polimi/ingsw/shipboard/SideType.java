package src.main.java.it.polimi.ingsw.shipboard;

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
            return (s2 == SINGLE || s2 == DOUBLE || s2 == UNIVERSAL);
        }
        else {
            return false;
        }
    }
}
