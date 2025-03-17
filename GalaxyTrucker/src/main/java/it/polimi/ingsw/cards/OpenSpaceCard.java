package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class OpenSpaceCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public OpenSpaceCard(String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
	}

	/**
	 * Iterates each player, requires how many thrust power they wanna use, and moves accordingly.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()){
			//TODO: request to controller the movement power.
			movePlayer(p, 4);
		}
	}
}
