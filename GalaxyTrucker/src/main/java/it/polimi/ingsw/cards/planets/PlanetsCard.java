package it.polimi.ingsw.cards.planets;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.exceptions.PlanetsCardException;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

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
		super(textureName, level);
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
	 * Iteratively asks each player if they want to land. Then in reverse order allows players to pickup their items
	 * and move back in days.
	 */
	@Override
	public void playEffect(GameData game) {
		for (Player p : game.getPlayers()){
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
						throw new RuntimeException("Somehow a player choose to land on a planet that was already");
					}
					//TODO: allow rearrrangement / discard of goods
					game.getPIRHandler().setAndRunTurn(
							new PIRAddLoadables(p, 30, planet.getAvailableGoods())
					);
				}
			}
		}
		for(Player p : game.getPlayers().reversed()){
			Planet landedPlanet = Arrays.stream(planets)
					.filter(pl -> pl.getCurrentPlayer().equals(p))
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

		CLIFrame cardBorder = getScreenFrame(11, 20, ANSI.BACKGROUND_CYAN);

		// frame title
		CLIFrame title = new CLIFrame(new String[]{
				ANSI.WHITE + "PLANETS" + ANSI.RESET
		});
		cardBorder = cardBorder.merge(title, AnchorPoint.TOP, AnchorPoint.CENTER, 0, 0);

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

		cardBorder = cardBorder.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

		return cardBorder;
	}
}
