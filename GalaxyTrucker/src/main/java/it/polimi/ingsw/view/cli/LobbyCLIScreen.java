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
		super("lobby", true);
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
	protected void processCommand(String command, String[] args) throws RemoteException, IllegalArgumentException {
		switch(command){
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
	protected void printScreenSpecificCommands() {
		if(getLastUpdate().isGameLeader()){
			printCommands(screenName, "settings|Change the game settings.", "leave|Leave the current lobby.");
		}else{
			printCommands(screenName, "leave|Leave the current lobby.");
		}
	}


	@Override
	public CLIFrame getCLIRepresentation() {
		CLIFrame screenBorder = getScreenFrame(24, 100);
		CLIFrame lobbyInfoFrame = getScreenFrame(18, 80);
		CLIFrame title = new CLIFrame(new String[]{"LOBBY INFO"});
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

		// Game info block
		List<String> lobbyInfoLines = new ArrayList<>();
		lobbyInfoLines.add("Lobby ID: " + lobbyID);
		lobbyInfoLines.add("Leader: " + host);
		lobbyInfoLines.add("Your Name: " + yourName);
		lobbyInfoLines.add("Required Players: " + requiredPlayers);

		// Flight levels display
		String levelsDisplay = Arrays.stream(GameLevel.values())
				.map(level -> level == currentLevel
						? "[*" + level.toString() + "*]"
						: level.toString()
				)
				.collect(Collectors.joining(" | "));
		lobbyInfoLines.add("Flight Level: " + levelsDisplay);

		CLIFrame gameInfoBlock = new CLIFrame(lobbyInfoLines.toArray(new String[0]));
		lobbyInfoFrame = lobbyInfoFrame.merge(gameInfoBlock, AnchorPoint.TOP, AnchorPoint.CENTER, 5, 0);

		// Lobby members
		List<String> membersLines = new ArrayList<>();
		membersLines.add("");
		membersLines.add("Lobby Members:");
		lobbyMembers.forEach(member -> membersLines.add(" - " + member));
		CLIFrame membersFrame = new CLIFrame(membersLines.toArray(new String[0]));
		lobbyInfoFrame = lobbyInfoFrame.merge(membersFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 2, 0);


		CLIFrame res = screenBorder.merge(lobbyInfoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);

		// Tip for game leader
		if (getLastUpdate().isGameLeader()) {
			CLIFrame tip = new CLIFrame(new String[]{
					"",
					"Tip: Change the game settings with the command >settings"
			});
			res = res.merge(tip, AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);
		}
		return res;
	}

}
