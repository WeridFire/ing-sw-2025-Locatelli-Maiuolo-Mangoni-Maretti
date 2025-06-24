package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class SmugglersCard extends EnemyCard {


	private final LoadableType[] prizeGoods;
	private final int punishCargo;

	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param punishCargo the amount of cargo to be removed from the player that is beaten by this card
	 * @param prizeGoods The goods earned by the player that beats this card.
	 */
	public SmugglersCard(int punishCargo, LoadableType[] prizeGoods, int firePower, int lostDays, String textureName, int level) {
		super(firePower, lostDays, "SMUGGLERS", textureName, level);
		this.punishCargo = punishCargo;
		this.prizeGoods = prizeGoods;
	}

	@Override
	public void givePrize(Player player, GameData game) {
		PIRYesNoChoice pirYesOrNoChoice = new PIRYesNoChoice(player,
				30,
				"You will receive the following goods: " + prizeGoods + " but you will lose " + getLostDays() + " travel days.",
				true);
		boolean wantToAccept = game.getPIRHandler().setAndRunTurn(pirYesOrNoChoice);
		if(wantToAccept){
			int loadablesAmount = player
					.getShipBoard()
					.getVisitorCalculateCargoInfo()
					.getGoodsInfo()
					.getAllLoadedItems().size();
			if(loadablesAmount > 0){
				boolean result = game.getPIRHandler().setAndRunTurn(
						new PIRYesNoChoice(player, 30, "Do you want to rearrange the goods already on your ship?", false)
				);
				if(result) {
					List<LoadableType> loadablesToAdd = player
							.getShipBoard()
							.getVisitorCalculateCargoInfo()
							.getGoodsInfo()
							.getAllLoadedItems();
					player.getShipBoard().loseBestGoods(loadablesToAdd.size());
					game.getPIRHandler().setAndRunTurn(
							new PIRAddLoadables(player, 30, loadablesToAdd)
					);
				}
			}


			PIRAddLoadables pirAddLoadables = new PIRAddLoadables(player, 30, Arrays.stream(prizeGoods).toList());
			game.getPIRHandler().setAndRunTurn(pirAddLoadables);
			game.movePlayerBackward(player, getLostDays());
		}
	}

	@Override
	public void applyPunishment(Player player, GameData game) {
		player.getShipBoard().loseBestGoods(this.punishCargo);
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
				ANSI.BLACK + "Punish cargo: " + punishCargo + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Fire power: " + getFirePower() + ANSI.RESET
		);
		cardInfoLines.add(
				ANSI.BLACK + "Lost days: " + getLostDays() + ANSI.RESET
		);

		cardInfoLines.add(
				""
		);

		cardInfoLines.add(
				ANSI.BLACK + "Prizes: " + ANSI.RESET
		);
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < prizeGoods.length; i++) {
			if (i % 4 == 0 && i != 0) {
				cardInfoLines.add(line.toString());
				line = new StringBuilder();
			}
			line.append(prizeGoods[i].getUnicodeColoredString());
		}

		cardInfoLines.add(
				line.toString()
		);


		CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

		return super.getCLIRepresentation()
				.merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
	}
}
