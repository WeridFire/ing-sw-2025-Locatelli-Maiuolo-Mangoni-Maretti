package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRType;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.Set;

public class PIRMultipleChoice extends PIR {

	private int choice;
	private final String[] possibleOptions;
	private final String choiceMessage;
	private final CLIFrame optionalFrame;

	/**
	 * Object that makes the server wait for player to make a 2-ways choice. The choice is either yes or no.
	 * Will auto last 30 seconds, and then use default.
	 *
	 * @param currentPlayer  The player the game waits for
	 * @param cooldown       The cooldown duration.
	 * @param message The message to pass with the choice.
	 * @param toShow A frame to show between message and the choices list
	 * @param possibleOptions A list of the possible options to choose from
	 * @param defaultChoice The index of the default choice if the player does not respond on the action
	 */
	public PIRMultipleChoice(Player currentPlayer, int cooldown, String message, CLIFrame toShow,
							 String[] possibleOptions, int defaultChoice) {
		super(currentPlayer, cooldown, PIRType.CHOICE);
		this.possibleOptions = possibleOptions;
		this.choiceMessage = message;
		this.optionalFrame = toShow;
		this.choice = defaultChoice;
	}

	/**
	 * Object that makes the server wait for player to make a 2-ways choice. The choice is either yes or no.
	 * Will auto last 30 seconds, and then use default.
	 *
	 * @param currentPlayer  The player the game waits for
	 * @param cooldown       The cooldown duration.
	 * @param message The message to pass with the choice.
	 * @param possibleOptions A list of the possible options to choose from
	 * @param defaultChoice The index of the default choice if the player does not respond on the action
	 */
	public PIRMultipleChoice(Player currentPlayer, int cooldown, String message, String[] possibleOptions, int defaultChoice) {
		this(currentPlayer, cooldown, message, null, possibleOptions, defaultChoice);
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

	@Override
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

	@Override
	public CLIFrame getCLIRepresentation() {
		// containerRows below
		int containerColumns = 100;

		// Header frame
		CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " PLAYER INPUT REQUEST " + ANSI.RESET)
				.merge(new CLIFrame(""), Direction.SOUTH);

		// Message section
		frame = frame.merge(
				new CLIFrame(choiceMessage).paintForeground(ANSI.YELLOW)
						.wrap(containerColumns - 2, 1, AnchorPoint.CENTER),
				Direction.SOUTH, 1
		);

		// (optional) frame-to-show section
		if (optionalFrame != null) {
			frame = frame.merge(optionalFrame, Direction.SOUTH, 1);
		}

		// Options section
		if (possibleOptions != null && possibleOptions.length > 0) {
			CLIFrame optionsFrame = new CLIFrame(ANSI.CYAN + "Available Options:" + ANSI.RESET);

			for (int i = 0; i < possibleOptions.length; i++) {
				optionsFrame = optionsFrame.merge(
						new CLIFrame(ANSI.GREEN + "[" + i + "] " + ANSI.RESET + possibleOptions[i]),
						Direction.SOUTH, 1
				);
			}
			frame = frame.merge(optionsFrame, Direction.SOUTH, 1);
		} else {
			frame = frame.merge(
					new CLIFrame(ANSI.RED + "No available options!" + ANSI.RESET),
					Direction.SOUTH, 1
			);
		}

		// Command hint
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + "Command: " + ANSI.GREEN + ">choose [NUMBER]" + ANSI.RESET),
				Direction.SOUTH, 2
		);

		// Timeout information
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getCooldown() + " seconds" + ANSI.RESET + " to respond."),
				Direction.SOUTH, 1
		);

		// Create a container screen with a fixed size, background, and border
		int containerRows = Math.max(frame.getRows() + 2, 24);
		CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.BLACK);

		// Merge the content into the screen, centered
		return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
	}


}
