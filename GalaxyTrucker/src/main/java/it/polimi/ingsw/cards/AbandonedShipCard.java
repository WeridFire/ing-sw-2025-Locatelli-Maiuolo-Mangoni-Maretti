package it.polimi.ingsw.cards;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;

import java.util.UUID;

public class AbandonedShipCard extends Card{

	/**
	 * The crew that is required and will be taken away when taking the ship.
	 */
	private final int requiredCrew;
	/**
	 * The days removed when taking the ship.
	 */
	private final int lostDays;
	/**
	 * The credits earned upon fixing and selling the ship.
	 */
	private final int sellPrice;

	/**
	 * Instances a card.
	 * @param requiredCrew the crew that is required and will be taken away when selling the ship.
	 * @param lostDays the days removed when taking the ship.
	 * @param sellPrice the credits earned upon fixing and selling the ship.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public AbandonedShipCard(int requiredCrew, int lostDays, int sellPrice, String textureName, int level) {
		super(textureName, level);
		this.requiredCrew = requiredCrew;
		this.lostDays = lostDays;
		this.sellPrice = sellPrice;
	}

	/**
	 * Iterates through each player, checking if they can take the ship. If they can (and want), removes crew from their
	 * ship and awards the credits.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayers()){
			if(p.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET) >= requiredCrew){
				//TODO: ask player if they want to actually take the ship.
				if(true){ //meaning they accepted to do it
					for(int i=0; i<requiredCrew; i++){
						//TODO: asks player where they want to remove crew from
					}
					game.movePlayerBackward(p, lostDays);
					break;
				}
			}
		}
	}
}
