package it.polimi.ingsw.view;

import it.polimi.ingsw.network.messages.ClientUpdate;

/**
 * Represents a generic View in the client-side architecture of the game.
 * <p>
 * A View observes game state updates, provides a way to interact with the user (e.g., via TUI or GUI),
 * and communicates with the controller layer.
 * This interface abstracts the core responsibilities of a user interface, enabling different implementations.
 */
public interface IView {

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
}
