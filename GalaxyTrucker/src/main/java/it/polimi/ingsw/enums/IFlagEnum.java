package src.main.java.it.polimi.ingsw.enums;

/**
 * An interface for enums that represent bitwise flag values.
 * This interface allows for efficient manipulation and comparison of flag-based enums.
 *
 * @param <T> the type of the enum implementing this interface
 */
public interface IFlagEnum<T extends Enum<T> & IFlagEnum<T>> {

    /**
     * Retrieves the integer value representing the bitwise flags of this enum.
     *
     * @return the integer flag value
     */
    int getFlagsValue();

    /**
     * Retrieves an enum constant corresponding to the specified flag value.
     *
     * @param value the integer flag value
     * @return {@code null} if the value does not specify an implemented flag nor an implemented mix of flags,
     * otherwise the enum constant corresponding to the given value
     */
    T fromValue(int value);

    /**
     * Checks if this flag is compatible with the given flags.
     * Two flags are considered compatible if they share at least one common bit.
     *
     * @param flags the flags to compare with
     * @return {@code true} if there is at least one shared flag, {@code false} otherwise
     */
    default boolean isCompatibleWith(IFlagEnum<T> flags) {
        return (getFlagsValue() & flags.getFlagsValue()) != 0;
    }

    /**
     * Checks if this flag is entirely contained within another flag set.
     *
     * @param flags the flags to compare with
     * @return {@code true} if all bits of this flag are present in the given flags, {@code false} otherwise
     */
    default boolean isContainedIn(IFlagEnum<T> flags) {
        return (getFlagsValue() & flags.getFlagsValue()) == getFlagsValue();
    }

    /**
     * Checks if this flag set contains another flag set.
     *
     * @param flags the flags to check for containment
     * @return {@code true} if this flag set contains all bits of the given flags, {@code false} otherwise
     */
    default boolean contains(IFlagEnum<T> flags) {
        return flags.isContainedIn(this);
    }

    /**
     * Checks if this flag set is exactly equal to another flag set.
     *
     * @param flags the flags to compare with
     * @return {@code true} if both flag sets have the same bitwise value, {@code false} otherwise
     */
    default boolean isEqualTo(IFlagEnum<T> flags) {
        return getFlagsValue() == flags.getFlagsValue();
    }

    /**
     * Returns a new flag value that includes the given flag.
     * This performs a bitwise OR operation to add the flag.
     *
     * @param flag the flag to add
     * @return the new integer flag value including the added flag
     */
    default int addFlag(T flag) {
        return getFlagsValue() | flag.getFlagsValue();
    }

    /**
     * Returns a new flag value that removes the given flag.
     * This performs a bitwise AND NOT operation to remove the flag.
     *
     * @param flag the flag to remove
     * @return the new integer flag value excluding the removed flag
     */
    default int removeFlag(T flag) {
        return getFlagsValue() & ~flag.getFlagsValue();
    }
}

