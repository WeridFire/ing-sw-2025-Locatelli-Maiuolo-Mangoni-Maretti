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
}
