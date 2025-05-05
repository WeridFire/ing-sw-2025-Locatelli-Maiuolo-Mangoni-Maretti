package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class CLIScreenHandler {

	private final Deque<CLIScreen> currentScreens;
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
		currentScreens = new LinkedList<>();
		allScreens = new HashSet<>();
		allScreens.add(new MenuCLIScreen());  // menu
		allScreens.add(new LobbyCLIScreen());  // lobby
		allScreens.add(new AssembleCLIScreen());  // assemble
		allScreens.add(new AdventureCLIScreen());  // flight and adventure cards
		allScreens.add(new PIRCLIScreen());  // input
	}

	// END OF SINGLETON LOGIC

	/**
	 * Sets the last update received by the server, and refreshes the active screen.
	 * @param newUpdate the new update to manage
	 */
	public void setLastUpdate(ClientUpdate newUpdate){
		if (newUpdate == null) {
			throw new RuntimeException("Newly received update cannot be null.");
		}

		lastUpdate = newUpdate;

		// now forget about the current screen
		currentScreens.clear();

		// force activate screens which require it
		getAvailableScreens().stream()
				.filter(CLIScreen::isForceActivate)
				.max(Comparator.comparingInt(CLIScreen::getPriority))
				.ifPresent(forceActivate -> activateScreen(forceActivate.screenName));

		// If no screen is active: there is not a forcefully activated screen,
		// so look for all the available screens and get the one with the highest priority.
		// If no screen has been found, activate the default "Menu"
		if (getCurrentScreen() == null) {
			CLIScreen newScreen = getAvailableScreens().stream()
					.max(Comparator.comparingInt(CLIScreen::getPriority))
					.orElse(null);
			if (newScreen != null) {
				activateScreen(newScreen.screenName);
			} else {
				activateScreen("Menu");
			}
		}

		// now just refresh the current screen (if update requires it)
		if (newUpdate.isRefreshRequired()) {
			getCurrentScreen().refresh();
		}
	}

	/**
	 * @return the set of currently available and activable screens.
	 * This list does not include the currently enabled screen.
	 */
	private Set<CLIScreen> getAvailableScreens(){
		return allScreens.stream()
				.filter(cli -> cli.switchConditions() && cli != getCurrentScreen())
				.collect(Collectors.toSet());
	}

	/**
	 * Prints an informative message displaying the currently available and activable screens.
	 */
	protected CLIFrame popupAvailableScreens() {
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
		return frame;
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
				.filter((cli) -> Objects.equals(cli.screenName, screenName.toLowerCase()))
				.findFirst()
				.orElse(null);
		if (cliScreen != null) {
			currentScreens.addFirst(cliScreen);
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

			CLIScreen currentScreen = getCurrentScreen();
			if (currentScreen == null) continue;

			switch (cmd) {
				case "":
					//we close any "popup" window we are displaying.
					isShowingAvailableScreens = false;
					isShowingHelpScreen = false;
					// propagate to current screen management of empty message
					propagateProcessCommand(cmd, args);
					break;
				case "ping":
					gameClient.getServer().ping(gameClient.getClient());
					break;
				case "screen":
					if(args.length == 1){
						boolean success = activateScreen(args[0]);
						if (!success) {
							currentScreen.setScreenMessage("No screen found with name '" + args[0] +
									"'.\nPlease use one name in the provided list of available screens.");
						} else {
							currentScreen.refresh();
						}
						break;
					}
					else if (args.length > 1) {
						currentScreen.setScreenMessage("Usage: screen | screen <name>");
						break;
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
				case "cheat":
					if(args.length != 1){
						currentScreen.setScreenMessage("Usage: cheat <cheat name>");
						break;
					}
					gameClient.getServer().useCheat(gameClient.getClient(), args[0]);
					break;
				default:
					// If the command is not recognized as a global command, it lets the active screen process it.
					propagateProcessCommand(cmd, args);
			}
		}
	}

	/**
	 * The first active screen processes the specified command.
	 * If it throws a CommandNotAllowedException, it tries with the next active screen and so on, until
	 * no more screens are active, or
	 * the command is processed without raising a CommandNotAllowedException.
	 * @param cmd The command to process
	 * @param args Arguments of the command to process
	 * @throws RemoteException if any screen trying to process the command throws a RemoteException.
	 */
	private void propagateProcessCommand(String cmd, String[] args) throws RemoteException {
		// If more than one screen is present (case of non-forceActivated screens),
		// it propagates until a screen accepts it.
		String firstMessageNotAllowedException = null;
		for (CLIScreen processScreen : currentScreens) {
			try{
				// NOTE: processScreen always satisfies processScreen.switchConditions()
				// <===> processCommand precondition is satisfied
				processScreen.processCommand(cmd, args);
			} catch (CommandNotAllowedException e) {
				if (firstMessageNotAllowedException == null) {
					firstMessageNotAllowedException = e.getMessage();
				}
				continue;
			} catch(IllegalArgumentException e) {
				processScreen.setScreenMessage(e.getMessage());
			}
			// if here: the command has been processed
			firstMessageNotAllowedException = null;
			break;
		}
		// if the first exception message is not null, it means no screen was able to process the command
		if (firstMessageNotAllowedException != null) {
			getCurrentScreen().setScreenMessage(firstMessageNotAllowedException);
		}
	}

	protected GameClient getGameClient() {
		return gameClient;
	}

	public ClientUpdate getLastUpdate() {
		return lastUpdate;
	}

	protected CLIScreen getCurrentScreen(){
		return currentScreens.peekFirst();
	}
}
