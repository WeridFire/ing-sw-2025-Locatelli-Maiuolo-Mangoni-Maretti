package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.UUID;

public class SmugglersCard extends EnemyCard {


	private LoadableType[] prizeGoods;
	private int punishCargo;


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param punishCargo the amount of cargo to be removed from the player that is beaten by this card
	 * @param prizeGoods The goods earned by the player that beats this card.
	 */
	public SmugglersCard(int punishCargo, LoadableType[] prizeGoods, int firePower, int lostDays, String textureName, int level) {
		super(firePower, lostDays, textureName, level);
		this.punishCargo = punishCargo;
		this.prizeGoods = prizeGoods;
	}

	@Override
	public void givePrize(Player player, GameData game) {
		for(LoadableType c : prizeGoods){
			//TODO: ask player what they want to do for each cargo. Here we get coordinates, but we can assume we will
			//just get a Tile object, and we'll be able to add the cargo on it.
			Coordinates coordinates = new Coordinates(0, 0);
			//player.getShipBoard().getTile(coordinates).getContent().addCargo(c);
		}
		game.movePlayerBackward(player, getLostDays());

	}

	@Override
	public void applyPunishment(Player player, GameData game) {
		//TODO: Remove most valuable items from shipboard,
		//player.getShipBoard().removeMostValuableCargo(this.punishCargo);
	}

}
