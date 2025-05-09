package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.cp.MenuCommandsProcessor;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameClient;

import java.util.ArrayList;
import java.util.List;

public class MenuCLIScreen extends CLIScreen {

	/**
	 * Creates a screen used for displaying the main-menu phase. Displays the available games
	 * and allows commands to join and create a match.
	 */
	public MenuCLIScreen(GameClient gameClient) {
		super("menu", true, 0, new MenuCommandsProcessor(gameClient));
	}

	@Override
	protected boolean switchConditions() {
		return CommonState.getGameData() == null;
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		// Create the borders with colored backgrounds.
		CLIFrame screenBorder = getScreenFrame(24, 100, ANSI.BACKGROUND_WHITE);
		CLIFrame gamesListBorder = getScreenFrame(16, 60, ANSI.BACKGROUND_YELLOW);

		// Create a title with a red foreground and a default (reset) background.
		CLIFrame title = new CLIFrame(new String[]{ANSI.BACKGROUND_RED + ANSI.WHITE + "AVAILABLE GAMES" + ANSI.RESET});
		gamesListBorder = gamesListBorder.merge(title, AnchorPoint.TOP, AnchorPoint.CENTER, 1, 0);

		List<GameData> availableGames = CommonState.getLastUpdate().getAvailableGames();
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
