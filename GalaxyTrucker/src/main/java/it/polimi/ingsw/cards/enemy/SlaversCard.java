package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.UUID;

public class SlaversCard extends EnemyCard {

	/**
	 * How many crew members to remove.
	 */
	private int punishCrewAmount;

	/**
	 * The amount of money to award to the player that beats this card.
	 */
	private int prizeBounty;


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param gameId      the ID of the game this card is part of.
	 * @param punishCrewAmount The amount of crew members to remove upon being beat by this card.
	 * @param prizeBounty The amount of money to award to the player that beats this card.
	 */
	public SlaversCard(int punishCrewAmount, int prizeBounty, int firePower, int lostDays, String textureName, int level, UUID gameId) {
		super(firePower, lostDays, textureName, level, gameId);
		this.punishCrewAmount = punishCrewAmount;
		this.prizeBounty = prizeBounty;
	}

	@Override
	public void givePrize(Player player) {
		player.addCredits(prizeBounty);
		movePlayer(player, getLostDays());
	}

	@Override
	public void applyPunishment(Player player) {
		for(int i=0; i<punishCrewAmount; i++){
			//TODO: ask player where he wants to remove the crew member from.
		}
	}
}
