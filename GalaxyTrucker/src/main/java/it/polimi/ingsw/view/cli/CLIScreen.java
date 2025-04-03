package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;

public abstract class CLIScreen {

	protected final String screenName;
	private String screenMessage;
	/**
	 * Abstract class for a CLI screen. Contains the standardized methods and fields to design a new screen.
	 * A screen is an object that displays information on the CLI based on the current game state.
	 * The user, based on the screen they are on, can perform different commands.
	 * A user can swap between screens using the screen global command. Screens differ in availability, and
	 * based on the current state of the game there might be different available screens.
	 * @param screenName The identifier of the screen.
	 */
	public CLIScreen(String screenName){
		this.screenName = screenName;
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
	protected abstract void processCommand(String command, String[] args) throws RemoteException;

	/**
	 * Refreshes the whole screen. Causes the CLI to clear, print newly the screen using the specific screen logic,
	 * and then prints any eventual error or message.
	 */
	protected void refresh(){
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


	protected IClient getClient(){
		return CLIScreenHandler.getInstance().getGameClient().getClient();
	}

	protected IServer getServer() throws RemoteException {
		return getClient().getServer();
	}

	protected  ClientUpdate getLastUpdate(){
		return CLIScreenHandler.getInstance().getLastUpdate();
	}

	private void displayError(){
		if(getLastUpdate().getError() != null){
			System.out.println(ANSI.ANSI_RED + "[SERVER ERROR] " + getLastUpdate().getError() + ANSI.ANSI_RESET);
		}
	}

	private void displayScreenMessage(){
		if(this.screenMessage != null){
			System.out.println(ANSI.ANSI_YELLOW + "[SCREEN ERROR] " + screenMessage + ANSI.ANSI_RESET);
			this.screenMessage = null;
		}
	}

	void setScreenMessage(String message){
		this.screenMessage = message;
		this.refresh();
	}

	protected static void clear(){
		for (int i = 0; i < 50; i++) {
			System.out.println();
		}
	}

	protected void printAvailableCommands(){
		clear();
		printCommands("GLOBAL", "ping|Ping the host server.", "screen|Navigate screens.");
	}

	public void printCommands(String screenName, String... commands) {
		String stringBuilder = ANSI.ANSI_BLUE_BACKGROUND +
				ANSI.ANSI_RED +
				screenName.toUpperCase() +
				ANSI.ANSI_RESET +
				ANSI.ANSI_BLUE_BACKGROUND +
				" SPECIFIC COMMANDS" +
				ANSI.ANSI_RESET;
		System.out.println(stringBuilder);

		for (String command : commands) {
			String[] parts = command.split("\\|", 2);
			if (parts.length == 2) {
				System.out.println(ANSI.ANSI_CYAN + ">" + parts[0] + ANSI.ANSI_RESET + " | " + parts[1]);
			} else {
				System.out.println(ANSI.ANSI_CYAN + ">" + command + ANSI.ANSI_RESET);
			}
		}
	}


}
