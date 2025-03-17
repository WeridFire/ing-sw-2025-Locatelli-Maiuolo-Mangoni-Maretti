package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.UUID;

public class SlaversCard extends EnemyCard {


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param gameId      the ID of the game this card is part of.
	 */
	public SlaversCard(int firePower, int lostDays, String textureName, int level, UUID gameId) {
		super(firePower, lostDays, textureName, level, gameId);
	}

	@Override
	public void givePrize(Player player) {

	}

	@Override
	public void applyPunishment(Player player) {

	}
}
