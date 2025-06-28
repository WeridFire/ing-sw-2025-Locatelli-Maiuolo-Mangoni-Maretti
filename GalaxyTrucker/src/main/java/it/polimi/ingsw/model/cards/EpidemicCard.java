package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.model.shipboard.visitors.VisitorEpidemic;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.*;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class EpidemicCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public EpidemicCard(String textureName, int level) {
		super("EPIDEMIC", textureName, level);
	}

	/**
	 * For each player, iterates on all the tiles present in the shipboard. Then for each one it applies the check of
	 * looking for adjacent tiles, to kill the passengers.
	 */
	@Override
	public void playEffect(GameData game) throws InterruptedException {
		VisitorEpidemic visitor = new VisitorEpidemic();
		game.getPIRHandler().broadcastPIR(
				game.getPlayersInFlight(),
				(player, pirHandler) -> {
					PIRDelay pirDelay = new PIRDelay(
							player,
							10,
							"An epidemic has struck your ship! Some of your crew died",
							null);
					pirHandler.setAndRunTurn(pirDelay);
				});
		for (Player p : game.getPlayersInFlight()) {
			ShipBoard shipBoard = p.getShipBoard();
			Map<Coordinates, TileSkeleton> board = shipBoard.getTilesOnBoard();
			board.values().forEach(tile -> {
				tile.accept(visitor);
			});

			visitor.applyEpidemicEffect(board);
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
				ANSI.BLACK + "Some crewmember" + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "died from covid!" + ANSI.RESET
		);
		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
