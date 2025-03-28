package src.main.java.it.polimi.ingsw.cards.meteorstorm;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class MeteorStormCard extends Card {

	/**
	 * The list of meteors that will hit the players.
	 */
	private Projectile[] meteors;

	/**
	 * Instances a card.
	 * @param meteors The list of meteors that will hit the players.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public MeteorStormCard(Projectile[] meteors, String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
		this.meteors = meteors;
	}

	/**
	 * Iterates through each meteor. For each meteor, hits all the victims in the same way.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		for(Projectile m : meteors){
			for(Player victim : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()){
				//TODO: hit victim with the current projectile.
			}
		}
	}
}
