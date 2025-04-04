package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
		printCommands(screenName, "settings|Change the game settings.", "leave|Leave the current lobby.");
	}


}
