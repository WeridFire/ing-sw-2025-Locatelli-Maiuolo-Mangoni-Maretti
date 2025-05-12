package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.cp.ICommandsProcessor;
import it.polimi.ingsw.controller.cp.ViewCommandsProcessor;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.RemoteException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class View implements IView {

    protected GameClient gameClient;

    private final ViewCommandsProcessor commandsProcessor;

    private final Set<Runnable> listenersOnRefreshOnce;
    private final Set<Runnable> listenersOnRefreshPermanent;

    private final Set<Consumer<ClientUpdate>> listenersOnUpdate;

    protected boolean isInitialized;

    public View() {
        commandsProcessor = new ViewCommandsProcessor(this);

        listenersOnRefreshOnce = new HashSet<>();
        listenersOnRefreshPermanent = new HashSet<>();

        listenersOnUpdate = new HashSet<>();

        isInitialized = false;
    }

    /**
     * Initializes the internal components or data structures of the view.
     * <p>
     * This method is intended to be called once before the view is used, typically after construction
     * but before invoking {@link #run()} or processing any updates.
     * Its purpose is to link the related game client (useful to avoid circular dependence from it) and
     * to set up screens, handlers, or other necessary elements required for the view to function correctly.
     * <p>
     * Calling this method multiple times is safe: the implementation guards against redundant initialization.
     * However, there is no need to call this method more than once.
     *
     * @implNote To define the initialization logic, override the {@link #_init()} method.
     */
    public final void init(GameClient gameClient) {
        if (isInitialized) return;
        this.gameClient = gameClient;
        _init();
        isInitialized = true;
    }

    /**
     * Contains the actual implementation of the initialization logic for this component.
     * This method is called only once by {@link #init(GameClient)} and should not be invoked directly.
     *
     * @see #init(GameClient)
     */
    protected abstract void _init();

    /**
     * Registers a listener that will be notified every time a new {@code ClientUpdate} is received by the view.
     * <p>
     * This method allows external components to hook into the flow and react to changes from the controller (updates).
     *
     * @param onUpdate a {@link Consumer} function that will be invoked for every new {@code ClientUpdate}.
     *                 Should not be {@code null}, otherwise it's ignored.
     */
    public void registerOnUpdateListener(Consumer<ClientUpdate> onUpdate) {
        if (onUpdate == null) return;
        synchronized (listenersOnUpdate) {
            listenersOnUpdate.add(onUpdate);
        }
    }

    /**
     * Unregisters a listener that has been added with {@link #registerOnUpdateListener(Consumer)}
     * @param onUpdate the reference to the {@link Consumer} function that was previously added,
     *                 and now it's wanted to be removed. If not found -> simply ignore.
     */
    public void unregisterOnUpdateListener(Consumer<ClientUpdate> onUpdate) {
        if (onUpdate == null) return;
        synchronized (listenersOnUpdate) {
            listenersOnUpdate.remove(onUpdate);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if {@code update} is {@code null}.
     */
    @Override
    public final synchronized void onUpdate(ClientUpdate update) {
        if (update == null) {
            throw new NullPointerException("Newly received update cannot be null.");
        }
        _onUpdate(update);  // perform main body

        // trigger listeners
        synchronized (listenersOnUpdate) {
            listenersOnUpdate.forEach(l -> l.accept(update));
        }

        // now just refresh the current screen (if update requires it)
        if (update.isRefreshRequired()) {
            onRefresh();
        }
    }
    /**
     * Contains the core logic to be executed whenever a new {@link ClientUpdate} is received by the view.
     * <p>
     * This method is invoked by the final {@link #onUpdate(ClientUpdate)} method, which handles null {@code update}.
     * @param update the update received from the client-side game state. Guaranteed to be non-null.
     */
    protected abstract void _onUpdate(ClientUpdate update);

    /**
     * Called when an empty command or "void" input is given.
     * <p>
     * Useful for refreshing state or dismissing temporary overlays/popups.
     */
    public abstract void onVoid();

    /**
     * Registers a runnable that will be run and removed whenever the first refresh done by the view.
     * <p>
     * This method allows external components to hook into the flow and react to changes in the view,
     * so both client-side and from controller (a new game state).
     *
     * @param onRefresh a {@link Runnable} function that will be invoked with the first refresh done by the view.
     *                 Should not be {@code null}, otherwise it's ignored.
     */
    public void doOnceOnRefresh(Runnable onRefresh) {
        if (onRefresh == null) return;
        synchronized (listenersOnRefreshOnce) {
            listenersOnRefreshOnce.add(onRefresh);
        }
    }

    /**
     * Registers a listener that will be notified every time refresh is done by the view.
     * <p>
     * This method allows external components to hook into the flow and react to changes in the view,
     * so both client-side and from controller (a new game state).
     *
     * @param onRefresh a {@link Runnable} function that will be invoked at each refresh.
     *                 Should not be {@code null}, otherwise it's ignored.
     */
    public void registerPermanentOnRefreshListener(Runnable onRefresh) {
        if (onRefresh == null) return;
        synchronized (listenersOnRefreshPermanent) {
            listenersOnRefreshPermanent.add(onRefresh);
        }
    }

    /**
     * Unregisters a listener that has been added with {@link #registerPermanentOnRefreshListener(Runnable)}
     * @param onRefresh the reference to the {@link Runnable} function that was previously added,
     *                 and now it's wanted to be removed. If not found -> simply ignore.
     */
    public void unregisterPermanentOnRefreshListenerPermanent(Runnable onRefresh) {
        if (onRefresh == null) return;
        synchronized (listenersOnRefreshPermanent) {
            listenersOnRefreshPermanent.remove(onRefresh);
        }
    }

    /**
     * Notifies all registered listeners of a new refresh.
     * <p>
     * It first notifies all one-time listeners and clears them, then proceeds to notify all permanent listeners.
     * Synchronization is used to ensure thread-safe access to listener collections.
     */
    private void notifyOnRefresh() {
        // apply update to temporary listeners and remove them
        synchronized (listenersOnRefreshOnce) {
            listenersOnRefreshOnce.forEach(Runnable::run);
            listenersOnRefreshOnce.clear();
        }
        // apply update to permanent listeners
        synchronized (listenersOnRefreshPermanent) {
            listenersOnRefreshPermanent.forEach(Runnable::run);
        }
    }

    /**
     * Requests the view to refresh its current state.
     * <p>
     * This typically re-renders the screen or updates visible information.
     */
    public final void onRefresh() {
        _onRefresh();
        notifyOnRefresh();
    }
    /**
     * Defines the specific logic to be executed when the view is requested to refresh its state.
     * <p>
     * This method is called by {@link #onRefresh()}, which also notifies any registered listeners.
     * Override this method to define how the current screen or view should visually or logically update itself
     * based on the latest available state.
     */
    protected abstract void _onRefresh();

    /**
     * Executes a ping to the server from the view.
     * <p>
     * Can be used to check connectivity or keep the connection alive.
     */
    public void onPing() {
        try {
            gameClient.getServer().ping(gameClient.getClient());
        } catch (RemoteException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Switches the active screen to the one identified by the given name.
     * <p>
     * If the screen name is {@code null}, the view should show a list of available screens.
     *
     * @param screenName the name of the screen to switch to; case-insensitive, or {@code null} to display options.
     */
    public abstract void onScreen(String screenName);

    /**
     * Displays the help screen.
     * <p>
     * Typically, shows a list of available commands or user guidance.
     */
    public abstract void onHelp();

    /**
     * Triggers debug behavior to save the last update
     */
    public void onDebug() {
        ClientUpdate.saveDebugUpdate(CommonState.getLastUpdate());
        showInfo("The current game state was saved to update.json");
    }

    /**
     * Sends a cheat command to the server (if enabled and permitted).
     *
     * @param cheatName the name of the cheat to activate.
     */
    public void onCheat(String cheatName) {
        try {
            gameClient.getServer().useCheat(gameClient.getClient(), cheatName);
        } catch (RemoteException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Returns the primary command processor responsible for handling global commands in this view context.
     * <p>
     * This processor manages top-level commands and acts as the entry point for interpreting user input
     * before potentially delegating to screen-specific logic via {@link #getCommandsProcessors()}.
     *
     * @return the {@link ViewCommandsProcessor} instance associated with this view.
     */
    public ViewCommandsProcessor getCommandsProcessor() {
        return commandsProcessor;
    }

    /**
     * Returns the stack of command processors associated with this view.
     * <p>
     * Commands are routed through this stack in order to find a handler.
     * Typically, each active screen contributes its own command processor.
     *
     * @return a deque of {@link ICommandsProcessor} instances, from top (most important screen) to bottom.
     */
    public abstract Deque<ICommandsProcessor> getCommandsProcessors();
}
