package it.polimi.ingsw.cards.meteorstorm;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.util.UUID;

public class MeteorStormCard extends Card {

	/**
	 * The list of meteors that will hit the players.
	 */
	private final Projectile[] meteors;

	/**
	 * Instances a card.
	 * @param meteors The list of meteors that will hit the players.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public MeteorStormCard(Projectile[] meteors, String textureName, int level) {
		super(textureName, level);
		this.meteors = meteors;
	}

	/**
	 * Iterates through each meteor. For each meteor, hits all the victims in the same way.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Projectile m : meteors){
			for(Player victim : game.getPlayers()){
				//TODO: hit victim with the current projectile.
			}
		}
	}
}
