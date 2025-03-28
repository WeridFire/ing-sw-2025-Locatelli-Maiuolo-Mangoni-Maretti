package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.CabinTile;
import src.main.java.it.polimi.ingsw.shipboard.tiles.ContainerTile;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	/**
	 *
	 * @param player player on which the method is currently acting upon
	 */
	@Override
	public void applyPunishment(Player player) {
		Map<Coordinates, CabinTile> itemsPosition = player.getShipBoard().getVisitorCalculateCargoInfo()
				.getCrewInfo().getLocationsWithLoadedItems(1);
		//TODO: send the map of positions and content to the player, wait for them to return
		// a similar map that will contain the items that the user desires to remove from each coordinate.
		Map<Coordinates, List<LoadableType>> itemsToRemove = new HashMap<>();
		itemsToRemove.forEach((coords, items) -> {
			ContainerTile tile = null;
			try {
				tile = (ContainerTile) player.getShipBoard().getTile(coords);
			} catch (OutOfBuildingAreaException | NoTileFoundException e) {
				throw new RuntimeException(e);
			}
			for(LoadableType l : items){
				try {
					tile.loadItems(l, 1);
				} catch (TooMuchLoadException | UnsupportedLoadableItemException e) {
					//TODO: Contact player to notify failure, and retry
				}
			}
		});
	}
}
