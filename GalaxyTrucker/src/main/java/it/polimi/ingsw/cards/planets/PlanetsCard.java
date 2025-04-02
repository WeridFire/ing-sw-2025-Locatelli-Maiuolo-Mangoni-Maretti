package it.polimi.ingsw.cards.planets;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.exceptions.PlanetsCardException;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
}
