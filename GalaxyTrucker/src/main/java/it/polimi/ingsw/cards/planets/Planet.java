package src.main.java.it.polimi.ingsw.cards.planets;

import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.List;

public class Planet {
	/**
	 * Dynamic list of the currently available goods on the planet.
	 */
	private List<CargoType> availableGoods;

	/**
	 * The player currently landed on the planet.
	 */
	private Player currentPlayer;

	/**
	 * Instances a planet for the PlanetsCard.
	 * @param availableGoods The goods available on the planet.
	 */
	public Planet(List<CargoType> availableGoods){
		this.availableGoods = availableGoods;
	}

	/**
	 *
	 * @ the dynamic list of the currently available goods on the planet.
	 */
	public List<CargoType> getAvailableGoods() {
		return availableGoods;
	}

	public void assignGood(CargoType good){
		if(availableGoods.contains(good)){
			availableGoods.remove(good);
			//TODO: Ask player the Tile where to put the good on. Add the good in the correct position on the ship.
		}
	}
}
