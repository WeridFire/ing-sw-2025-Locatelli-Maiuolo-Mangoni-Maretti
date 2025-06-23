package it.polimi.ingsw.model.cards.warzone;

import java.io.Serializable;
import java.util.Comparator;
import it.polimi.ingsw.model.player.Player;

public interface WarCriteria extends Comparator<Player>, Serializable {
    String getName();
}