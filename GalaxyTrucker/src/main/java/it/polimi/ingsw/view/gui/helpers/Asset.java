package it.polimi.ingsw.view.gui.helpers;

import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.shipboard.LoadableType;

/**
 * Enum representing high-level GUI assets and providing utility methods
 * to resolve their filenames based on game context and credit values.
 */
public enum Asset {

    /** Asset for the player's ship image. */
    SHIP,
    /** Asset for the game board image. */
    BOARD;

    /**
     * Returns the filename of a credit-related cardboard image based on the credit value.
     * <p>
     * Maps specific credit amounts to corresponding asset filenames.
     *
     * @param value the credit value (e.g., 1, 2, 5, 10, 50)
     * @return the filename of the cardboard image for the given credit
     */
    public static String getCredit(int value) {
        return switch (value) {
            case 2 -> "cardboard-6.png";
            case 5 -> "cardboard-7.png";
            case 10 -> "cardboard-8.png";
            case 50 -> "cardboard-9.png";
            default -> "cardboard-10.png";
        };
    }

    /**
     * Resolves the filename of this asset based on the current game level.
     * <p>
     * Uses {@link LobbyState#getGameLevel()} to determine the active {@link GameLevel},
     * and selects the appropriate image variant for test flight, level one, or higher levels.
     *
     * @return the filename of this asset for the current game level
     */
    @Override
    public String toString() {
        GameLevel level = LobbyState.getGameLevel();

        return switch (level) {
            case TESTFLIGHT, ONE -> switch (this) {
                case SHIP -> "cardboard-1.jpg";
                case BOARD -> "cardboard-3.png";
            };
            case TWO -> switch (this) {
                case SHIP -> "cardboard-1b.jpg";
                case BOARD -> "cardboard-5.png";
            };
        };
    }

    public static String fromLoadableType(LoadableType type) {
        switch (type) {
            case BATTERY -> {
                return "battery.png";
            }
            case BLUE_GOODS -> {
                return "loadable_blue.png";
            }
            case GREEN_GOODS -> {
                return"loadable_green.png";
            }
            case YELLOW_GOODS -> {
                return "loadable_yellow.png";
            }
            case RED_GOODS -> {
                return "loadable_red.png";
            }
            case HUMAN -> {
                return "astronaut.png";
            }
            case PURPLE_ALIEN -> {
                return "purple_alien.png";
            }
            default -> {
                return "brown_alien.png";
            }
        }
    }
}