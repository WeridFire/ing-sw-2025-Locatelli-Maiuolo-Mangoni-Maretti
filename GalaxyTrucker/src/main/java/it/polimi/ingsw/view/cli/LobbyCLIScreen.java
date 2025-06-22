package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.commandsProcessors.LobbyCommandsProcessor;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.player.Player;

import java.util.*;
import java.util.stream.Collectors;

public class LobbyCLIScreen extends CLIScreen {

	public LobbyCLIScreen(GameClient gameClient) {
		super("lobby", true, 0, new LobbyCommandsProcessor(gameClient));
	}

	@Override
	protected boolean switchConditions() {
		return CommonState.isCurrentPhase(GamePhaseType.LOBBY);
	}

	private String getPlayerDisplayInfo(Player player, boolean isLobbyLeader) {
		String prefix, suffix;
		if (player.isConnected()) {
			prefix = ANSI.GREEN;
			suffix = ANSI.RESET;
		} else {
			prefix = ANSI.YELLOW;
			suffix = " [disconnected]" + ANSI.RESET;
		}
		return prefix +
				(isLobbyLeader ? " * " : " - ")
				+ player.getUsername() + suffix;
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		// Screen border with white background (and default foreground)
		CLIFrame screenBorder = getScreenFrame(24, 100, ANSI.BACKGROUND_WHITE);

		// Lobby info frame with blue background (and default foreground)
		CLIFrame lobbyInfoFrame = getScreenFrame(18, 80, ANSI.BACKGROUND_BLUE);

		// Title in yellow foreground on blue background, then reset
		CLIFrame title = new CLIFrame(new String[]{
				ANSI.WHITE + "LOBBY INFO" + ANSI.RESET
		});
		lobbyInfoFrame = lobbyInfoFrame.merge(title, AnchorPoint.TOP, AnchorPoint.TOP, 1, 0);

		GameData currentGame = LobbyState.getGameData();

		String lobbyID = currentGame.getGameId().toString();
		String requiredPlayers = String.valueOf(currentGame.getRequiredPlayers());
		GameLevel currentLevel = currentGame.getLevel();
		String host = currentGame.getGameLeader();
		String yourName = LobbyState.getPlayerUsername();

		// Build game info block with contrasting label and value colors.
		List<String> lobbyInfoLines = new ArrayList<>();
		lobbyInfoLines.add(ANSI.BLACK + "Lobby ID: " + lobbyID + ANSI.RESET);
		lobbyInfoLines.add(ANSI.BLACK + "Leader: " + host + ANSI.RESET);
		lobbyInfoLines.add(ANSI.BLACK + "Your Name: " + yourName + ANSI.RESET);
		lobbyInfoLines.add(ANSI.BLACK + "Required Players: " + requiredPlayers + ANSI.RESET);

		// Flight levels: highlight the current level in purple, others in white.
		String levelsDisplay = Arrays.stream(GameLevel.values())
				.map(level -> level == currentLevel
						? ANSI.BACKGROUND_YELLOW + ANSI.BLACK + level.toString() + ANSI.RESET
						: ANSI.CYAN + level.toString() + ANSI.RESET)
				.collect(Collectors.joining(ANSI.YELLOW + " | " + ANSI.RESET));
		lobbyInfoLines.add(ANSI.BLACK + "Flight Level: " + ANSI.RESET + levelsDisplay);

		CLIFrame gameInfoBG = getScreenFrame(7, 60, ANSI.BACKGROUND_WHITE);
		CLIFrame gameInfoBlock = new CLIFrame(lobbyInfoLines.toArray(new String[0]));
		gameInfoBlock = gameInfoBG.merge(gameInfoBlock, AnchorPoint.CENTER, AnchorPoint.CENTER);
		// Merge the game info block into the lobby info frame (with some vertical offset)
		lobbyInfoFrame = lobbyInfoFrame.merge(gameInfoBlock, AnchorPoint.TOP, AnchorPoint.TOP, 3, 0);

		// Build the lobby members block
		List<String> membersLines = new ArrayList<>();
		membersLines.add("");
		membersLines.add(ANSI.CYAN + "Lobby Members" + ANSI.RESET);
		currentGame.getPlayers().forEach(player -> membersLines.add(getPlayerDisplayInfo(player,
				Objects.equals(LobbyState.getGameData().getGameLeader(), player.getUsername()))));
		membersLines.add("");

		CLIFrame membersFrame = getScreenFrame(membersLines.size(), 30, ANSI.BACKGROUND_WHITE)
				.merge(new CLIFrame(membersLines.toArray(new String[0])), AnchorPoint.CENTER, AnchorPoint.CENTER);
		// Merge lobby members block in the center of the lobby info frame
		lobbyInfoFrame = lobbyInfoFrame.merge(membersFrame, AnchorPoint.BOTTOM, AnchorPoint.BOTTOM, -1, 0);

		// Merge the lobby info frame into the screen border
		CLIFrame res = screenBorder.merge(lobbyInfoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

		// Tip for game leader (in red) at the bottom, if applicable
		if (LobbyState.isGameLeader()) {
			CLIFrame tip = new CLIFrame(new String[]{
					"",
					ANSI.BACKGROUND_GREEN + "Tip: Change the game settings with the command >settings" + ANSI.RESET
			});
			res = res.merge(tip, AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);
		}
		return res;
	}


}
