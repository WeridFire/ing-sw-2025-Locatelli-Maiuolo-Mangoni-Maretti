package it.polimi.ingsw.model.player.kpf;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.util.GameLevelStandards;

public class GetLappedKPF extends KeepPlayerFlyingPredicate {
    private final int maxLapSize;
    public GetLappedKPF(GameData gameData) {
        super(gameData);
        maxLapSize = GameLevelStandards.getLapSize(gameData.getLevel());
    }

    @Override
    public boolean test(Player player) {
        int deltaPos = gameData.getPlayersInFlight().getFirst().getPosition() - player.getPosition();
        return deltaPos < maxLapSize;
    }
}
