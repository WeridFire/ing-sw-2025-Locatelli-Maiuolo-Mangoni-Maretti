package it.polimi.ingsw.task.customTasks;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.task.TaskType;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.Set;
import java.util.function.BiConsumer;

public class TaskMultipleChoice extends Task {

	private int choice;
	private final String[] possibleOptions;
	private final String choiceMessage;
	private final CLIFrame optionalFrame;
	private boolean choiceSelectionCompleted;
	private final BiConsumer<Player, Integer> onFinish;


	public TaskMultipleChoice(String currentPlayer, int cooldown, String message, CLIFrame toShow,
							  String[] possibleOptions, int defaultChoice, BiConsumer<Player, Integer> onFinish) {
		super(currentPlayer, cooldown, TaskType.CHOICE);
		this.possibleOptions = possibleOptions;
		this.choiceMessage = message;
		this.optionalFrame = toShow;
		this.choice = defaultChoice;
		this.choiceSelectionCompleted = false;
		this.onFinish = onFinish;
	}


	@Override
	public boolean checkCondition() {

		if(choiceSelectionCompleted){
			return true;
		}
		if(getEpochTimestamp() > getExpiration()){
			return true;
		}

	}

	@Override
	protected void finish() {
		Player player = getPlayer();
		this.onFinish.accept(player, this.choice);
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return Set.of();
	}

	@Override
	public void makeChoice(Player player, int choice) throws WrongPlayerTurnException {
		checkForTurn(player.getUsername());
		this.choice = choice;
		choiceSelectionCompleted = true;
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
				new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getDuration() + " seconds" + ANSI.RESET + " to respond."),
				Direction.SOUTH, 1
		);

		// Create a container screen with a fixed size, background, and border
		int containerRows = Math.max(frame.getRows() + 2, 24);
		CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.BLACK);

		// Merge the content into the screen, centered
		return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
	}

}
