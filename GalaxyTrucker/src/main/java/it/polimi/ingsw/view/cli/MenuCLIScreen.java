package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuCLIScreen extends CLIScreen {

	/**
	 * Creates a screen used for displaying the main-menu phase. Displays the available games
	 * and allows commands to join and create a match.
	 */
	public MenuCLIScreen() {
		super("menu", true, 0);
	}

	@Override
	protected boolean switchConditions() {
		return getLastUpdate().getCurrentGame() == null;
	}

	/*
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
			System.out.println("\n" + ANSI.ANSI_GREEN_BACKGROUND + ANSI.ANSI_BLACK
					+ " Tip: " + ANSI.ANSI_RESET + ANSI.ANSI_GREEN
					+ "Join a game with " + ANSI.ANSI_YELLOW + ">join"
					+ ANSI.ANSI_RESET);
		} else {
			System.out.println(ANSI.ANSI_RED + "There are no available games." + ANSI.ANSI_RESET);
			System.out.println("\n" + ANSI.ANSI_GREEN_BACKGROUND + ANSI.ANSI_BLACK
					+ " Tip: " + ANSI.ANSI_RESET + ANSI.ANSI_GREEN
					+ "Create a game with " + ANSI.ANSI_YELLOW + ">create"
					+ ANSI.ANSI_RESET);
		}
	}
	 */

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
				setScreenMessage("Invalid command. Use help to view available commands.");
				break;
		}
	}

	@Override
	protected void printScreenSpecificCommands() {
		printCommands(screenName, "refresh|Refresh the game list.",
											"join|Join an existing game.",
											"create|Create a new game.");
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		// Create the borders with colored backgrounds.
		CLIFrame screenBorder = getScreenFrame(24, 100, ANSI.BACKGROUND_WHITE);
		CLIFrame gamesListBorder = getScreenFrame(16, 60, ANSI.BACKGROUND_YELLOW);

		// Create a title with a red foreground and a default (reset) background.
		CLIFrame title = new CLIFrame(new String[]{ANSI.BACKGROUND_RED + ANSI.WHITE + "AVAILABLE GAMES" + ANSI.RESET});
		gamesListBorder = gamesListBorder.merge(title, AnchorPoint.TOP, AnchorPoint.CENTER, 1, 0);

		List<GameData> availableGames = getLastUpdate().getAvailableGames();
		if (!availableGames.isEmpty()) {
			List<String> gameLines = new ArrayList<>();
			availableGames.stream()
					.limit(13)
					.forEach(g -> gameLines.add(String.format(
							"> " + ANSI.BLACK + "[%-10s]" + ANSI.RESET + " (%d/%d players)",
							g.getGameId().toString(),
							g.getPlayers().size(),
							g.getRequiredPlayers()
					)));
			CLIFrame gamesContent = new CLIFrame(gameLines.toArray(new String[0]));
			gamesListBorder = gamesListBorder.merge(gamesContent, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 3, 2);
		} else {
			// Add a fallback message in magenta.
			CLIFrame noGames = new CLIFrame(new String[]{
									ANSI.BACKGROUND_RED + "There are no available games." + ANSI.RESET
			});
			CLIFrame startOne = new CLIFrame(new String[]{ANSI.BACKGROUND_RED + "Start one with "+ANSI.WHITE +">create" + ANSI.RESET});
			gamesListBorder = gamesListBorder.merge(noGames, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
			gamesListBorder = gamesListBorder.merge(startOne, AnchorPoint.CENTER, AnchorPoint.CENTER, 1, 0);

		}

		// Merge the games border into the screen.
		CLIFrame res = screenBorder.merge(gamesListBorder, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

		// Create a tip message in green.
		String tipText = availableGames.isEmpty()
				? "Create a game with >create"
				: "Join a game with >join";
		CLIFrame tip = new CLIFrame(new String[]{
				"",
				ANSI.BACKGROUND_GREEN + ANSI.WHITE + "Tip" + ": " + tipText + ANSI.RESET
		});
		res = res.merge(tip, AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);

		return res;
	}

}
