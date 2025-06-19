package it.polimi.ingsw.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;
import it.polimi.ingsw.task.customTasks.TaskYesNoChoice;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class AbandonedShipCard extends Card{

	/**
	 * The crew that is required and will be taken away when taking the ship.
	 */
	private final int requiredCrew;
	/**
	 * The days removed when taking the ship.
	 */
	private final int lostDays;
	/**
	 * The credits earned upon fixing and selling the ship.
	 */
	private final int sellPrice;

	/**
	 * Instances a card.
	 * @param requiredCrew the crew that is required and will be taken away when selling the ship.
	 * @param lostDays the days removed when taking the ship.
	 * @param sellPrice the credits earned upon fixing and selling the ship.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public AbandonedShipCard(int requiredCrew, int lostDays, int sellPrice, String textureName, int level) {
		super("ABANDONED SHIP", textureName, level);
		this.requiredCrew = requiredCrew;
		this.lostDays = lostDays;
		this.sellPrice = sellPrice;
	}


	@Override
	public void startCardBehaviour(GameData game) {
		playTask(game,  game.getPlayersInFlight().getFirst());
	}

	public void runRemoveCrewTask(GameData game, Player player) {
		game.getTaskStorage().addTask(new TaskRemoveLoadables(
				player, 30, LoadableType.CREW_SET, requiredCrew,
				(p) -> {
					/**
					 * ON INTERACTION END
					 */
					game.movePlayerBackward(player, lostDays);
					game.getCurrentGamePhase().endPhase();
				}
		));
	};



	/**
	 * Given the current player, checks if they can take the ship. If they can (and want), removes crew from their
	 * ship and awards the credits. Then proceeds to the next player, if the ship hasnt been taken.
	 */
	public void playTask(GameData game, Player player) {
		if(player == null){
			//The previous player was the last one. We end the gamephase.
			game.getCurrentGamePhase().endPhase();
			return;
		}


		if(player.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getCrewInfo()
				.countAll(LoadableType.CREW_SET) >= requiredCrew){
			game.getTaskStorage().addTask(
					new TaskYesNoChoice(player.getUsername(), 30, "Do you want to take the ship? " +
							"You will lose " + requiredCrew + " crew " +
							"and " + lostDays + " travel days, but you will receive " +
							sellPrice + " credits.", false,
							(p, choice) -> {
								/**
								 * EXECUTED ON END OF CHOICE
								 */
								if(TaskYesNoChoice.isChoiceYes(choice)){
									runRemoveCrewTask(game, p);
								}else{
									playTask(game, game.getNextPlayerInFlight(player));
								}
							}
						)
			);
		}else{
			playTask(game, game.getNextPlayerInFlight(player));
		}
	}

	/**
	 * Generates a CLI representation of the implementing object.
	 *
	 * @return A {@link CLIFrame} containing the CLI representation.
	 */
	@Override
	public CLIFrame getCLIRepresentation(){
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
				ANSI.BLACK + "Required Crew: " + requiredCrew + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Lost days: " + lostDays + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Sell Price:  " + sellPrice + ANSI.RESET
		);

		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
