package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class OpenSpaceCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public OpenSpaceCard(String textureName, int level) {
		super(textureName, level);
	}

	/**
	 * Iterates each player, requires how many thrust power they wanna use, and moves accordingly.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayers()){
			//TODO: request to controller the movement power.
			game.movePlayerForward(p, 4);
		}
	}
}
