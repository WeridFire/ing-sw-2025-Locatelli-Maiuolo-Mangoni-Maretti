package src.main.java.it.polimi.ingsw.cards.warzone;

import java.util.Comparator;
import src.main.java.it.polimi.ingsw.player.Player;

public class WarCriteria {

	/**
	 * Comparator used to classify players.
	 */
	private Comparator<Player> comparator;

	/**
	 * Instances the object and accepts the comparator.
	 * @param comparator The comparatoor used to classify players.
	 */
	public WarCriteria(Comparator<Player> comparator) {
		this.comparator = comparator;
	}


	/**
	 *
	 * @return Comparator used to classify players.
	 */
	public Comparator<Player> getComparator() {
		return comparator;
	}
}