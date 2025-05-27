package it.polimi.ingsw.task.customTasks;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.task.TaskType;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.Set;
import java.util.function.Consumer;

public class TaskDelay extends Task {

	private final String message;
	private final CLIFrame cliToShow;
	private final String player;
	private final Consumer<Player> onFinish;

	public TaskDelay(String player, int cooldown, String message, CLIFrame toShow, Consumer<Player> onFinish){
		super(null, cooldown, TaskType.DELAY);
		this.message = message;
		cliToShow = (toShow != null) ? toShow : new CLIFrame();
		this.player = player;
		this.onFinish = onFinish;
	}

	@Override
	public boolean checkCondition() {
		return getEpochTimestamp() > getExpiration();
	}


	@Override
	protected void finish() {
		Player player = getPlayer();
		this.onFinish.accept(player);
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
				new CLIFrame(message).paintForeground(ANSI.YELLOW)
						.wrap(containerColumns - 2, 1, AnchorPoint.CENTER),
				Direction.SOUTH, 1
		);

		// Info section
		frame = frame.merge(cliToShow, Direction.SOUTH, 2);

		// Command hint
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + "Press " + ANSI.GREEN + "[Enter]" + ANSI.WHITE + " to continue"),
				Direction.SOUTH, 2
		);

		// Create a container screen with a fixed size, background, and border
		int containerRows = Math.max(frame.getRows() + 2, 24);
		CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.BLACK);

		// Merge the content into the screen, centered
		return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
	}
}
