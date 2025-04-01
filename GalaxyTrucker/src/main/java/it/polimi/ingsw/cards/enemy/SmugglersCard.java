package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRAddLoadables;
import it.polimi.ingsw.shipboard.LoadableType;

import java.util.Arrays;

public class SmugglersCard extends EnemyCard {


	private final LoadableType[] prizeGoods;
	private final int punishCargo;

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
		game.setCurrentPlayerTurn(new PIRAddLoadables(player, 30, Arrays.stream(prizeGoods).toList()));
		game.movePlayerBackward(player, getLostDays());
	}

	@Override
	public void applyPunishment(Player player, GameData game) {
		//TODO: Remove most valuable items from shipboard,
		//player.getShipBoard().removeMostValuableCargo(this.punishCargo);
	}

}
