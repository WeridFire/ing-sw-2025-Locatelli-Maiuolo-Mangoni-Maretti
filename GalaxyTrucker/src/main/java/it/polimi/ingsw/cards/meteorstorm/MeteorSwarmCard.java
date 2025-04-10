package it.polimi.ingsw.cards.meteorstorm;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class MeteorSwarmCard extends Card {

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
	public MeteorSwarmCard(Projectile[] meteors, String textureName, int level) {
		super(textureName, level);
		this.meteors = meteors;
	}

	/**
	 * Iterates through each meteor. For each meteor, hits all the victims in the same way.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Projectile proj : meteors){
			//TODO: make first player roll coordinates
			for(Player player : game.getPlayers()){
				boolean defended = PIRUtils.runPlayerProjectileDefendRequest(player, proj, game);
				if(!defended){
					//TODO: HIT PLAYER
				}
			}
		}
	}

	/**
	 * Generates a CLI representation of the implementing object.
	 *
	 * @return A {@link CLIFrame} containing the CLI representation.
	 */
	@Override
	public CLIFrame getCLIRepresentation() {
		/**
		 * sembrano in obliquo i bordi ma è
		 * perche è un commento
		 *
		 * +--------------+
		 * |   Slavers    |
		 * | lost days: x |
		 * | firepower: x |
		 * | bounty: x    |
		 * |              |
		 * | crew cost:        |
		 * | ............ |
		 * | ............ |
		 * +--------------+
		 * */

		CLIFrame cardBorder = getScreenFrame(11, 20, ANSI.BACKGROUND_CYAN);

		// frame title
		CLIFrame title = new CLIFrame(new String[]{
				ANSI.WHITE + "METEOR SWARM" + ANSI.RESET
		});
		cardBorder = cardBorder.merge(title, AnchorPoint.TOP, AnchorPoint.CENTER, 0, 0);

		List<String> cardInfoLines = new ArrayList<>();

		for(Projectile p: meteors){
			cardInfoLines.add(ANSI.BLACK + p.toUnicodeString());
		}

		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		cardBorder = cardBorder.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

		return cardBorder;
	}
}
