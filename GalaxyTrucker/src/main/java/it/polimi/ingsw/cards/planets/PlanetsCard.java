package it.polimi.ingsw.cards.planets;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.exceptions.PlanetsCardException;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.task.customTasks.TaskAddLoadables;
import it.polimi.ingsw.task.customTasks.TaskMultipleChoice;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.*;

public class PlanetsCard extends Card {

	private Planet[] planets;
	private int lostDays;

	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param planets Instances of the planets, each one populated with the goods available on it.
	 * @param lostDays amount of days lost upon landing on a planet.
	 */
	public PlanetsCard(Planet[] planets, int lostDays, String textureName, int level) {
		super("PLANETS", textureName, level);
		this.planets = planets;
		this.lostDays = lostDays;
	}

	@Override
	public void startCardBehaviour(GameData game) {
		playTask(game, game.getPlayersInFlight().getFirst());
	}

	/**
	 * Gets, given an index, the corresponding planet.
	 * @param i the index of the planet.
	 * @throws PlanetsCardException the planet does not exists in the list of planets n the card.
	 * @return
	 */
	public Planet getPlanet(int i){
		if(i<0 || i >= planets.length){
			return null;
		}
		return planets[i];
	}


	public void runAddLoadablesTask(GameData game, Player player, Planet planet){
		game.getTaskStorage().addTask(new TaskAddLoadables(
				player.getUsername(), 30, planet.getAvailableGoods(),
				(currentPlayer) -> {
					//IF we have gone through ALL players (so the current player is the last in order)
					//Players leave the planet in reversed order.
					//Storing beforehands the position in the flight is FUNDAMENTAL, as if you have an operation
					//that makes the order change it might cause players to be processed twice.
					playTask(game, game.getNextPlayerInFlight(currentPlayer));
				}
		));
	}

	public void disembarkInReverse(GameData game){
		for(Player p : game.getPlayersInFlight().reversed()){
			Planet landedPlanet = Arrays.stream(planets)
					.filter(pl -> Objects.equals(pl.getCurrentPlayer(), p))
					.findFirst()
					.orElse(null);

			if(landedPlanet != null){
				landedPlanet.leavePlanet();
				game.movePlayerBackward(p, lostDays);
			}
		}
	}


	public void playTask(GameData game, Player player){
		if(player == null){
			disembarkInReverse(game);
			game.getCurrentGamePhase().endPhase();
			return;
		}

		List<Planet> availablePlanets = Arrays.stream(planets)
				.filter(planet -> planet.getCurrentPlayer() == null).toList();
		List<String> planetsOption = new ArrayList<String>();
		availablePlanets.forEach(planet -> planetsOption.add("Loot: " + planet.getAvailableGoods()));
		planetsOption.addFirst("Don't land");
		//Ask current player their desired planet
		game.getTaskStorage().addTask(new TaskMultipleChoice(player.getUsername(),
				30,
				"On what planet do you want to land? " + "(-" + lostDays +" travel days)",
				planetsOption.toArray(new String[0]),
				0,
				(currentPlayer, choice) -> {
					//------------------------------
					//AFTER CHOICE OF PLANET IS MADE
					//------------------------------
					if (choice > 0){
						//The player LANDS and retrieves loadables IMMEDIATELY
						Planet planet = availablePlanets.get(choice-1);
						if(planet != null){
							try {
								planet.landPlayer(currentPlayer);
							} catch (PlanetsCardException e) {
								throw new RuntimeException("Somehow a player choose to land " +
															"on a planet that was already occupied");
							}
							runAddLoadablesTask(game, player, planet);
						}
					}else{
						//IF the player DID NOT LAND we must still proceed with the loop!!
						//If the next player in flight is null, it is handled in the start of playTask.
						playTask(game, game.getNextPlayerInFlight(player));
						//CHIUSA
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

		List<String> cardInfoLines = new ArrayList<>();

		cardInfoLines.add(
				ANSI.BLACK + "Lost days: " + lostDays + ANSI.RESET
		);
		cardInfoLines.add(
				""
		);

		int p = 1;
		for (Planet planet : planets) {
			cardInfoLines.add(ANSI.BLACK + "Planet "+ p + ": "  + ANSI.RESET);
			p++;

			StringBuilder line = new StringBuilder();
			for (int i = 0; i < planet.getAvailableGoods().size(); i++) {
				if (i % 4 == 0 && i != 0) {
					cardInfoLines.add(line.toString());
					line = new StringBuilder();
				}
				line.append(planet.getAvailableGoods().get(i).getUnicodeColoredString());
			}

			cardInfoLines.add(
					line.toString()
			);


		}

		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
