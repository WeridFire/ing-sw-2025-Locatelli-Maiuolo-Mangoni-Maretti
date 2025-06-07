package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.io.Serializable;
import java.util.function.Consumer;

public interface WarPunishment extends Serializable {
    void apply(Player player, GameData gameData, Consumer<Player> onFinish);
}
