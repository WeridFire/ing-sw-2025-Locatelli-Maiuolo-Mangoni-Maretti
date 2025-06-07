package it.polimi.ingsw.cards.warzone;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Consumer;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

public interface WarCriteria extends Comparator<Player>, Serializable {

	 void computeCriteria(GameData game, Consumer<Player> postCompute);

}