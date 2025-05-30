package it.polimi.ingsw.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.task.customTasks.TaskActivateTiles;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.*;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class OpenSpaceCard extends Card{


	private Player[] playersFlightOrder;

	private Player getPlayer(int i){
		if (i >= playersFlightOrder.length){
			return null;
		}
		return playersFlightOrder[i];
	}

	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public OpenSpaceCard(String textureName, int level) {
		super("OPEN SPACE", textureName, level);

	}


	@Override
	public void startCardBehaviour(GameData game) {
		this.playersFlightOrder = game.getPlayersInFlight().toArray(new Player[0]);
		playTask(game,  0);
	}

	public void movePlayerAndPlayNextTask(GameData game, int playerIdx, float activatedPower){
		Player player = getPlayer(playerIdx);
		if(player != null){
			//this condition should always be satisfied


			VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player
					.getShipBoard()
					.getVisitorCalculatePowers()
					.getInfoPower(PowerType.THRUST);

			float totalFirePower = powerInfo.getBasePower() + activatedPower;
			totalFirePower += powerInfo.getBonus(totalFirePower);
			int steps = Math.round(totalFirePower);
			game.movePlayerForward(player, steps);
			playTask(game, playerIdx+1);
		}


	}

	public void playTask(GameData game, int playerIdx) {
		Player player = getPlayer(playerIdx);
		/**
		 * The flight order might change during the actual performance of the turn. Therefore we must first save in
		 * an unmodifiable array the order of the flight at the start of the card, and then process it.
		 */

		if(player == null){
			//The previous player was the last one. We end the gamephase.
			game.getCurrentGamePhase().endPhase();
			return;
		}

		game.getTaskStorage().addTask(new TaskActivateTiles(
				player.getUsername(),
				30,
				PowerType.THRUST,
				(p, coordinatesSet) -> {
					int batteriesAmount = coordinatesSet.size();
					if(batteriesAmount <= 0f){
						/**
						 * No extra power, proceed.
						 */
						movePlayerAndPlayNextTask(game, playerIdx, 0f);
						return;
					}else{
						game.getTaskStorage().addTask(new TaskRemoveLoadables(
								p,
								30,
								Set.of(LoadableType.BATTERY),
								batteriesAmount,
								(p1) -> {
									VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player
											.getShipBoard()
											.getVisitorCalculatePowers()
											.getInfoPower(PowerType.THRUST);
									float activatedPower = (float) powerInfo.getLocationsToActivate().entrySet().stream()
											.filter(entry -> coordinatesSet.contains(entry.getKey()))
											.mapToDouble(Map.Entry::getValue)
											.sum();
									movePlayerAndPlayNextTask(game, playerIdx, activatedPower);
								}));
					}
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

		CLIFrame infoFrame = new CLIFrame(ANSI.BLACK + "Let's get")
				.merge(new CLIFrame(ANSI.BLACK + "some fly days!"), Direction.SOUTH);

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
