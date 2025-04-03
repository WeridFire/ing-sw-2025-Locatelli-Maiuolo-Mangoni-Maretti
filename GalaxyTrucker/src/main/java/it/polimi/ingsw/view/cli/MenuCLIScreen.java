package it.polimi.ingsw.view.cli;

import java.rmi.RemoteException;
import java.util.UUID;

public class MenuCLIScreen extends CLIScreen{

	/**
	 * Creates a screen used for displaying the main-menu phase. Displays the available games
	 * and allows commands to join and create a match.
	 */
	public MenuCLIScreen() {
		super("menu", true);
	}

	@Override
	protected boolean switchConditions() {
		return getLastUpdate().getCurrentGame() == null;
	}

	@Override
	protected void printScreen() {
		if (!getLastUpdate().getAvailableGames().isEmpty()) {
			System.out.println(ANSI.ANSI_YELLOW + "\n======= AVAILABLE GAMES =======" + ANSI.ANSI_RESET);
			getLastUpdate().getAvailableGames().forEach((g) ->
					System.out.printf(ANSI.ANSI_GREEN + "[%s] " + ANSI.ANSI_BLUE + "(%d/%d players)" + ANSI.ANSI_RESET + " %n",
							g.getGameId().toString(),
							g.getPlayers().size(),
							g.getRequiredPlayers())
			);
			System.out.println(ANSI.ANSI_YELLOW + "================================" + ANSI.ANSI_RESET);
		} else {
			System.out.println(ANSI.ANSI_RED + "There are no available games." + ANSI.ANSI_RESET);
		}
	}

	@Override
	protected void processCommand(String command, String[] args) throws RemoteException {
		switch(command){
			case "refresh":
				getServer().ping(getClient());
				break;
			case "join":
				if (args.length == 2) {
					UUID uuid = UUID.fromString(args[0]);
					String username = args[1];
					getServer().joinGame(getClient(), uuid, username);
				} else {
					setScreenMessage("Usage: join <uuid> <username>");
				}
				break;
			case "create":
				if (args.length == 1) {
					String username = args[0];
					// Call the appropriate method to handle create logic here
					getServer().createGame(getClient(), username);
				} else {
					setScreenMessage("Usage: create <username>");
				}
				break;
			default:
				setScreenMessage("Invalid command. Available commands are: join, create, help, screen");
				break;
		}
	}

	@Override
	protected void printAvailableCommands() {
		super.printAvailableCommands();
		printCommands(screenName, "refresh|Refresh the game list.", "join|Join an existing game.", "create|Create a new game.");
	}
}
