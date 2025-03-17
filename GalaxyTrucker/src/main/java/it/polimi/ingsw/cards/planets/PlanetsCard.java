package src.main.java.it.polimi.ingsw.cards.planets;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.cards.exceptions.PlanetsCardException;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.Arrays;
import java.util.UUID;

public class PlanetsCard extends Card {



	private Planet[] planets;
	private int lostDays;


	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 * @param planets Instances of the planets, each one populated with the goods available on it.
	 * @param lostDays amount of days lost upon landing on a planet.
	 */
	public PlanetsCard(Planet[] planets, int lostDays, String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
	}

	/**
	 * Gets, given an index, the corresponding planet.
	 * @param i the index of the planet.
	 * @throws PlanetsCardException the planet does not exists in the list of planetsi n the card.
	 * @return
	 */
	public Planet getPlanet(int i) throws PlanetsCardException{
		if(i<0 || i >= planets.length){
			throw new PlanetsCardException("Could not find a planet with this index.")
		}
		return planets[i];
	}

	/**
	 * Iteratively asks each player if they want to land. Then in reverse order allows players to pickup their items
	 * and move back in days.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		for (Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()){
			//TODO: ask player if they wanna land. We should get here directly the reference to the planet to land on.
			//TODO: the check that ID is valid AND the planet is free should be handled in the controller.

			Planet planet = getPlanet(0);
			planet.landPlayer(p);

		}
		for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers().reversed()){
			Planet landedPlanet = Arrays.stream(planets)
					.filter(pl -> pl.getCurrentPlayer().equals(p))
					.findFirst()
					.orElse(null);

			if(landedPlanet != null){
				landedPlanet.lootPlanet();
				landedPlanet.leavePlanet();
				movePlayer(p, lostDays);
			}
		}
	}
}
