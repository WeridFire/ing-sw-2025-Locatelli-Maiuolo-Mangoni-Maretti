package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.cp.ICommandsProcessor;
import it.polimi.ingsw.network.messages.ClientUpdate;

import java.util.Deque;

/**
 * Represents a generic View in the client-side architecture of the game.
 * <p>
 * A View observes game state updates, provides a way to interact with the user (e.g., via TUI or GUI),
 * and communicates with the controller layer.
 * This interface abstracts the core responsibilities of a user interface, enabling different implementations.
 */
public interface IView {

    /**
     * Initializes the internal components or data structures of the view.
     * <p>
     * This method is intended to be called once before the view is used, typically after construction
     * but before invoking {@link #run()} or processing any updates. Its purpose is to set up screens,
     * handlers, or other necessary elements required for the view to function correctly.
     * <p>
     * Calling this method multiple times should be safe; implementations are expected to guard against
     * redundant initialization. However, there is no need to invoke this method more than once.
     *
     * @implSpec Implementations should ensure that repeated invocations do not perform duplicate work.
     */
    void init();

    /**
     * Called whenever the game state is updated.
     * <p>
     * The implementation should handle screen transitions, refreshes, or any other reaction
     * to changes in the observable game state.
     *
     * @param update the most recent update to the game state; never {@code null}.
     */
    void onUpdate(ClientUpdate update);

    /**
     * Starts the execution of the view logic.
     * <p>
     * Runs the main input loop, listening for user commands and interacting with the controller accordingly.
     */
    void run();

    /**
     * Called when an empty command or "void" input is given.
     * <p>
     * Useful for refreshing state or dismissing temporary overlays/popups.
     */
    void onVoid();

    /**
     * Requests the view to refresh its current state.
     * <p>
     * This typically re-renders the screen or updates visible information.
     */
    void onRefresh();

    /**
     * Executes a ping to the server from the view.
     * <p>
     * Can be used to check connectivity or keep the connection alive.
     */
    void onPing();

    /**
     * Switches the active screen to the one identified by the given name.
     * <p>
     * If the screen name is {@code null}, the view should show a list of available screens.
     *
     * @param screenName the name of the screen to switch to; case-insensitive, or {@code null} to display options.
     */
    void onScreen(String screenName);

    /**
     * Displays the help screen.
     * <p>
     * Typically, shows a list of available commands or user guidance.
     */
    void onHelp();

    /**
     * Triggers debug behavior for developers or advanced users.
     * <p>
     * For example, this may dump the current game state to a file.
     */
    void onDebug();

    /**
     * Sends a cheat command to the server (if enabled and permitted).
     *
     * @param cheatName the name of the cheat to activate.
     */
    void onCheat(String cheatName);

    /**
     * Displays an informational message to the user with a title and detailed content.
     * <p>
     * The implementation should clearly distinguish this type of message (e.g., via color or format).
     *
     * @param title a short, descriptive title for the message
     * @param content the detailed content/body of the info to display
     */
    void showInfo(String title, String content);

    /**
     * Displays an informational message to the user.
     *
     * @param info the message to display.
     */
    default void showInfo(String info) {
        showInfo("Info", info);
    }

    /**
     * Displays a warning message to the user with a title and detailed content.
     * <p>
     * The implementation should clearly distinguish this type of message (e.g., via color or format).
     *
     * @param title a short, descriptive title for the message
     * @param content the detailed content/body of the warning to display
     */
    void showWarning(String title, String content);

    /**
     * Displays a warning message to the user.
     *
     * @param warning the message to display.
     */
    default void showWarning(String warning) {
        showWarning("Warning", warning);
    }

    /**
     * Displays an error message to the user with a title and detailed content.
     * <p>
     * The implementation should clearly distinguish this type of message (e.g., via color or format).
     *
     * @param title a short, descriptive title for the message
     * @param content the detailed content/body of the error to display
     */
    void showError(String title, String content);

    /**
     * Displays an error message to the user.
     *
     * @param error the message to display.
     */
    default void showError(String error) {
        showError("Error", error);
    }

    /**
     * Returns the stack of command processors associated with this view.
     * <p>
     * Commands are routed through this stack in order to find a handler.
     * Typically, each active screen contributes its own command processor.
     *
     * @return a deque of {@link ICommandsProcessor} instances, from top (most important screen) to bottom.
     */
    Deque<ICommandsProcessor> getCommandsProcessors();
}
