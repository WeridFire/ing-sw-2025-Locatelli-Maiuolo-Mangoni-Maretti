package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

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
	 * @param gameId      The ID of the game this card is part of.
	 */
	public EpidemicCard(String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
	}

	/**
	 * For each player, iterates on all the tiles present in the shipboard. Then for each one it applies the check of
	 * looking for adiacent tiles, to kill the passengers.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		Set<Tile> valid = new HashSet<>(); // Initialize a modifiable set

		for (Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()) {
			// Filters to access only the tiles where there's a crew
			Set<Map.Entry<Coordinates, Tile>> crewTiles = p.getShipBoard().getTilesOnBoard()
					.stream()
					.filter(entry -> entry.getValue().getContent().countCrew() > 0)
					.collect(Collectors.toSet());

			for (Map.Entry<Coordinates, Tile> entry : crewTiles) {
				// TODO: Implement logic to check if nearby tiles contain a valid crew cabin
				valid.add(entry.getValue());
			}

			// Process each valid tile (Replace the incomplete lambda logic)
			valid.forEach(tile -> {
				try {
					tile.getContent().removeCrew(1);
				} catch (UnsupportedLoadableItemException e) {
					//TODO: ignore error as we know the tile is valid, find a clean way to do this.
				}
			});
		}
	}

}
