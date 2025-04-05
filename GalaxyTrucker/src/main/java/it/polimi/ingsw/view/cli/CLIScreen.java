package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CLIScreen implements ICLIPrintable {

	protected final String screenName;
	private String screenMessage;
	private final boolean forceActivate;


	/**
	 * Abstract class for a CLI screen. Contains the standardized methods and fields to design a new screen.
	 * A screen is an object that displays information on the CLI based on the current game state.
	 * The user, based on the screen they are on, can perform different commands.
	 * A user can swap between screens using the screen global command. Screens differ in availability, and
	 * based on the current state of the game there might be different available screens.
	 * @param screenName The identifier of the screen.
	 * @param forceActivate if to forcefully activate this screen whenever an update satisfying it will be received.
	 */
	public CLIScreen(String screenName, boolean forceActivate){
		this.screenName = screenName;
		this.forceActivate = forceActivate;
	}

	/**
	 * @param screenName The identifier of the screen.
	 * @see #CLIScreen(String, boolean)  CLIScreen
	 */
	public CLIScreen(String screenName){
		this(screenName, false);
	}

	/**
	 * The condition a state has to fullfill for a screen to be activable.
	 * This does not include if the screen is currently active on the screen handler, but just based on the game state.
	 * @return If the screen can be activable.
	 */
	protected abstract boolean switchConditions();

	/**
	 * Prints the screen to the CLI. Gets called on every refresh, and prints out the actual
	 * juice of the screen. This does not include "service" messages such as server errors, which
	 * are handled in their own functions.
	 */
	protected final void printScreen(){
		System.out.println(getCLIRepresentation());
	}

	/**
	 * This function basically allows the main CLI logic to delegate to the active screen the handling of a command.
	 * Passes the command and the args, and the screen tries to execute. If it fails, the screen will display the error
	 * and the requirements for the command to execute.
	 * @param command The command to execute
	 * @param args The args to pass
	 * @throws RemoteException
	 */
	protected abstract void processCommand(String command, String[] args) throws RemoteException, IllegalArgumentException;

	/**
	 * Refreshes the whole screen. Causes the CLI to clear, print newly the screen using the specific screen logic,
	 * and then prints any eventual error or message.
	 */
	protected final void refresh(){
		clear();
		printScreen();
		if(getLastUpdate().getError() != null){
			displayError();
		}
		if(screenMessage != null){
			displayScreenMessage();
		}
		System.out.print("\n> ");
	}

	/**
	 * This function should call inside of it the method printCommands, passing the commands available
	 * in the specific screen.
	 * Check out the examples in {@link MenuCLIScreen#printScreenSpecificCommands()}.
	 */
	abstract void printScreenSpecificCommands();

	/*
	!!!BELOW THIS YOU DON'T NEED TO OVERRIDE ANYTHING!!!
	 */

	protected final void printAvailableCommands(){
		clear();
		printScreenSpecificCommands();
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

	private void displayError(){
		if(getLastUpdate().getError() != null){
			System.out.println(ANSI.ANSI_RED + "[SERVER ERROR] " + getLastUpdate().getError() + ANSI.ANSI_RESET);
		}
	}

	private void displayScreenMessage(){
		if(this.screenMessage != null){
			System.out.println(ANSI.ANSI_YELLOW + "[SCREEN INFO] " + screenMessage + ANSI.ANSI_RESET);
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
	 * Prints commands for a specific screen name.
	 * This method will display a blue header with the screen name, followed by each command on a new line.
	 * Each command should be formatted as <b>command</b>|<b>description</b>, where:
	 * <ul>
	 *     <li><b>command</b> is the name or identifier of the command.</li>
	 *     <li><b>description</b> is a brief explanation of what the command does.</li>
	 * </ul>
	 *
	 * @param screenName The name of the screen for which the commands are printed. This will be displayed as the header.
	 * @param commands An array or list of commands to be printed, each formatted as <command>|<description>.
	 */
	public final void printCommands(String screenName, String... commands) {
		CLIScreen.clear();
		CLIFrame frame = CLIScreen.getScreenFrame(14, 60, ANSI.ANSI_BLACK_BACKGROUND);

		String header = ANSI.ANSI_BLUE_BACKGROUND +
				ANSI.ANSI_RED + " " + screenName.toUpperCase() +
				ANSI.ANSI_RESET + ANSI.ANSI_BLUE_BACKGROUND + " SPECIFIC COMMANDS " +
				ANSI.ANSI_RESET;
		List<String> cmds = null;
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
					cmdBuilder.append(ANSI.ANSI_CYAN).append("> ").append(parts[0])
							.append(ANSI.ANSI_RESET).append(" | ").append(parts[1]);
				} else {
					cmdBuilder.append(ANSI.ANSI_CYAN).append("> ").append(command);
				}
				cmdList.add(cmdBuilder.toString());
			}
			CLIFrame cmdFrame = new CLIFrame(cmdList.toArray(new String[0]));
			frame = frame.merge(cmdFrame, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 3, 1);
		} else {
			frame = frame.merge(new CLIFrame(ANSI.ANSI_RED_BACKGROUND + ANSI.ANSI_WHITE + "No Commands Available"),
					AnchorPoint.CENTER, AnchorPoint.CENTER, -1, 0);
		}

		frame = frame.merge(new CLIFrame(ANSI.ANSI_RED_BACKGROUND + ANSI.ANSI_WHITE + "Enter to close"),
				AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);

		CLIFrame currentScr = CLIScreenHandler.getInstance().getCurrentScreen().getCLIRepresentation();
		currentScr = currentScr.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
		System.out.println(currentScr);
	}



	public static CLIFrame getScreenFrame(int rows, int columns){
		return getScreenFrame(rows, columns, ANSI.ANSI_RESET);
	}

	public static CLIFrame getScreenFrame(int rows, int columns, String bg_color) {
		StringBuilder top = new StringBuilder(ANSI.ANSI_RESET).append("┏");
		StringBuilder middle = new StringBuilder(ANSI.ANSI_RESET).append("┃").append(bg_color);
		StringBuilder bottom = new StringBuilder(ANSI.ANSI_RESET).append("┗");

		for (int i = 0; i < columns; i++) {
			top.append(ANSI.ANSI_RESET).append("━");
			middle.append(bg_color).append(" ");
			bottom.append(ANSI.ANSI_RESET).append("━");
		}

		top.append(ANSI.ANSI_RESET).append("┓");
		middle.append(ANSI.ANSI_RESET).append("┃");
		bottom.append(ANSI.ANSI_RESET).append("┛");

		String[] frame = new String[rows + 2];
		frame[0] = top.toString();
		for (int i = 1; i <= rows; i++) {
			frame[i] = middle.toString();
		}
		frame[rows + 1] = bottom.toString();

		return new CLIFrame(frame, false);
	}



}
