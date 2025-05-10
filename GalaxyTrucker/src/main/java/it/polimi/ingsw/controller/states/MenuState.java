package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.game.GameData;

import java.util.List;
import java.util.UUID;

public class MenuState extends CommonState {
    public static List<String> getActiveGamesUUID() {
        return getLastUpdate().getAvailableGames().stream()
                .map(GameData::getGameId)
                .map(UUID::toString).toList();
    }
}
