package it.polimi.ingsw.cards.meteorstorm;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.task.customTasks.TaskDelay;
import it.polimi.ingsw.util.Coordinates;
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


	@Override
	public void startCardBehaviour(GameData game) {
		Random random = new Random();
		int tmpDice1 = random.nextInt(6) + 1;
		int tmpDice2 = random.nextInt(6) + 1;
		//Restart the cycle with next meteor.

		String[] dicesString = dicesString(tmpDice1, tmpDice2);
		game.getTaskStorage().addTask(new TaskDelay(
				null,
				6,
				Arrays.toString(dicesString),
				getCLIRepresentation(),
				(p) -> {
					hitPlayerWithMeteor(game, game.getPlayersInFlight().getFirst(),
							0, tmpDice1, tmpDice2);
				}));
	}

	public void hitPlayerWithMeteor(GameData game, Player player, int meteorIdx, int dice1, int dice2){
		if(meteorIdx >= meteors.length){
			game.getCurrentGamePhase().endPhase();
			return;
		}

		if(player == null){
			Random random = new Random();
			int tmpDice1 = random.nextInt(6) + 1;
			int tmpDice2 = random.nextInt(6) + 1;
			//Restart the cycle with next meteor.

			String[] dicesString = dicesString(tmpDice1, tmpDice2);
			game.getTaskStorage().addTask(new TaskDelay(
					null,
					6,
					Arrays.toString(dicesString),
					getCLIRepresentation(),
					(p) -> {
						//first player, next meteor
						hitPlayerWithMeteor(game, game.getPlayersInFlight().getFirst(), meteorIdx+1, tmpDice1, tmpDice2);
				}));
			return;
		}

		//TODO: RUN INTERACTION FOR DEFENDING
		// THEN CALL hitPlayerWithMeteor(game, nextPlayer, meteorIdx, dice1, dice2) (same meteor, next index)

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
				"In the great halls beyond, the dice have fallen",
				"The ancient sigmas have spoken, and no one is safe from them",
				"BAZINGA",
				"Holy moly, someone just rolled the dies",
				"subemelaradio, esquececanzon",
				"9/11 was an inside job",
				"If he's invincible why can I see him?",
				"That was fantastic! wait... say that again",
				"Protocol 3, protect the pilot",
				"Oh, man, I don't wanna die to this song.",
				"AND DEY SAY Chivalry is dead",
				"Jarvis, stroke it a lil",
				"Jarvis, nuke that family of four",
				"Jarvis, increase the gender pay gap",
				"A really big fucking hole, coming right up",
				"Sybau Prof. Sanpietro, sincerely"
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
