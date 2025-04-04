package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;

public abstract class CLIScreen {

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
	protected abstract void printScreen();

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
	};

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
		printCommands("global", "ping|Ping the host server.", "screen|Navigate screens.", "help|Get all the available commands.", "debug|Create a json containing the current game state.");
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
		String stringBuilder = ANSI.ANSI_BLUE_BACKGROUND +
				ANSI.ANSI_RED +
				" " + screenName.toUpperCase() +
				ANSI.ANSI_RESET +
				ANSI.ANSI_BLUE_BACKGROUND +
				" SPECIFIC COMMANDS " +
				ANSI.ANSI_RESET;
		System.out.println(stringBuilder);
		if(commands != null){
			for (String command : commands) {
				String[] parts = command.split("\\|", 2);
				if (parts.length == 2) {
					System.out.println(ANSI.ANSI_CYAN + ">" + parts[0] + ANSI.ANSI_RESET + " | " + parts[1]);
				} else {
					System.out.println(ANSI.ANSI_CYAN + ">" + command + ANSI.ANSI_RESET);
				}
			}
		}

		if(!screenName.equals("global")){
			printBackButton(screenName);
		}
	}

	public static void printBackButton(String screenName){
		System.out.print(ANSI.ANSI_RED_BACKGROUND+" <- BACK TO " +screenName.toUpperCase() +" " +ANSI.ANSI_RESET);
	}


}
