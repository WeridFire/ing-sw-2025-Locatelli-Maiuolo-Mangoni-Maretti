package it.polimi.ingsw.model.cards.meteorstorm;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRUtils;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.model.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.model.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
		super("METEOR SWARM", textureName, level);
		this.meteors = meteors;
	}

	/**
	 * Iterates through each meteor. For each meteor, hits all the victims in the same way.
	 */
	@Override
	public void playEffect(GameData game) throws InterruptedException {
		for(Projectile proj : meteors){
			PIRUtils.runProjectile(game.getPlayersInFlight().getFirst(), proj, game, true, getTitle());
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

		List<String> cardInfoLines = new ArrayList<>();

		for(Projectile p: meteors){
			cardInfoLines.add(ANSI.BLACK + p.toUnicodeString());
		}

		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
