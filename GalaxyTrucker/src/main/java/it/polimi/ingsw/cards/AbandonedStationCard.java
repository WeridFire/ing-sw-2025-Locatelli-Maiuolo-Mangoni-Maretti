package it.polimi.ingsw.cards;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.shipboard.LoadableType;

import java.util.List;

public class AbandonedStationCard extends Card{
	/**
	 * Cargo available on the ship. TODO: consider if to convert to dynamic?
	 */
	private LoadableType[] availableCargo;
	/**
	 * The days removed when looting the station.
	 */
	private final int lostDays;
	/**
	 * The crew required to access the station.
	 */
	private final int requiredCrew;

	/**
	 * Instances a card.
	 * @param availableCargo the cargo available on the abandoned ship.
	 * @param lostDays the lost days in case of ship looting.
	 * @param requiredCrew the amount of crew required to loot the station.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public AbandonedStationCard(LoadableType[] availableCargo, int lostDays, int requiredCrew, String textureName, int level) {
		super(textureName, level);
		this.availableCargo = availableCargo;
		this.lostDays = lostDays;
		this.requiredCrew = requiredCrew;
	}

	/**
	 * Iterates through each player, looking for the first one that can (and wants) to take over the station.
	 * @param game The game data associated with the request.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayers()){
			if(p.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET) >= requiredCrew){
				boolean result = game.getPIRHandler().setAndRunTurn(
						new PIRYesNoChoice(p, 30, "Do you want to loot the station? " +
								"You will lose " + lostDays + " travel days, but you will receive the " +
								"following loot: " + availableCargo
								, false)
				);
				if(result){ //meaning they accepted to do it
					game.getPIRHandler().setAndRunTurn(
							new PIRAddLoadables(p, 30, List.of(availableCargo))
					);
					game.movePlayerBackward(p, lostDays);
					break;
				}
			}
		}
	}
}
