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
    ENGINE
}
