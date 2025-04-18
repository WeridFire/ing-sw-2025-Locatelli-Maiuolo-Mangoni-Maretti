package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CLIScreen implements ICLIPrintable {

	protected final String screenName;
	private String screenMessage;
	private final boolean forceActivate;
	private int priority = 0;

	/**
	 * Abstract class for a CLI screen. Contains the standardized methods and fields to design a new screen.
	 * A screen is an object that displays information on the CLI based on the current game state.
	 * The user, based on the screen they are on, can perform different commands.
	 * A user can swap between screens using the screen global command. Screens differ in availability, and
	 * based on the current state of the game there might be different available screens.
	 * @param screenName The identifier of the screen.
	 * @param forceActivate if to forcefully activate this screen whenever an update satisfying it will be received.
	 * @param priority if there are multiple screens not force-activable, priority will indicate which one to prioritize.
	 */
	public CLIScreen(String screenName, boolean forceActivate, int priority) {
		this.screenName = screenName;
		this.forceActivate = forceActivate;
		this.priority = priority;
	}

	/**
	 * @param screenName The identifier of the screen.
	 * @see #CLIScreen(String)  CLIScreen
	 */
	public CLIScreen(String screenName){
		this(screenName, false, 0);
	}

	/**
	 * The condition a state has to fullfill for a screen to be activable.
	 * This does not include if the screen is currently active on the screen handler, but just based on the game state.
	 * @return If the screen can be activable.
	 */
	protected abstract boolean switchConditions();

	/**
	 * This function basically allows the main CLI logic to delegate to the active screen the handling of a command.
	 * Passes the command and the args, and the screen tries to execute. If it fails, the screen will display the error
	 * and the requirements for the command to execute.
	 * @requires this.switchConditions() == true
	 * @param command The command to execute
	 * @param args The args to pass
	 */
	protected abstract void processCommand(String command, String[] args) throws RemoteException;

	/**
	 * Refreshes the whole screen. Causes the CLI to clear, print newly the screen using the specific screen logic,
	 * and then prints any eventual error or message.
	 */
	protected final void refresh(){
		CLIFrame screen = getCLIRepresentation();
		if (screen == null) screen = new CLIFrame();

		CLIFrame popups = new CLIFrame();
		if(CLIScreenHandler.getInstance().isShowingHelpScreen){
			popups = popups.merge(popupAvailableCommands(),
					AnchorPoint.CENTER, AnchorPoint.CENTER);
		}
		if(CLIScreenHandler.getInstance().isShowingAvailableScreens){
			popups = popups.merge(CLIScreenHandler.getInstance().popupAvailableScreens(),
					AnchorPoint.CENTER, AnchorPoint.CENTER);
		}

		clear();
		// print the screen with popups at the center
		System.out.println(screen.merge(popups, AnchorPoint.CENTER, AnchorPoint.CENTER));

		if(getLastUpdate().getError() != null){
			displayError();
		}
		if(screenMessage != null){
			displayScreenMessage();
		}
		System.out.print("\n> ");
	}

	/**
	 * This function should return the commands available in the specific screen
	 * to pass as parameter while calling the method printCommands.
	 * Can be immutable.
	 */
	protected abstract List<String> getScreenSpecificCommands();

	/*
	!!!BELOW THIS YOU DON'T NEED TO OVERRIDE ANYTHING!!!
	 */

	protected final CLIFrame popupAvailableCommands(){
		return popupCommands(screenName, getScreenSpecificCommands().toArray(new String[0]));
	}

	protected final IClient getClient(){
		return CLIScreenHandler.getInstance().getGameClient().getClient();
	}

	protected final IServer getServer() throws RemoteException {
		return getClient().getServer();
	}

	protected final ClientUpdate getLastUpdate(){
		return CLIScreenHandler.getInstance().getLastUpdate();
	}

	public final boolean isForceActivate() {
		return forceActivate;
	}

	public final int getPriority() {
		return priority;
	}

	private void displayError(){
		if(getLastUpdate().getError() != null){
			System.out.println(ANSI.RED + "[SERVER ERROR] " + getLastUpdate().getError() + ANSI.RESET);
		}
	}

	private void displayScreenMessage(){
		if(this.screenMessage != null){
			System.out.println(ANSI.YELLOW + "[SCREEN INFO] " + screenMessage + ANSI.RESET);
			this.screenMessage = null;
		}
	}

	/**
	 * Sets an informative message on the client's screen, to inform about something (usually the failed
	 * execution of the command on the client-side checks). Causes a full screen refresh.
	 * @param message The message to display in yellow under the screen.
	 */
	final void setScreenMessage(String message){
		this.screenMessage = message;
		this.refresh();
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
							.append(ANSI.RESET).append(" | ");
					// add all the description without overflowing the frame width
					int colsLeft = frameWidth - ANSI.Helper.stripAnsi(cmdBuilder.toString()).length();
					while (parts[1].length() > colsLeft) {
						cmdBuilder.append(parts[1], 0, colsLeft);
						cmdList.add(cmdBuilder.toString());
						parts[1] = parts[1].substring(colsLeft);
						cmdBuilder = new StringBuilder(" ".repeat(spacesOnAvoidOverload));
						colsLeft = frameWidth - spacesOnAvoidOverload;
					}
					cmdBuilder.append(parts[1]);
				} else {
					cmdBuilder.append(ANSI.CYAN).append("> ").append(command);
				}
				cmdList.add(cmdBuilder.toString());
			}
			CLIFrame cmdFrame = new CLIFrame(cmdList.toArray(new String[0]));
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
