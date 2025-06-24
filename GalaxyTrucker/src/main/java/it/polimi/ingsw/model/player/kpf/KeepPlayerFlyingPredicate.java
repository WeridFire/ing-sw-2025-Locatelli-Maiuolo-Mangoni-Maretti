package it.polimi.ingsw.model.player.kpf;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;

import java.io.Serializable;
import java.util.function.Predicate;

public abstract class KeepPlayerFlyingPredicate implements Predicate<Player>, Serializable {
    protected GameData gameData;

    public KeepPlayerFlyingPredicate(GameData gameData) {
        this.gameData = gameData;
    }
}
