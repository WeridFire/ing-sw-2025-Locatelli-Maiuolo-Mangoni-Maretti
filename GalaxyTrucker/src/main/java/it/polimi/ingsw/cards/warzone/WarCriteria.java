package src.main.java.it.polimi.ingsw.cards.warzone;

import java.util.Comparator;
import src.main.java.it.polimi.ingsw.player.Player;

public class WarCriteria {

	private Comparator<Player> comparator;

	public WarCriteria(Comparator<Player> comparator) {
		this.comparator = comparator;
	}

	public Comparator<Player> getComparator() {
		return comparator;
	}
}