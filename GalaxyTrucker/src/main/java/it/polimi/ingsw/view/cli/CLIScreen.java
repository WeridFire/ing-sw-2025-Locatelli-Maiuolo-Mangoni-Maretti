package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.cp.ICommandsProcessor;
import it.polimi.ingsw.controller.cp.PhaseCommandsProcessor;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.view.IView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CLIScreen implements ICLIPrintable {

	protected final String screenName;
	private String screenMessage;
	private final boolean forceActivate;
	private final int priority;

	private final PhaseCommandsProcessor commandsProcessor;
	private CLIView parentView;

	/**
	 * Abstract class for a CLI screen. Contains the standardized methods and fields to design a new screen.
	 * A screen is an object that displays information on the CLI based on the current game state.
	 * The user, based on the screen they are on, can perform different commands (see {@code commandsProcessor}).
	 * A user can swap between screens using the screen global command. Screens differ in availability, and
	 * based on the current state of the game there might be different available screens.
	 *
	 * @param screenName The identifier of the screen.
	 * @param forceActivate if to forcefully activate this screen whenever an update satisfying it will be received.
	 * @param priority if there are multiple screens not force-activable, priority will indicate which one to prioritize.
	 *
	 * @param commandsProcessor The commands processor that manages the execution of command and args.<br>
	 * This implementation allows the main CLI logic to delegate to this specific active screen the handling of a command.
	 * The specified processor manages how the command and the arguments are parsed and how the screen should execute it.
	 * If it fails, the screen will display the error and the requirements for the command to execute.<br>
	 * Note: the {@link ICommandsProcessor#processCommand(String, String[])} requires this.switchConditions() == true
	 */
	public CLIScreen(String screenName, boolean forceActivate, int priority, PhaseCommandsProcessor commandsProcessor) {
		this.screenName = screenName;
		this.forceActivate = forceActivate;
		this.priority = priority;
		this.commandsProcessor = commandsProcessor;
	}

	/**
	 * The condition a state has to fullfill for a screen to be activable.
	 * This does not include if the screen is currently active on the screen handler, but just based on the game state.
	 * @return If the screen can be activable.
	 */
	protected abstract boolean switchConditions();

	public final void setParentView(CLIView parentView) {
		this.parentView = parentView;
	}

	protected final IView getParentView() {
		return parentView;
	}

	public final PhaseCommandsProcessor getCommandsProcessor() {
		return commandsProcessor;
	}

	/**
	 * Refreshes the whole screen. Causes the CLI to clear, print newly the screen using the specific screen logic,
	 * and then prints any eventual error or message.
	 */
	protected final void refresh() {
		if (parentView == null) throw new NullPointerException("Parent view is null");

		CLIFrame screen = getCLIRepresentation();
		if (screen == null) screen = new CLIFrame();

		CLIFrame popups = new CLIFrame();
		if (parentView.isShowingHelpScreen) {
			popups = popups.merge(popupAvailableCommands(),
					AnchorPoint.CENTER, AnchorPoint.CENTER);
		}
		if (parentView.isShowingAvailableScreens) {
			popups = popups.merge(parentView.popupAvailableScreens(),
					AnchorPoint.CENTER, AnchorPoint.CENTER);
		}

		clear();
		/* TODO: there are concurrency problems! TWO clears, then two System.out.println(screen...)
		    note: do not easy fix with a lock, because first of all there shouldn't be two concurrent refreshes
		    -> find that problem, fix, then eventually if needed fix this concurrency problem
		 */
		// print the screen with popups at the center
		System.out.println(screen.merge(popups, AnchorPoint.CENTER, AnchorPoint.CENTER));

		displayError();
		displayScreenMessage();

		System.out.print("\n> ");
	}

	/*
	!!!BELOW THIS YOU DON'T NEED TO OVERRIDE ANYTHING!!!
	 */

	protected final CLIFrame popupAvailableCommands(){
		return popupCommands(screenName, commandsProcessor.getAvailableCommands().toArray(new String[0]));
	}

	public final boolean isForceActivate() {
		return forceActivate;
	}

	public final int getPriority() {
		return priority;
	}

	private void displayError(){
		String error = CommonState.getLastUpdate().getError();
		if (error != null) {
			System.out.println(ANSI.RED + "[SERVER ERROR] " + error + ANSI.RESET);
		}
	}

	private void displayScreenMessage(){
		if (screenMessage != null) {
			System.out.println(ANSI.YELLOW + "[SCREEN INFO] " + screenMessage + ANSI.RESET);
			screenMessage = null;
		}
	}

	/**
	 * Sets an informative message on the client's screen, to inform about something (usually the failed
	 * execution of the command on the client-side checks). Causes a full screen refresh.
	 * @param message The message to display in yellow under the screen.
	 */
	protected final void setScreenMessage(String message){
		setScreenMessage(message, true);
	}

	/**
	 * Sets an informative message on the client's screen, to inform about something (usually the failed
	 * execution of the command on the client-side checks). The message is shown after a refresh.
	 * Set {@code refresh} to {@code true} to include that refresh,
	 * {@code false} if the caller wants to manage the refresh by itself.
	 * @param message The message to display in yellow under the screen.
	 * @param refresh {@code true} if a full screen refresh is desired with the setting of this message,
	 *                           {@code false} otherwise (note that no refresh <===> no screen message visualization)
	 */
	protected final void setScreenMessage(String message, boolean refresh){
		this.screenMessage = message;
		if (refresh) {
			parentView.onRefresh();
		}
	}

	protected static void clear(){
		for (int i = 0; i < 50; i++) {
			System.out.println();
		}
	}

	/**
	 * Return the {@link CLIFrame} of commands with a specific screen name and commands.
	 * This will be a popup which display a blue header with the screen name, followed by each command on a new line.
	 * Each command should be formatted as <b>command</b>|<b>description</b>, where:
	 * <ul>
	 *     <li><b>command</b> is the name or identifier of the command.</li>
	 *     <li><b>description</b> is a brief explanation of what the command does.</li>
	 * </ul>
	 *
	 * @param screenName The name of the screen for which the commands are printed. This will be displayed as the header.
	 * @param commands An array or list of commands to be printed, each formatted as {@code command|description}.
	 */
	public final CLIFrame popupCommands(String screenName, String... commands) {
		final int frameWidth = 60;
		final int spacesOnAvoidOverload = 1;
		CLIFrame frame = CLIScreen.getScreenFrame(14, frameWidth, ANSI.BACKGROUND_BLACK);

		String header = ANSI.BACKGROUND_BLUE +
				ANSI.RED + " " + screenName.toUpperCase() +
				ANSI.RESET + ANSI.BACKGROUND_BLUE + " SPECIFIC COMMANDS " +
				ANSI.RESET;
		List<String> cmds;
		if(commands == null){
			cmds = new ArrayList<>();
		}else{
			cmds = new ArrayList<>(Arrays.asList(commands));
		}
		cmds.addFirst("ping|Ping the host server.");
		cmds.addFirst("screen|Navigate screens.");
		cmds.addFirst("help|Get all the available commands.");
		cmds.addFirst("debug|Create a json containing the current game state.");
		frame = frame.merge(new CLIFrame(header), AnchorPoint.TOP, AnchorPoint.CENTER, 1, 0);

		if (!cmds.isEmpty()) {
			List<String> cmdList = new ArrayList<>();
			for (String command : cmds) {
				StringBuilder cmdBuilder = new StringBuilder();
				String[] parts = command.split("\\|", 2);
				if (parts.length == 2) {
					cmdBuilder.append(ANSI.CYAN).append("> ").append(parts[0])
							.append(ANSI.RESET).append(" | ").append(parts[1]);
				} else {
					cmdBuilder.append(ANSI.CYAN).append("> ").append(command);
				}
				cmdList.add(cmdBuilder.toString());
			}
			CLIFrame cmdFrame = new CLIFrame(cmdList.toArray(new String[0]))
					.wrap(frameWidth, spacesOnAvoidOverload, AnchorPoint.LEFT);
			frame = frame.merge(cmdFrame, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 3, 1);
		} else {
			frame = frame.merge(new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + "No Commands Available"),
					AnchorPoint.CENTER, AnchorPoint.CENTER, -1, 0);
		}

		frame = frame.merge(new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + "Enter to close"),
				AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);

		return frame;
	}


	public static CLIFrame getScreenFrame(int[] rowsBlocks, int columns, String fillColor, String borderColor) {
		StringBuilder top = new StringBuilder(ANSI.RESET).append(borderColor).append("┏");
		StringBuilder middle = new StringBuilder(ANSI.RESET).append(borderColor).append("┃");
		if (!fillColor.equals(ANSI.RESET)) {
			middle.append(fillColor);
		}
		StringBuilder separator = new StringBuilder(ANSI.RESET).append(borderColor).append("┣");
		StringBuilder bottom = new StringBuilder(ANSI.RESET).append(borderColor).append("┗");

		for (int i = 0; i < columns; i++) {
			top.append("━");
			middle.append(" ");
			separator.append("━");
			bottom.append("━");
		}

		String sTop = top.append("┓").append(ANSI.RESET).toString();
		String sMiddle = middle.append(ANSI.RESET).append(borderColor).append("┃").append(ANSI.RESET).toString();
		String sSeparator = separator.append("┫").append(ANSI.RESET).toString();
		String sBottom = bottom.append("┛").append(ANSI.RESET).toString();

		int totRows = Arrays.stream(rowsBlocks).sum()  // middle empty
				+ rowsBlocks.length - 1;  // separators
		String[] frame = new String[totRows + 2];
		frame[0] = sTop;
		int contentIndex = 1;
		for (int rows : rowsBlocks) {
			for (int i = 0; i < rows; i++) {
				frame[contentIndex++] = sMiddle;
			}
			frame[contentIndex++] = sSeparator;  // last will be overridden
		}
		frame[totRows + 1] = sBottom;

		return new CLIFrame(frame);
	}

	public static CLIFrame getScreenFrame(int rows, int columns, String fillColor, String borderColor) {
		return getScreenFrame(new int[] {rows}, columns, fillColor, borderColor);
	}

	public static CLIFrame getScreenFrame(int[] rowsBlocks, int columns, String fillColor) {
		return getScreenFrame(rowsBlocks, columns, fillColor, ANSI.RESET);
	}

	public static CLIFrame getScreenFrame(int rows, int columns, String fillColor) {
		return getScreenFrame(new int[] {rows}, columns, fillColor);
	}

	public static CLIFrame getScreenFrame(int[] rowsBlocks, int columns) {
		return getScreenFrame(rowsBlocks, columns, ANSI.RESET, ANSI.RESET);
	}

	public static CLIFrame getScreenFrame(int rows, int columns) {
		return getScreenFrame(new int[] {rows}, columns);
	}



}
