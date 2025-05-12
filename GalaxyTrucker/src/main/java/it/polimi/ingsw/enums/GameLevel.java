package it.polimi.ingsw.enums;

import java.io.Serializable;

public enum GameLevel implements Serializable {
    TESTFLIGHT, ONE, TWO;

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
