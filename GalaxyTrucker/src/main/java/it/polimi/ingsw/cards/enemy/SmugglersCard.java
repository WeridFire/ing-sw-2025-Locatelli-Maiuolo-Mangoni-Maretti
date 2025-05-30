package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.planets.Planet;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.task.customTasks.TaskAddLoadables;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;
import it.polimi.ingsw.task.customTasks.TaskYesNoChoice;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class SmugglersCard extends EnemyCard {


	private final LoadableType[] prizeGoods;
	private final int punishCargo;

	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param punishCargo the amount of cargo to be removed from the player that is beaten by this card
	 * @param prizeGoods The goods earned by the player that beats this card.
	 */
	public SmugglersCard(int punishCargo, LoadableType[] prizeGoods, int firePower, int lostDays, String textureName, int level) {
		super(firePower, lostDays, "SMUGGLERS", textureName, level);
		this.punishCargo = punishCargo;
		this.prizeGoods = prizeGoods;
	}

	@Override
	public void givePrizeTask(Player player, GameData game){
		game.getTaskStorage().addTask(new TaskYesNoChoice(
				player.getUsername(),
				30,
				"You will receive the following goods: " + prizeGoods + " but you will lose " + getLostDays() + " travel days.",
				false,
				(p, choice) -> {
					if(TaskYesNoChoice.isChoiceYes(choice)){
						addLoadablesTask(p, game);
					}else{
						game.getCurrentGamePhase().endPhase();
					}
				}
		));
	}

	public void addLoadablesTask(Player player, GameData game){
		game.getTaskStorage().addTask(new TaskAddLoadables(
				player.getUsername(),
				30,
				List.of(prizeGoods),
				(p) -> {
					game.movePlayerBackward(p, getLostDays());
					game.getCurrentGamePhase().endPhase();
				}
		));
	}


	@Override
	public void applyPunishmentTask(Player player, GameData game){
		player.getShipBoard().loseBestGoods(this.punishCargo);
		/**
		 * Proceed to next player
		 */
		super.playTask(game, game.getNextPlayerInFlight(player));
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
		 * |   PIRATES    |
		 * | lost days: x |
		 * | firepower: x |
		 * | bounty: x    |
		 * |              |
		 * | hits:        |
		 * | ............ |
		 * | ............ |
		 * +--------------+
		 * */

		List<String> cardInfoLines = new ArrayList<>();

		cardInfoLines.add(
				ANSI.BLACK + "Punish cargo: " + punishCargo + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Fire power: " + getFirePower() + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Lost days: " + getLostDays() + ANSI.RESET
		);

		cardInfoLines.add(
				""
		);

		cardInfoLines.add(
				ANSI.BLACK + "Prizes: " + ANSI.RESET
		);
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < prizeGoods.length; i++) {
			if (i % 4 == 0 && i != 0) {
				cardInfoLines.add(line.toString());
				line = new StringBuilder();
			}
			line.append(prizeGoods[i].getUnicodeColoredString());
		}

		cardInfoLines.add(
				line.toString()
		);


		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
