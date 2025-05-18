package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuState extends CommonState {
    public static List<String> getActiveGamesUUID() {
        return getLastUpdate().getAvailableGames().stream()
                .map(GameData::getGameId)
                .map(UUID::toString).toList();
    }

    public static MainCabinTile.Color[] getAvailableColorsForGame(UUID gameId) {
        GameData gameData = getLastUpdate().getAvailableGames().stream()
                .filter(gd -> gd.getGameId().equals(gameId))
                .findFirst().orElse(null);
        if (gameData == null) return MainCabinTile.Color.values();
        List<MainCabinTile.Color> colors = new ArrayList<>(List.of(MainCabinTile.Color.values()));
        for (Player player : gameData.getPlayers()) {
            colors.remove(player.getColor());
        }
        return colors.toArray(new MainCabinTile.Color[0]);
    }
}
