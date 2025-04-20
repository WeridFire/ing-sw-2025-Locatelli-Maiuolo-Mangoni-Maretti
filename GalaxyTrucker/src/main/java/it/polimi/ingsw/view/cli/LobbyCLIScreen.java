package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class LobbyCLIScreen extends CLIScreen{

	public LobbyCLIScreen() {
		super("lobby", true, 0);
	}

	@Override
	protected boolean switchConditions() {
		//Player must be in a game and game must be in phase lobby.
		return getLastUpdate().getCurrentGame() != null &&
				getLastUpdate().getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.LOBBY;
	}


	/*
	@Override
	protected void printScreen() {

		List<String> lobbyMembers = getLastUpdate().getCurrentGame().getPlayers().stream()
				.map(Player::getUsername)
				.toList();

		String lobbyID = getLastUpdate().getCurrentGame().getGameId().toString();
		String requiredPlayersAmount = String.valueOf(getLastUpdate().getCurrentGame().getRequiredPlayers());
		GameLevel currentLevel = getLastUpdate().getCurrentGame().getLevel();
		String host = getLastUpdate().getCurrentGame().getGameLeader();
		String yourName = getLastUpdate().getClientPlayer() != null ?
				getLastUpdate().getClientPlayer().getUsername()
				:
				"Unknown";

		System.out.println(ANSI.ANSI_YELLOW + "\n======= LOBBY INFO =======" + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Lobby ID: " + ANSI.ANSI_CYAN + lobbyID + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Leader: " + ANSI.ANSI_CYAN + host + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Your Name: " + ANSI.ANSI_CYAN + yourName + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Required Players: " + ANSI.ANSI_CYAN + requiredPlayersAmount + ANSI.ANSI_RESET);

		// Print all levels, highlight current one
		String levelsDisplay = Arrays.stream(GameLevel.values())
				.map(level -> level == currentLevel
						? ANSI.ANSI_YELLOW_BACKGROUND + ANSI.ANSI_BLACK + level.toString() + ANSI.ANSI_RESET
						: ANSI.ANSI_CYAN + level.toString() + ANSI.ANSI_RESET
				)
				.collect(Collectors.joining(ANSI.ANSI_WHITE + " | " + ANSI.ANSI_RESET));

		System.out.println(ANSI.ANSI_GREEN + "Flight Level: " + levelsDisplay);

		System.out.println(ANSI.ANSI_YELLOW + "============================" + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_BLUE + "\nLobby Members:" + ANSI.ANSI_RESET);
		lobbyMembers.forEach(member ->
				System.out.println(ANSI.ANSI_CYAN + "- " + member + ANSI.ANSI_RESET)
		);
		if (getLastUpdate().isGameLeader()) {
			System.out.println("\n" + ANSI.ANSI_GREEN_BACKGROUND + ANSI.ANSI_BLACK
					+ " Tip: " + ANSI.ANSI_RESET + ANSI.ANSI_GREEN
					+ "Change the game settings with the command " + ANSI.ANSI_YELLOW + ">settings"
					+ ANSI.ANSI_RESET);
		}
	}
	 */



	@Override
	protected void processCommand(String command, String[] args) throws RemoteException {
		switch(command){
			case "": break;  // on simple enter do nothing in particular

			case "settings":
				if(!getLastUpdate().isGameLeader()){
					setScreenMessage("You must be the game leader to perform this command.");
					break;
				}
				if (args.length == 2){
					GameLevel level = getLastUpdate().getCurrentGame().getLevel();
					int minPlayers = getLastUpdate().getCurrentGame().getRequiredPlayers();
					switch(args[0].toLowerCase()){
						case "level":
							try{
								level = GameLevel.valueOf(args[1]);
							}catch(IllegalArgumentException e){
								setScreenMessage("Available levels: " +
										Arrays.stream(GameLevel.values())
												.map(Enum::toString)
												.collect(Collectors.joining(", "))
								);
								return;
							}
							break;
						case "minplayers": //fallthrough
						case "requiredplayers":
							minPlayers = Integer.parseInt(args[1]);
							break;
					}
					//if player specified already set settings, we don't bother with sending the update to
					// the server.
					if(level == getLastUpdate().getCurrentGame().getLevel() &&
					minPlayers == getLastUpdate().getCurrentGame().getRequiredPlayers()){
						refresh();
						break;
					}else{
						getServer().updateGameSettings(getClient(), level, minPlayers);
						break;
					}
				}
				setScreenMessage("Usage: settings <level|minplayers>");
				break;
			case "leave":
				setScreenMessage("Function not implemented yet.");
				break;
			default:
				setScreenMessage("Invalid command. Use help to view available commands.");
				break;
		}
	}

	@Override
	protected List<String> getScreenSpecificCommands() {
		List<String> availableCommands = new ArrayList<>(
				List.of("leave|Leave the current lobby.")
		);

		if(getLastUpdate().isGameLeader()){
			availableCommands.add("settings|Change the game settings.");
		}
		return availableCommands;
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
		lobbyInfoFrame = lobbyInfoFrame.merge(title, AnchorPoint.TOP, AnchorPoint.CENTER, 1, 0);

		GameData currentGame = getLastUpdate().getCurrentGame();
		List<String> lobbyMembers = currentGame.getPlayers().stream()
				.map(Player::getUsername)
				.toList();

		String lobbyID = currentGame.getGameId().toString();
		String requiredPlayers = String.valueOf(currentGame.getRequiredPlayers());
		GameLevel currentLevel = currentGame.getLevel();
		String host = currentGame.getGameLeader();
		String yourName = getLastUpdate().getClientPlayer() != null
				? getLastUpdate().getClientPlayer().getUsername()
				: "Unknown";

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

		CLIFrame gameInfoBG = getScreenFrame(8, 60, ANSI.BACKGROUND_WHITE);
		CLIFrame gameInfoBlock = new CLIFrame(lobbyInfoLines.toArray(new String[0]));
		gameInfoBlock = gameInfoBG.merge(gameInfoBlock, AnchorPoint.CENTER, AnchorPoint.CENTER);
		// Merge the game info block into the lobby info frame (with some vertical offset)
		lobbyInfoFrame = lobbyInfoFrame.merge(gameInfoBlock, AnchorPoint.TOP, AnchorPoint.CENTER, 8, 0);

		// Build the lobby members block
		List<String> membersLines = new ArrayList<>();
		membersLines.add("");
		membersLines.add(ANSI.CYAN + "Lobby Members:" + ANSI.RESET);
		lobbyMembers.forEach(member ->
				membersLines.add(ANSI.GREEN + " - " + member + ANSI.RESET)
		);

		CLIFrame membersFrame = new CLIFrame(membersLines.toArray(new String[0]));
		// Merge lobby members block in the center of the lobby info frame
		lobbyInfoFrame = lobbyInfoFrame.merge(membersFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 4, 0);

		// Merge the lobby info frame into the screen border
		CLIFrame res = screenBorder.merge(lobbyInfoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

		// Tip for game leader (in red) at the bottom, if applicable
		if (getLastUpdate().isGameLeader()) {
			CLIFrame tip = new CLIFrame(new String[]{
					"",
					ANSI.BACKGROUND_GREEN + "Tip: Change the game settings with the command >settings" + ANSI.RESET
			});
			res = res.merge(tip, AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);
		}
		return res;
	}


}
