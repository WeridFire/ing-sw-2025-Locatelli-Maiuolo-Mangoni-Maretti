package it.polimi.ingsw.model.cards.warzone;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;

public interface WarCriteria extends Comparator<Player>, Serializable {
    String getName();

    Player computeCriteria(GameData game);
}