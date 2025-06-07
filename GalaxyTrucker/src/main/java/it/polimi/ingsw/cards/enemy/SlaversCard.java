package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRRemoveLoadables;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;
import it.polimi.ingsw.task.customTasks.TaskYesNoChoice;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class SlaversCard extends EnemyCard {

	/**
	 * How many crew members to remove.
	 */
	private final int punishCrewAmount;

	/**
	 * The amount of money to award to the player that beats this card.
	 */
	private final int prizeBounty;


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param punishCrewAmount The amount of crew members to remove upon being beat by this card.
	 * @param prizeBounty The amount of money to award to the player that beats this card.
	 */
	public SlaversCard(int punishCrewAmount, int prizeBounty, int firePower, int lostDays, String textureName, int level) {
		super(firePower, lostDays, "SLAVERS", textureName, level);
		this.punishCrewAmount = punishCrewAmount;
		this.prizeBounty = prizeBounty;
	}


	@Override
	public void givePrizeTask(Player player, GameData game){

		game.getTaskStorage().addTask(new TaskYesNoChoice(
				player.getUsername(),
				30,
				"You will receive " + prizeBounty +" credits, but you will lose "
						+ getLostDays() + " days.",
				false,
				(p, choice) -> {
					if(TaskYesNoChoice.isChoiceYes(choice)){
						p.addCredits(prizeBounty);
						game.movePlayerBackward(player, getLostDays());
					}
					game.getCurrentGamePhase().endPhase();
				}
		));
	}

	/**
	 * New method for applying punishment using tasks
	 * @param player player on which the method is currently acting upon
	 * @param game the gamedata
	 */
	@Override
	public void applyPunishmentTask(Player player, GameData game) {
		game.getTaskStorage().addTask(new TaskRemoveLoadables(
				player,
				30,
				LoadableType.CREW_SET,
				punishCrewAmount,
				(p) -> {
					playTask(game, game.getNextPlayerInFlight(p));
				}
		));
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
		cardInfoLines.add(
				ANSI.BLACK + "Lost days: " + getLostDays() + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Firepower: " + getFirePower() + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Bounty: " + prizeBounty + ANSI.RESET
		);
		cardInfoLines.add(
				" "
		);
		cardInfoLines.add(
				ANSI.BLACK + "Crew cost: " + punishCrewAmount + ANSI.RESET
		);

		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
