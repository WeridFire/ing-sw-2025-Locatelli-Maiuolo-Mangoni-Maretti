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
		Random random = new Random();

		for(Projectile proj : meteors){
			int dice1 = random.nextInt(6) + 1;
			int dice2 = random.nextInt(6) + 1;

			String[] dicesString = dicesString(dice1, dice2);

			game.getPIRHandler().broadcastPIR(game.getPlayersInFlight(), (player, pirHandler) -> {
				PIRDelay pirDelay = new PIRDelay(player, 6,
                        Arrays.toString(dicesString),
						getCLIRepresentation());
				pirHandler.setAndRunTurn(pirDelay);
			});

			for(Player player : game.getPlayersInFlight()){
				boolean defended = PIRUtils.runPlayerProjectileDefendRequest(player, proj, game);
				if(!defended){
                    try {
                        player.getShipBoard().hit(proj.getDirection(), proj.getCoord());
                    } catch (NoTileFoundException | OutOfBuildingAreaException e) {
						throw new RuntimeException(e);  // should never happen -> runtime exception
                    }
                }
			}
		}
	}

	public String[] dicesString(int dice1, int dice2){
		String[] diceLines = {
				"The ancients have rolled the dice of fate",
				"The will of the cosmos has been cast",
				"The dice thunder with divine judgment",
				"The threads of destiny have been thrown upon the table",
				"Eternity has spoken in the language of dice",
				"From the heavens, the roll echoes through time",
				"The celestial hand has sealed your fate",
				"In the great halls beyond, the dice have fallen"
		};

		int index = ThreadLocalRandom.current().nextInt(diceLines.length);
		String randomLine = diceLines[index];

		ArrayList<String> diceFrameLines = new ArrayList<>();
		diceFrameLines.add(randomLine);
		diceFrameLines.add("The magic numbers are: " + dice1 + " and " + dice2);

		return diceFrameLines.toArray(new String[0]);
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
