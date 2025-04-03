package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
import java.util.List;

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
		List<String> lobbyMembers = getLastUpdate().getCurrentGame().getPlayers().stream().map(Player::getUsername).toList();
		String lobbyID = getLastUpdate().getCurrentGame().getGameId().toString();
		String requiredPlayersAmount = String.valueOf(getLastUpdate().getCurrentGame().getRequiredPlayers());
		String flightLevel = String.valueOf(getLastUpdate().getCurrentGame().getLevel());
		String host = lobbyMembers.getFirst();
		String yourName = getLastUpdate().getCurrentGame().getPlayers().stream()
				.filter(p -> p.getConnectionUUID().equals(CLIScreenHandler.getInstance().getLastUpdate().getClientUUID()))
				.map(Player::getUsername)
				.findFirst()
				.orElse("Unknown");

		System.out.println(ANSI.ANSI_YELLOW + "\n======= LOBBY INFO =======" + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Lobby ID: " + ANSI.ANSI_CYAN + lobbyID + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Host: " + ANSI.ANSI_CYAN + host + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Your Name: " + ANSI.ANSI_CYAN + yourName + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Required Players: " + ANSI.ANSI_CYAN + requiredPlayersAmount + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_GREEN + "Flight Level: " + ANSI.ANSI_CYAN + flightLevel + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_YELLOW + "============================" + ANSI.ANSI_RESET);
		System.out.println(ANSI.ANSI_BLUE + "\nLobby Members:" + ANSI.ANSI_RESET);
		lobbyMembers.forEach(member -> System.out.println(ANSI.ANSI_CYAN + "- " + member + ANSI.ANSI_RESET));
	}


	@Override
	protected void processCommand(String command, String[] args) throws RemoteException {

	}

	@Override
	protected void printScreenSpecificCommands() {
		printCommands(screenName, null);
	}


}
