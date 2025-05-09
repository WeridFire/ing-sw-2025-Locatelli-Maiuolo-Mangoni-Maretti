package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.enums.GameLevel;

public class LobbyState extends CommonState {

    public static String getPlayerUsername() {
        return getPlayer() != null ? getPlayer().getUsername() : "Unknown";
    }

    public static boolean isGameLeader() {
        return getLastUpdate().isGameLeader();
    }

    public static GameLevel getGameLevel() {
        return getGameData().getLevel();
    }

    public static int getRequiredPlayers() {
        return getGameData().getRequiredPlayers();
    }

}
