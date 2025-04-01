package it.polimi.ingsw.playerInput;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;

import java.util.Set;

public class PIRChoice extends PIR {

	private boolean choice;
	private final String choiceMessage;

	/**
	 * Object that makes the server wait for player to make a 2-ways choice. The choice is either yes or no.
	 * Will auto last 30 seconds, and then use default.
	 *
	 * @param currentPlayer  The player the game waits for
	 * @param cooldown       The cooldown duration.
	 * @param choiceMessage The message to pass with the choice.
	 * @param defaultChoice The default choice if the player does not respond on the action
	 */
	public PIRChoice(Player currentPlayer, int cooldown, String choiceMessage, boolean defaultChoice) {
		super(currentPlayer, cooldown, PIRType.CHOICE);
		this.choice = defaultChoice;
		this.choiceMessage = choiceMessage;
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
	public void endTurn() {
		synchronized (lock){
			lock.notifyAll();
		}
	}

	@Override
	public void makeChoice(Player player, boolean choice) throws WrongPlayerTurnException {
		checkForTurn(player);
		this.choice = choice;
		endTurn();
	}

	public boolean getChoice() {
		return choice;
	}

	public String getChoiceMessage() {
		return choiceMessage;
	}

}
