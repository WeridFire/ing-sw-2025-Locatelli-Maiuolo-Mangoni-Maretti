package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class StarDustCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public StarDustCard(String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
	}

	/**
	 * Iterates each player in reverse, counts how many connectors each one has, and moves accordingly.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers().reversed()){
			//TODO: count how many exposed connectors there are, and move accordingly
			movePlayer(p, 4);
		}
	}
}
