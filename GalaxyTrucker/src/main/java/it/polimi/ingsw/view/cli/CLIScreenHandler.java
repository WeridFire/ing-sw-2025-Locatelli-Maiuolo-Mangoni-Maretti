package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class CLIScreenHandler {

	private CLIScreen currentScreen;
	private final Set<CLIScreen> allScreens;
	private final GameClient gameClient;
	private ClientUpdate lastUpdate;
	protected boolean isShowingHelpScreen = false;
	protected boolean isShowingAvailableScreens = false;

	//SINGLETON LOGIC

	private static CLIScreenHandler instance;

	public static CLIScreenHandler getInstance() {
		if (instance == null) {
			throw new AssertionError("First initialize the screen handler.");
		}
		return instance;
	}

	public synchronized static CLIScreenHandler init(GameClient gameClient) {
		if (instance != null)
		{
			throw new AssertionError("You already initialized this.");
		}
		instance = new CLIScreenHandler(gameClient);
		return instance;
	}

	public CLIScreenHandler(GameClient gameClient){
		this.gameClient = gameClient;
		allScreens = new HashSet<>();
		allScreens.add(new MenuCLIScreen()); //menu
		allScreens.add(new LobbyCLIScreen()); //lobby
		allScreens.add(new AssembleCLIScreen()); //assemble
		allScreens.add(new PIRCLIScreen()); //input
	}

	// END OF SINGLETON LOGIC

	/**
	 * Sets the last update received by the server, and refreshes the active screen.
	 * @param newUpdate
	 */
	public void setLastUpdate(ClientUpdate newUpdate){
		if(newUpdate == null){
			throw new RuntimeException("Newly received update cannot be null.");
		}
		this.lastUpdate = newUpdate;
		if(getCurrentScreen() == null){
			activateScreen("Menu");
		}
		CLIScreen forceActivate = getAvailableScreens()
									.stream()
									.filter(CLIScreen::isForceActivate)
									.findFirst()
									.orElse(null);
		if(forceActivate != null){
			activateScreen(forceActivate.screenName);
			return;
		}

		if(getCurrentScreen().switchConditions()){
			getCurrentScreen().refresh(); //In case the screen conditions are still met, we just refresh the current screen
		}else{
			//If the previous screen is no longer activable, and there is not a forced screen to activate,
			//Look for all the available screens, and get the one with the highest priority (usually the PIRs).
			CLIScreen newScreen = getAvailableScreens().stream().sorted((c1, c2) -> {
				return Integer.compare(c2.getPriority(), c1.getPriority());
			}).findFirst().orElse(null);
			if(newScreen != null){
				activateScreen(newScreen.screenName);
			}else{
				activateScreen("Menu");
			}

		}

	}

	/**
	 * @return the list of currently available and activable screens. This list does not include the currently enabled screen.
	 */
	private Set<CLIScreen> getAvailableScreens(){
		return allScreens.stream().filter(cli -> cli.switchConditions() && cli != currentScreen).collect(Collectors.toSet());
	}

	/**
	 * Prints an informative message displaying the currently available and activable screens.
	 */
	protected void printAvailableScreens() {

		CLIScreen.clear();
		Set<CLIScreen> screens = getAvailableScreens();
		CLIFrame frame = CLIScreen.getScreenFrame(12, 40, ANSI.BACKGROUND_BLACK);
		frame = frame.merge(new CLIFrame(ANSI.RED + "AVAILABLE SCREENS"), AnchorPoint.TOP, AnchorPoint.CENTER, 1, 0);
		if (screens.isEmpty()) {
			frame = frame.merge(new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + "No Screens Available"), AnchorPoint.CENTER, AnchorPoint.CENTER, -1, 0);
		}else{
			List<String> screensList = new ArrayList<>(screens.size());
			screens.forEach(cli ->
					screensList.add(ANSI.BACKGROUND_WHITE + ANSI.BLACK + " > " + ANSI.WHITE + cli.screenName)
			);
			CLIFrame s = new CLIFrame(screensList.toArray(new String[0]));
			frame = frame.merge(s, AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 1, 1);
		}
		frame = frame.merge(new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + "Enter to close"),
										AnchorPoint.BOTTOM, AnchorPoint.CENTER, -2, 0);
		CLIFrame currentScr = getCurrentScreen().getCLIRepresentation();
		currentScr = currentScr.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
		System.out.println(currentScr);
	}

	/**
	 * Activates a Screen given a name, if available. If the screenName is not available does nothing.
	 * A screen is available if the conditions in its method switchConditions is true, and therefore means
	 * it can be switched. Also the screen must not be already active.
	 * @param screenName the name of the screen to switch to. Case insensitive
	 * @return if the activation succeeds.
	 */
	private boolean activateScreen(String screenName){
		CLIScreen cliScreen = getAvailableScreens()
				.stream()
				.filter((cli) -> 	Objects.equals(cli.screenName, screenName.toLowerCase()))
				.findFirst()
				.orElse(null);
		if(cliScreen != null){
			this.currentScreen = cliScreen;
			return true;
		}
		return false;
	}

	/**
	 * Runs the CLI on the client, allowing player to input commands to send to the server. These commands will be
	 * executed automatically on the desired client. Based on the active screen of the player, there will be differently
	 * available commands. This function will handle routing to the active screen the input command, or handles it directly
	 * if it is recognized as a global command (example ping & screen)
	 * @throws RemoteException
	 */
	public void runCli() throws RemoteException {
		Scanner scan = new Scanner(System.in);
		while (true) {
			String command = scan.nextLine().trim();  // Reading the full line for commands with arguments
			List<String> commandParts = new ArrayList<>(Arrays.asList(command.split(" ")));
			String cmd = commandParts.removeFirst();
			String[] args = commandParts.toArray(new String[0]);
			switch (cmd) {
				case "":
					//we close any "popup" window we are displaying.
					isShowingAvailableScreens = false;
					isShowingHelpScreen = false;

					currentScreen.refresh();
					break;
				case "ping":
					gameClient.getServer().ping(gameClient.getClient());
					break;
				case "screen":
					if(args.length == 1){
						boolean success = activateScreen(args[0]);
						if(success){
							break;
						} else {
							currentScreen.setScreenMessage("No screen found with name '" + args[0] +
									"'. Please use one name in the provided list of available screens.");
							// TODO: adjust double refresh
						}
					}
					else if (args.length > 1) {
						currentScreen.setScreenMessage("Usage: screen | screen <name>");
					}
					isShowingAvailableScreens = true;
					currentScreen.refresh();
					isShowingAvailableScreens = false;
					break;
				case "help":
					isShowingHelpScreen = true;
					currentScreen.refresh();
					isShowingHelpScreen = false;
					break;
				case "debug":
					currentScreen.setScreenMessage("The current game state was saved to update.json");
					ClientUpdate.saveDebugUpdate(getLastUpdate());
					break;
				default:
					//if the command is not recognized as a global command, it lets the active screen process it.
					if (currentScreen != null){
						try{
							// NOTE: currentScreen always satisfies currentScreen.switchConditions()
							// <===> processCommand precondition is satisfied
							currentScreen.processCommand(cmd, args);
						}catch(IllegalArgumentException e){
							currentScreen.setScreenMessage(e.getMessage());
							break;
						}

					}
			}
		}
	}

	protected GameClient getGameClient() {
		return gameClient;
	}

	protected ClientUpdate getLastUpdate() {
		return lastUpdate;
	}

	protected CLIScreen getCurrentScreen(){
		return currentScreen;
	}
}
