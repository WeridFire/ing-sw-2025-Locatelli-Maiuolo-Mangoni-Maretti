package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.ArrayList;
import java.util.List;

public class WarZoneCard extends Card {

	/**
	 * Levels of this warZone.
	 */
	private final WarLevel[] warLevels;

	/**
	 * Instances a card.
	 * @param warLevels the levels for this warzone.
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public WarZoneCard(WarLevel[] warLevels, String textureName, int level) {
		super("WAR ZONE", textureName, level);
		this.warLevels = warLevels;
	}

	/**
	 * Iterates each single war level. For each one selects the worst player. Punishes that player.
	 */
	@Override
	public void playEffect(GameData game) throws InterruptedException {
		// verify at least two players
		List<Player> playersInFlight = game.getPlayersInFlight();
		if (playersInFlight.isEmpty()) return;
		if (playersInFlight.size() == 1) {
			game.getPIRHandler().setAndRunTurn(new PIRDelay(playersInFlight.getFirst(),
					Default.PIR_SHORT_SECONDS, "Since you are the only player still in flight, "
					+ getTitle() + " has no effects on you!", null));
			return;
		}
		// play war
		for(WarLevel wl : warLevels){
			Player p = wl.getWorstPlayer(playersInFlight);
			if(p != null){
				wl.applyPunishment(p, game);
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
		CLIFrame baseFrame = super.getCLIRepresentation();
		int colsLimit = baseFrame.getColumns() - 2;  // - (1+1) for borders

		int[] levelHeights = new int[warLevels.length];
		int index = 0;
		CLIFrame infoFrame = new CLIFrame();
		for (WarLevel warLevel : warLevels) {
			CLIFrame levelFrame = warLevel.getCLIRepresentation(colsLimit);
			levelHeights[index] = levelFrame.getRows();
			infoFrame = infoFrame.merge(levelFrame, AnchorPoint.BOTTOM_LEFT, AnchorPoint.TOP_LEFT, 1, 0);
			index++;
		}

		CLIFrame containerFrame = CLIScreen.getScreenFrame(levelHeights, colsLimit);
		ArrayList<String> containerLines = new ArrayList<>(List.of(containerFrame.getContentAsLines()));
		containerLines.removeFirst();
		containerLines.removeLast();

		return baseFrame
				.merge(new CLIFrame(containerLines.toArray(new String[0]))
								.merge(infoFrame, AnchorPoint.TOP, AnchorPoint.TOP, -1, 0),
						AnchorPoint.CENTER, AnchorPoint.CENTER);
	}
}
