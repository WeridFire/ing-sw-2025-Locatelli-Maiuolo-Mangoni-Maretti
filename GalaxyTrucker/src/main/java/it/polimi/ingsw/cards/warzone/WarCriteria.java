package it.polimi.ingsw.cards.warzone;

import java.io.Serializable;
import java.util.Comparator;
import it.polimi.ingsw.player.Player;

public interface WarCriteria extends Comparator<Player>, Serializable {
    String getName();
}