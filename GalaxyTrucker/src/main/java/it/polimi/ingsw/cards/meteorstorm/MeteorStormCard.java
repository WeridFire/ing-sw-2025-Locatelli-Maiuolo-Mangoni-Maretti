package src.main.java.it.polimi.ingsw.cards.meteorstorm;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;

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
