package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.UUID;

public class SmugglersCard extends EnemyCard {


	private CargoType[] prizeGoods;
	private int punishCargo;


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param gameId      the ID of the game this card is part of.
	 * @param punishCargo the amount of cargo to be removed from the player that is beaten by this card
	 * @param prizeGoods The goods earned by the player that beats this card.
	 */
	public SmugglersCard(int punishCargo, CargoType[] prizeGoods, int firePower, int lostDays, String textureName, int level, UUID gameId) {
		super(firePower, lostDays, textureName, level, gameId);
		this.punishCargo = punishCargo;
		this.prizeGoods = prizeGoods;
	}

	@Override
	public void givePrize(Player player) {
		for(CargoType c : prizeGoods){
			//TODO: ask player what they want to do for each cargo. Here we get coordinates, but we can assume we will
			//just get a Tile object, and we'll be able to add the cargo on it.
			Coordinates coordinates = new Coordinates(0, 0);
			//player.getShipBoard().getTile(coordinates).getContent().addCargo(c);
			//TODO: wait for Manuel to finish his part.
		}
		movePlayer(player, getLostDays());

	}

	@Override
	public void applyPunishment(Player player) {
		//TODO: Remove most valuable items from shipboard, wait for manuel to finish his part.
		//player.getShipBoard().removeMostValuableCargo(this.punishCargo);
	}

}
