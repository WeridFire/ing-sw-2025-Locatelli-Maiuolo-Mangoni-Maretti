package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.player.Player;

public class PIRYesNoChoice extends PIRMultipleChoice{
	/**
	 * Object that makes the server wait for player to make a 2-ways choice. The choice is either yes or no.
	 * Will auto last 30 seconds, and then use default.
	 *
	 * @param currentPlayer   The player the game waits for
	 * @param cooldown        The cooldown duration.
	 * @param message         The message to pass with the choice
	 * @param defaultChoice   The default choice if the player does not respond on the action
	 */
	public PIRYesNoChoice(Player currentPlayer, int cooldown, String message, boolean defaultChoice) {
		super(currentPlayer, cooldown, message, new String[]{"YES", "NO"}, defaultChoice ? 0 : 1);
	}

	boolean isChoiceYes(){
		return getChoice() == 0;
	}
}
