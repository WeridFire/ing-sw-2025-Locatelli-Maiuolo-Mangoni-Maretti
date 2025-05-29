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
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public OpenSpaceCard(String textureName, int level) {
		super("OPEN SPACE", textureName, level);
	}


	/**
	 * Iterates each player, requires how many thrust power they wanna use, and moves accordingly.
	 */
	//@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayersInFlight()){
			// here we just cast to int, but know for sure that the thrusters tiles won't return numbers with decimals.
			int steps = (int) PIRUtils.runPlayerPowerTilesActivationInteraction(p, game, PowerType.THRUST);
			if (steps < 1) {
				// exit flight if no thrust power is given
				p.requestEndFlight();
			} else {
				game.movePlayerForward(p, steps);
			}
		}
	}

	@Override
	public void startCardBehaviour(GameData game) {
		playTask(game,  game.getPlayersInFlight().getFirst());
	}

	public void playTask(GameData game, Player player) {
		if(player == null){
			//The previous player was the last one. We end the gamephase.
			game.getCurrentGamePhase().endPhase();
			return;
		}

		TaskActivateTiles task = new TaskActivateTiles(
				player.getUsername(),
				30,
				PowerType.THRUST,
				(p, coordinatesSet) -> {
				}
		);

		task.setOnFinish((p, coordinatesSet) -> {
			//After player has chosen which tiles to activate
			try {

				//activate tiles
				task.activateTiles(p, coordinatesSet);

				//remove batteries
				game.getTaskStorage().addTask(new TaskRemoveLoadables(
						p,
						30,
						Set.of(LoadableType.BATTERY),
						coordinatesSet.size(),
						(p1) -> {

						}
				));

				//calculate total power (previously done by PIRs)
				//moved it here since facing an enemy is the only instance of the game
				//where it's needed to calculate the thrust
				VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(PowerType.THRUST);

				//activated firepower
				int totalThrustPower = (int) powerInfo.getLocationsToActivate().entrySet().stream()
						.filter(entry -> coordinatesSet.contains(entry.getKey()))
						.mapToDouble(Map.Entry::getValue)
						.sum();

				//add base firepower
				totalThrustPower += (int) powerInfo.getBasePower();

				//add bonus if purple alien is present
				totalThrustPower += (int) powerInfo.getBonus(totalThrustPower);

				game.movePlayerForward(player, totalThrustPower);

			} catch (WrongPlayerTurnException | NotEnoughItemsException | TileNotAvailableException e) {
				throw new RuntimeException(e);
			}
		});

		game.getTaskStorage().addTask(task);
		playTask(game, game.getNextPlayerInFlight(player));
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
