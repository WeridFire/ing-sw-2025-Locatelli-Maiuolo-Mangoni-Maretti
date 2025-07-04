package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRUtils;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class StarDustCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public StarDustCard(String textureName, int level) {
		super("STARDUST", textureName, level);
	}

	/**
	 * Iterates each player (still flying) in reverse, counts how many connectors each one has, and moves accordingly.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayersInFlight().reversed()){
			int exposedConnectors = countExposedConnectors(p);
			PIRUtils.runPlayerMovementBackward(p, exposedConnectors, game, movement ->
					"Some stardust got into some exposed components on your ship! You lose "
                    + movement + " travel days to clean the mess up!");
		}
	}

	public int countExposedConnectors(Player player) {
		return player.getShipBoard().getExposedConnectorsCount();
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
				ANSI.BLACK + "    Count your   " + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "exposed connectors!" + ANSI.RESET
		);
		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
