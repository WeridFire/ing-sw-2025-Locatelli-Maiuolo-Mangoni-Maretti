package it.polimi.ingsw.util;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.view.cli.ANSI;

import java.util.List;

/**
 * Utility class providing game-specific standards and constants based on the {@link GameLevel}.
 * This includes for example all the info retrievable from the cardboard.
 */
public class GameLevelStandards {

    /**
     * Returns the ANSI color code (foreground or background) associated with the given game level.
     *
     * @param level the game level to retrieve color for.
     * @param background {@code true} to get the background color, {@code false} for foreground color.
     * @return the corresponding ANSI color code as a string.
     */
    public static String getColorANSI(GameLevel level, boolean background) {
        return switch (level) {
            case TESTFLIGHT, ONE -> background ? ANSI.BACKGROUND_CYAN : ANSI.CYAN;
            case TWO -> background ? ANSI.BACKGROUND_PURPLE : ANSI.PURPLE;
        };
    }

    /**
     * Returns the list of parking lot indices used on the flight board for the specified game level.
     * <p>
     * These indices represent the positions where players can park ships at the start of the game,
     * sorted in descending order (the first is the most advanced position).
     *
     * @param level the game level.
     * @return an unmodifiable list of integers representing starting parking lot positions.
     */
    public static List<Integer> getFlightBoardParkingLots(GameLevel level) {
        return switch (level) {
            case TESTFLIGHT, ONE -> List.of(4, 2, 1, 0);
            case TWO -> List.of(6, 3, 1, 0);
        };
    }

    /**
     * Returns the number of spaces used for the game timer in the specified game level.
     *
     * @param level the game level.
     * @return the number of timer spaces <===> the number of times the timer should run
     */
    public static int getTimerSlotsCount(GameLevel level) {
        return switch (level) {
            case TESTFLIGHT -> 0;
            case ONE -> 2;
            case TWO -> 3;
        };
    }

    /**
     * Returns the number of spaces required to complete a lap for the given game level.
     *
     * @param level the game level.
     * @return the lap size in number of spaces.
     */
    public static int getLapSize(GameLevel level) {
        return switch (level) {
            case TESTFLIGHT, ONE -> 18;
            case TWO -> 24;
        };
    }

    /**
     * Returns the list of cosmic credits rewarded for players based on their finish order in the given game level.
     * <p>
     * The list is ordered from first place to fourth place. Each element corresponds to the points awarded
     * to a player finishing in that position.
     *
     * @param level the game level.
     * @return an unmodifiable list of integers representing the finish order rewards.
     */
    public static List<Integer> getFinishOrderRewards(GameLevel level) {
        return switch (level) {
            case TESTFLIGHT, ONE -> List.of(4, 3, 2, 1);
            case TWO -> List.of(8, 6, 4, 2);
        };
    }

    /**
     * Returns the number of points awarded for building the best-looking ship in the specified game level.
     *
     * @param level the game level.
     * @return the number of points awarded for the best-looking ship.
     */
    public static int getAwardForBestLookingShip(GameLevel level) {
        return switch (level) {
            case TESTFLIGHT, ONE -> 2;
            case TWO -> 4;
        };
    }

}
