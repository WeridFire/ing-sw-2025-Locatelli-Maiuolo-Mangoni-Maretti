package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;

import java.util.Set;

public class PIRMultipleChoice extends PIR {

	private int choice;
	private final String[] possibleOptions;
	private final String choiceMessage;

	/**
	 * Object that makes the server wait for player to make a 2-ways choice. The choice is either yes or no.
	 * Will auto last 30 seconds, and then use default.
	 *
	 * @param currentPlayer  The player the game waits for
	 * @param cooldown       The cooldown duration.
	 * @param message The message to pass with the choice.
	 * @param defaultChoice The default choice if the player does not respond on the action
	 */
	public PIRMultipleChoice(Player currentPlayer, int cooldown, String message, String[] possibleOptions, int defaultChoice) {
		super(currentPlayer, cooldown, it.polimi.ingsw.playerInput.PIRType.CHOICE);
		this.possibleOptions = possibleOptions;
		this.choiceMessage = message;
		this.choice = defaultChoice;
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return Set.of();
	}

	@Override
	public void run() throws InterruptedException {
		synchronized (lock){
			lock.wait(getCooldown() * 1000L);
		}
	}

	@Override
	void endTurn() {
		synchronized (lock){
			lock.notifyAll();
		}
	}

	public void makeChoice(Player player, int choice) throws WrongPlayerTurnException {
		checkForTurn(player);
		this.choice = choice;
		endTurn();
	}

	 int getChoice() {
		return choice;
	}

	public String[] getPossibleOptions(){
		return possibleOptions;
	}

	public String getChoiceMessage() {
		return choiceMessage;
	}

}
