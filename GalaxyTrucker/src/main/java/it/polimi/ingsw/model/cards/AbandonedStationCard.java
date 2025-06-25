package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

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
		super("ABANDONED STATION", textureName, level);
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
		for(Player p : game.getPlayersInFlight()){
			if(p.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET) >= requiredCrew){
				boolean result = game.getPIRHandler().setAndRunTurn(
						new PIRYesNoChoice(p, Default.PIR_SECONDS, "Do you want to loot the station? " +
								"You will lose " + lostDays + " travel days, but you will receive the " +
								"following loot: " + Arrays.toString(availableCargo)
								, false)
				);
				if(result){ //meaning they accepted to do it
					game.getPIRHandler().setAndRunTurn(
							new PIRAddLoadables(p, Default.PIR_SECONDS, List.of(availableCargo))
					);
					game.movePlayerBackward(p, lostDays);
					break;
				}
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
				ANSI.BLACK + "Required Crew: " + requiredCrew + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Goods:  "  + ANSI.RESET
		);

		StringBuilder line = new StringBuilder();
		for (int i = 0; i < availableCargo.length; i++) {
			if (i % 4 == 0 && i != 0) {
				cardInfoLines.add(line.toString());
				line = new StringBuilder();
			}
			line.append(availableCargo[i].getUnicodeColoredString());
		}
		cardInfoLines.add(ANSI.BLACK + line.toString());

		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

	}
}
