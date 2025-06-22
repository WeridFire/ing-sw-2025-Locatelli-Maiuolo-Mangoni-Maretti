package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.io.Serializable;

public interface WarPunishment extends Serializable {
    String getDetails();
    void apply(Player player, GameData gameData);
}
