package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;

import java.util.Set;
import java.util.UUID;

public class AbandonedStationCard extends Card{
	/**
	 * Cargo available on the ship. TODO: consider if to convert to dynamic?
	 */
	private LoadableType[] availableCargo;
	/**
	 * The days removed when looting the station.
	 */
	private int lostDays;
	/**
	 * The crew required to access the station.
	 */
	private int requiredCrew;

	/**
	 * Instances a card.
	 * @param availableCargo the cargo available on the abandoned ship.
	 * @param lostDays the lost days in case of ship looting.
	 * @param requiredCrew the amount of crew required to loot the station.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 * @param gameId      The ID of the game this card is part of.
	 */
	public AbandonedStationCard(LoadableType[] availableCargo, int lostDays, int requiredCrew, String textureName, int level, UUID gameId) {
		super(textureName, level, gameId);
		this.availableCargo = availableCargo;
		this.lostDays = lostDays;
		this.requiredCrew = requiredCrew;
	}

	/**
	 * Iterates through each player, looking for the first one that can (and wants) to take over the station.
	 * @param gameId The UUID of the game associated to this card, to access the game handler.
	 */
	@Override
	public void playEffect(UUID gameId) {
		for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()){
			if (p.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET) >= requiredCrew){
				//TODO: ask player if they want to actually take over the station.
				if(true){ //meaning they accepted to do it
					for(LoadableType c : availableCargo){
						//TODO: asks player where they want to put each single cargo.
					}
					movePlayer(p, lostDays);
					break;
				}
			}
		}
	}
}
