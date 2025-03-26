package src.main.java.it.polimi.ingsw.cards.planets;

import src.main.java.it.polimi.ingsw.cards.exceptions.PlanetsCardException;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.List;

public class Planet {
	/**
	 * Dynamic list of the currently available goods on the planet.
	 */
	private List<LoadableType> availableGoods;

	/**
	 * The player currently landed on the planet.
	 */
	private Player currentPlayer;

	/**
	 * Instances a planet for the PlanetsCard.
	 * @param availableGoods The goods available on the planet.
	 */
	public Planet(List<LoadableType> availableGoods){
		this.availableGoods = availableGoods;
	}

	/**
	 * Lands the player on the planet.
	 * @param player The player to perform the landing on.
	 * @throws PlanetsCardException if the planet is already occupied.
	 */
	public void landPlayer(Player player) throws PlanetsCardException{
		if(currentPlayer != null){
			throw new PlanetsCardException("Unable to land on planet: already occupied.");
		}
		currentPlayer = player;
	}

	/**
	 *
	 * @return The player currently landed on the planet.
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 *
	 * @ the dynamic list of the currently available goods on the planet.
	 */
	public List<LoadableType> getAvailableGoods() {
		return availableGoods;
	}

	/**
	 * Assigns a good from the planet available goods to a player.
	 * @param good the good to load on the ship. The good must be present on the planet.
	 * @throws PlanetsCardException if the good is not present on the planet.
	 */
	public void assignGood(LoadableType good) throws PlanetsCardException {
		if(availableGoods.contains(good)){
			availableGoods.remove(good);
			//TODO: Ask player the Tile where to put the good on. Add the good in the correct position on the ship.
			return;
		}
		throw new PlanetsCardException("The good is not present on the planet.");
	}

	/**
	 * Loots the planet, asking iteratively the player what cargo item to load / discard.
	 */
	public void lootPlanet(){
		//TODO: whole implementation, iterate through cargo, ask, perform.
	}

	/**
	 * Frees up the planet, making the landed player leave.
	 */
	public void leavePlanet(){
		currentPlayer = null;
	}


}
