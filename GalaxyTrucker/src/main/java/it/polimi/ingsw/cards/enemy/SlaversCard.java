package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PlayerRemoveLoadableRequest;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.tiles.CabinTile;
import it.polimi.ingsw.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlaversCard extends EnemyCard {

	/**
	 * How many crew members to remove.
	 */
	private final int punishCrewAmount;

	/**
	 * The amount of money to award to the player that beats this card.
	 */
	private final int prizeBounty;


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param punishCrewAmount The amount of crew members to remove upon being beat by this card.
	 * @param prizeBounty The amount of money to award to the player that beats this card.
	 */
	public SlaversCard(int punishCrewAmount, int prizeBounty, int firePower, int lostDays, String textureName, int level) {
		super(firePower, lostDays, textureName, level);
		this.punishCrewAmount = punishCrewAmount;
		this.prizeBounty = prizeBounty;
	}

	@Override
	public void givePrize(Player player, GameData game) {
		player.addCredits(prizeBounty);
		game.movePlayerBackward(player, getLostDays());
	}

	/**
	 *
	 * @param player player on which the method is currently acting upon
	 */
	@Override
	public void applyPunishment(Player player, GameData game) {
		game.setCurrentPlayerTurn(
				new PlayerRemoveLoadableRequest(player, 30, LoadableType.CREW_SET, punishCrewAmount)
		);
		try {
			game.getCurrentPlayerTurn().run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
