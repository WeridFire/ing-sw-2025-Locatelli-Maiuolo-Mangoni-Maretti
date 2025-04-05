package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.playerInput.PIRs.PIRRemoveLoadables;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.view.cli.CLIFrame;

public class SlaversCard extends EnemyCard {

	/**
	 * How many crew members to remove.
	 */
	private final int punishCrewAmount;

	/**
	 * The amount of money to award to the player that beats this card.
	 */
	private final int prizeBounty;


	/**
	 * @param firePower   firepower of this enemy
	 * @param lostDays    days required to loot this enemy
	 * @param textureName the texture of the card
	 * @param level       the level this card is part of
	 * @param punishCrewAmount The amount of crew members to remove upon being beat by this card.
	 * @param prizeBounty The amount of money to award to the player that beats this card.
	 */
	public SlaversCard(int punishCrewAmount, int prizeBounty, int firePower, int lostDays, String textureName, int level) {
		super(firePower, lostDays, textureName, level);
		this.punishCrewAmount = punishCrewAmount;
		this.prizeBounty = prizeBounty;
	}

	@Override
	public void givePrize(Player player, GameData game) {
		PIRYesNoChoice pirYesNoChoice = new PIRYesNoChoice(player,
				30,
				"You will receive " + prizeBounty +" credits, but you will lose "
						+ getLostDays() + " days.",
				true);
		boolean wantToAccept = game.getPIRHandler().setAndRunTurn(pirYesNoChoice);
		if(wantToAccept){
			player.addCredits(prizeBounty);
			game.movePlayerBackward(player, getLostDays());
		}
	}

	/**
	 *
	 * @param player player on which the method is currently acting upon
	 */
	@Override
	public void applyPunishment(Player player, GameData game) {
		PIRRemoveLoadables pirRemoveLoadables = new PIRRemoveLoadables(player, 30, LoadableType.CREW_SET, punishCrewAmount);
		game.getPIRHandler().setAndRunTurn(pirRemoveLoadables);
	}

	/**
	 * Generates a CLI representation of the implementing object.
	 *
	 * @return A {@link CLIFrame} containing the CLI representation.
	 */
	@Override
	public CLIFrame getCLIRepresentation() {
		// TODO
		return null;
	}
}
