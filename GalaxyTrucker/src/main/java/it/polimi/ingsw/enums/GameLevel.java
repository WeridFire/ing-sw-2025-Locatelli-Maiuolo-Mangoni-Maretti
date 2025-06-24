package it.polimi.ingsw.enums;

import java.io.Serializable;
import java.util.List;

public enum GameLevel implements Serializable {
    TESTFLIGHT, ONE, TWO;

    public static final List<GameLevel> LEVELS_TO_PLAY = List.of(TESTFLIGHT, ONE, TWO);
    /**
     * Validate a level to be in {@link #LEVELS_TO_PLAY}
     * @param level the level to check
     * @return {@code true} if and only if {@code level} is in {@link #LEVELS_TO_PLAY}.
     */
    public static boolean canBePlayed(GameLevel level) {
        return LEVELS_TO_PLAY.contains(level);
    }

    public static GameLevel fromInteger(int i) {
        return switch (i) {
            case 1 -> ONE;
            case 2 -> TWO;
            default -> TESTFLIGHT;
        };
    }

    public static GameLevel max(GameLevel l1, GameLevel l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;
        return (l1.ordinal() > l2.ordinal()) ? l1 : l2;
    }
}
