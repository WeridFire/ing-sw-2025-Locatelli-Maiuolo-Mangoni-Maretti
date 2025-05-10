package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.cp.ICommandsProcessor;
import it.polimi.ingsw.controller.cp.ViewCommandsProcessor;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.IView;
import it.polimi.ingsw.controller.states.State;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CLIView implements IView {

	private final GameClient gameClient;
	private final ViewCommandsProcessor commandsProcessor;
	private final List<Consumer<ClientUpdate>> listenersOnUpdate;

	private final Deque<CLIScreen> currentScreens;
	private final Set<CLIScreen> allScreens;

	private boolean isInitialized = false;

	protected boolean isShowingHelpScreen = false;
	protected boolean isShowingAvailableScreens = false;

	public CLIView(GameClient gameClient){
		this.gameClient = gameClient;
		commandsProcessor = new ViewCommandsProcessor(this);
		listenersOnUpdate = new ArrayList<>();

		currentScreens = new LinkedList<>();
		allScreens = new HashSet<>();
	}

	@Override
	public void init() {
		if (isInitialized) return;

		allScreens.add(new MenuCLIScreen(gameClient));  // menu
		allScreens.add(new LobbyCLIScreen(gameClient));  // lobby
		allScreens.add(new AssembleCLIScreen(gameClient));  // assemble
		allScreens.add(new AdventureCLIScreen(gameClient));  // flight and adventure cards
		allScreens.add(new PIRCLIScreen(gameClient));  // input

		for (CLIScreen screen : allScreens) {
			screen.setParentView(this);
		}

		isInitialized = true;
	}

	@Override
	public synchronized void onUpdate(ClientUpdate update) {
		if (update == null) {
			throw new RuntimeException("Newly received update cannot be null.");
		}

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
		if (update.isRefreshRequired()) {
			onRefresh();
		}

		// at the end: callback all the registered listeners
		synchronized (listenersOnUpdate) {
			listenersOnUpdate.forEach(listener -> listener.accept(update));
			listenersOnUpdate.clear();
		}
	}

	@Override
	public void registerOnUpdateListener(Consumer<ClientUpdate> onUpdate) {
		synchronized (listenersOnUpdate) {
			listenersOnUpdate.add(onUpdate);
		}
	}

	/**
	 * Runs the CLI on the client, allowing player to input commands to send to the server. These commands will be
	 * executed automatically on the desired client. Based on the active screen of the player, there will be differently
	 * available commands. This function will handle routing to the active screen the input command, or handles it directly
	 * if it is recognized as a global command (example ping & screen)
	 */
	@Override
	public void run() {
		Scanner scan = new Scanner(System.in);
		while (true) {
			String command = scan.nextLine().trim();  // Reading the full line for commands with arguments

			List<String> commandParts = new ArrayList<>(Arrays.asList(command.split(" ")));
			String cmd = commandParts.removeFirst();
			String[] args = commandParts.toArray(new String[0]);

			if (getCurrentScreen() != null) {
				commandsProcessor.processCommand(cmd, args);
			}
		}
	}

	@Override
	public void onVoid() {
		// we close any "popup" window we are displaying.
		isShowingAvailableScreens = false;
		isShowingHelpScreen = false;
		// propagate to current screen management of empty message
        try {
            commandsProcessor.propagateProcessCommand("", null);
        } catch (CommandNotAllowedException e) {
            // no problem, do nothing
        }
    }

	@Override
	public synchronized void onRefresh() {
		getCurrentScreen().refresh();
	}

	@Override
	public void onPing() {
        try {
            gameClient.getServer().ping(gameClient.getClient());
        } catch (RemoteException e) {
            showError(e.getMessage());
        }
    }

	@Override
	public void onScreen(String screenName) {
		if (screenName == null) {
			isShowingAvailableScreens = true;
			onRefresh();
			isShowingAvailableScreens = false;
			return;
		}

		boolean success = activateScreen(screenName);
		if (!success) {
			showWarning("No screen found with name '" + screenName +
					"'.\nPlease use one name in the provided list of available screens.");
		} else {
			onRefresh();
		}
	}

	@Override
	public void onHelp() {
		isShowingHelpScreen = true;
		onRefresh();
		isShowingHelpScreen = false;
	}

	@Override
	public void onDebug() {
		ClientUpdate.saveDebugUpdate(State.getInstance().getLastUpdate());
		showInfo("The current game state was saved to update.json");
	}

	@Override
	public void onCheat(String cheatName) {
        try {
            gameClient.getServer().useCheat(gameClient.getClient(), cheatName);
        } catch (RemoteException e) {
            showError(e.getMessage());
        }
    }

	@Override
	public void showInfo(String title, String content) {
		getCurrentScreen().setScreenMessage(ANSI.WHITE + title + " >> " + content);
	}

	@Override
	public void showWarning(String title, String content) {
		getCurrentScreen().setScreenMessage(ANSI.YELLOW + title + " >> " + content);
	}

	@Override
	public void showError(String title, String content) {
		getCurrentScreen().setScreenMessage(ANSI.RED + title + " >> " + content);
	}

	@Override
	public ViewCommandsProcessor getCommandsProcessor() {
		return commandsProcessor;
	}

	@Override
	public Deque<ICommandsProcessor> getCommandsProcessors() {
		return currentScreens.stream()
				.map(CLIScreen::getCommandsProcessor)
				.collect(Collectors.toCollection(LinkedList::new));
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

	public IClient getClient() {
		return gameClient.getClient();
	}

	protected CLIScreen getCurrentScreen(){
		return currentScreens.peekFirst();
	}
}
