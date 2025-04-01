package it.polimi.ingsw.cards;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.Tile;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.shipboard.visitors.VisitorEpidemic;
import it.polimi.ingsw.util.Coordinates;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EpidemicCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public EpidemicCard(String textureName, int level) {
		super(textureName, level);
	}

	/**
	 * For each player, iterates on all the tiles present in the shipboard. Then for each one it applies the check of
	 * looking for adiacent tiles, to kill the passengers.
	 */
	@Override
	public void playEffect(GameData game) {
		for (Player p : game.getPlayers()) {

		}
	}

}
