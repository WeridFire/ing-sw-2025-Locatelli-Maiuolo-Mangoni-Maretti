package it.polimi.ingsw.model.cards.planets;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.exceptions.PlanetsCardException;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.*;
import it.polimi.ingsw.model.shipboard.LoadableType;
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

	/**
	 * Controls the addLoadable interaction with the player.
	 * */
	public void addLoadablesInteraction(Player p, PIRHandler pirHandler) throws InterruptedException {
		Planet planet = getPlanetByPlayer(p);
		if(planet == null){
			return;
		}

		int loadablesAmount = p.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().getAllLoadedItems().size();
		if(loadablesAmount > 0){
			boolean result = pirHandler.setAndRunTurn(
					new PIRYesNoChoice(p, 30, "Do you want to rearrange the goods already on your ship?", false)
			);
			if(result){
				List<LoadableType> loadablesToAdd = p
						.getShipBoard()
						.getVisitorCalculateCargoInfo()
						.getGoodsInfo()
						.getAllLoadedItems();
				p.getShipBoard().loseBestGoods(loadablesToAdd.size());
				pirHandler.setAndRunTurn(
						new PIRAddLoadables(p, 30, loadablesToAdd)
				);
			}
		}

		PIRAddLoadables pirAddLoadables = new PIRAddLoadables(p, 30, planet.getAvailableGoods());
		pirHandler.setAndRunTurn(pirAddLoadables);
	}

	private Planet getPlanetByPlayer(Player p){
		for(Planet planet : planets){
			if(planet.getCurrentPlayer().equals(p)){
				return planet;
			}
		}
		return null;
	}

	/**
	 * Iteratively asks each player if they want to land. Then in reverse order allows players to pickup their items
	 * and move back in days.
	 */
	@Override
	public void playEffect(GameData game) throws InterruptedException {
		for (Player p : game.getPlayersInFlight()){
			List<Planet> availablePlanets = Arrays.stream(planets)
					.filter(planet -> planet.getCurrentPlayer() == null).toList();
			List<String> planetsOption = new ArrayList<String>();
			availablePlanets.forEach(planet -> planetsOption.add("Loot: " + planet.getAvailableGoods()));
			planetsOption.addFirst("Don't land");
			int choice = game.getPIRHandler().setAndRunTurn(
					new PIRMultipleChoice(p, 30, "On what planet do you want to land? " +
							"(-" + lostDays +" travel days)",
							planetsOption.toArray(new String[0]), 0)
			);
			if(choice > 0){
				Planet planet = availablePlanets.get(choice-1);
				if(planet != null){
					try {
						planet.landPlayer(p);
					} catch (PlanetsCardException e) {
						throw new RuntimeException("Somehow a player choose to land on a planet that was already occupied");
					}
				}
			}
		}


		game.getPIRHandler().broadcastPIR(
				game
						.getPlayersInFlight()
						.stream()
						.filter(p -> getPlanetByPlayer(p) != null)
						.toList(),
				(player, pirHandler) -> {
                    try {
                        addLoadablesInteraction(player, pirHandler);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
		);

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
