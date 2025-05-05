package it.polimi.ingsw.player.kpf;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.GameLevelStandards;

public class GetLappedKPF extends KeepPlayerFlyingPredicate {
    public GetLappedKPF(GameData gameData) {
        super(gameData);
    }

    @Override
    public boolean test(Player player) {
        int deltaPos = gameData.getPlayersInFlight().getFirst().getPosition() - player.getPosition();
        return deltaPos < GameLevelStandards.getLapSize(gameData.getLevel());
    }
}
