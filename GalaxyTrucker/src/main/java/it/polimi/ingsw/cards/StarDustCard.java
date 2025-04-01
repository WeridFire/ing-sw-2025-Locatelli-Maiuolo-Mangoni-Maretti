package it.polimi.ingsw.cards;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.util.UUID;

public class StarDustCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public StarDustCard(String textureName, int level) {
		super(textureName, level);
	}

	/**
	 * Iterates each player in reverse, counts how many connectors each one has, and moves accordingly.
	 *
	 */
	@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayers().reversed()){
			//TODO: count how many exposed connectors there are, and move accordingly
			game.movePlayerBackward(p, 4);
		}
	}
}
