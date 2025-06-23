package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.model.shipboard.integrity.IShipIntegrityListener;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.IView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the subject in the observer pattern for gfx (IView are observers)
 */
public class State {
    // SINGLETON LOGIC

    private static State instance;

    public synchronized static State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }

    /**
     * Replaces the current singleton instance with a custom {@link State} instance.
     * <p>
     * <strong>This method forcefully overrides the internal state singleton and should be used
     * <em>only</em> for testing purposes.</strong>
     * Improper use in production code may lead to inconsistent or invalid application state,
     * breaking the expected lifecycle of the singleton.
     *
     * @param state the new {@link State} instance to set as the singleton.
     *              May be {@code null} to clear the instance entirely.
     */
    public synchronized static void overrideInstance(State state) {
        instance = state;
    }

    // END OF SINGLETON LOGIC

    private final List<IView> viewsToUpdate;
    private ClientUpdate lastUpdate;

    public State() {
        viewsToUpdate = new ArrayList<>();
    }

    /**
     * Registers a {@link IView} to update each time a new {@link ClientUpdate} is received by this {@link State}.
     *
     * @param view the view to be attached; must not be {@code null}.
     * @throws NullPointerException if {@code view} is {@code null}.
     */
    public void attachView(IView view) {
        if (view == null) {
            throw new NullPointerException("It's not possible to attach a null view!");
        }
        synchronized (viewsToUpdate) {
            viewsToUpdate.add(view);
        }
    }

    /**
     * Unregisters a previously attached {@link IShipIntegrityListener}.
     *
     * @param view the view to be detached.
     * @return {@code true} if the view was successfully removed; {@code false} if it was not registered.
     */
    public boolean detachView(IView view) {
        synchronized (viewsToUpdate) {
            return viewsToUpdate.remove(view);
        }
    }

    /**
     * Sets the last update received by the server,
     * then perform {@link IView#onUpdate(ClientUpdate)} for each attached view.
     *
     * @param update the new update to manage. Can not be {@code null}.
     * @throws NullPointerException if {@code update} is {@code null}.
     */
    public final void setLastUpdate(ClientUpdate update) {
        if (update == null) {
            throw new NullPointerException("Newly received update cannot be null!");
        }
        lastUpdate = update;

        synchronized (viewsToUpdate) {
            for (IView view : viewsToUpdate) {
                view.onUpdate(lastUpdate);
            }
        }
    }

    /**
     * @return the most recent client update, from which to retrieve all the info about the current game state.
     */
    public final ClientUpdate getLastUpdate() {
        return lastUpdate;
    }
}
