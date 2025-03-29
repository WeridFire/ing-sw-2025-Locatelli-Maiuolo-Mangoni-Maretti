package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.PlayerAddLoadableRequest;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.Arrays;
import java.util.UUID;

public class SmugglersCard extends EnemyCard {


	private final LoadableType[] prizeGoods;
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
		game.setCurrentPlayerTurn(new PlayerAddLoadableRequest(player, 30, Arrays.stream(prizeGoods).toList()));
		game.movePlayerBackward(player, getLostDays());
	}

	@Override
	public void applyPunishment(Player player, GameData game) {
		//TODO: Remove most valuable items from shipboard,
		//player.getShipBoard().removeMostValuableCargo(this.punishCargo);
	}

}
