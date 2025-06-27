package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;

import java.io.Serializable;

public interface WarPunishment extends Serializable {
    String getDetails();
    void apply(Player player, GameData gameData) throws InterruptedException;
}
