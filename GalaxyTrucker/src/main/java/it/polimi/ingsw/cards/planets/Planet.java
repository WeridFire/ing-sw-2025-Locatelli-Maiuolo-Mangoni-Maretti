package it.polimi.ingsw.cards.planets;

import it.polimi.ingsw.cards.exceptions.PlanetsCardException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;

import java.io.Serializable;
import java.util.List;

public class Planet implements Serializable {
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
	 * Frees up the planet, making the landed player leave.
	 */
	public void leavePlanet(){
		currentPlayer = null;
	}


}
