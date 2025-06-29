package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.commandsProcessors.*;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.gui.utils.AlertUtils;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Represents the graphical user interface view of the game.
 */
public class GUIView extends View {

    private MenuCommandsProcessor cpMenu;  // menu
    private LobbyCommandsProcessor cpLobby;  // lobby
    private AssembleCommandsProcessor cpAssemble;  // assemble
    private AdventureCommandsProcessor cpAdventure;  // flight and adventure cards
    private PIRCommandsProcessor cpPIR;  // input

    /**
     * Initializes the view.
     */
    @Override
    public void _init() {
        cpMenu = new MenuCommandsProcessor(gameClient);
        cpLobby = new LobbyCommandsProcessor(gameClient);
        cpAssemble = new AssembleCommandsProcessor(gameClient);
        cpAdventure = new AdventureCommandsProcessor(gameClient);
        cpPIR = new PIRCommandsProcessor(gameClient);
    }

    /**
     * Called when the view receives an update from the server.
     * @param update the update message
     */
    @Override
    public void _onUpdate(ClientUpdate update) {
    }

    /**
     * Runs the view.
     */
    @Override
    public void run() {
        // System.out.println("GUI function: run");
    }

    /**
     * Called when a void event occurs.
     */
    @Override
    public void onVoid() {
        // System.out.println("GUI function: onVoid");
        onRefresh();
    }

    /**
     * Called when the view needs to be refreshed.
     */
    @Override
    protected void _onRefresh() {
        // System.out.println("GUI function: onRefresh");
    }

    /**
     * Called when the screen changes.
     * @param screenName the name of the new screen
     */
    @Override
    public void onScreen(String screenName) {
        System.out.println("GUI function: onScreen -> " + screenName);
        onRefresh();
    }

    /**
     * Called when the help command is triggered.
     */
    @Override
    public void onHelp() {
        System.out.println("GUI function: onHelp");
        onRefresh();
    }

    /**
     * Shows an information message.
     * @param title the title of the message
     * @param content the content of the message
     */
    @Override
    public void showInfo(String title, String content) {
        // System.out.println("GUI function: showInfo -> [" + title + "] >> " + content);
        AlertUtils.showInfo(title, content);
        onRefresh();
    }

    /**
     * Shows a warning message.
     * @param title the title of the message
     * @param content the content of the message
     */
    @Override
    public void showWarning(String title, String content) {
        // System.out.println("GUI function: showWarning -> [" + title + "] >> " + content);
        AlertUtils.showWarning(title, content);
        onRefresh();
    }

    /**
     * Shows an error message.
     * @param title the title of the message
     * @param content the content of the message
     */
    @Override
    public void showError(String title, String content) {
        // System.out.println("GUI function: showError -> [" + title + "] >> " + content);
        AlertUtils.showError(title, content);
        onRefresh();
    }

    /**
     * Returns the command processors for the current game state.
     * @return a deque of command processors
     */
    @Override
    public Deque<ICommandsProcessor> getCommandsProcessors() {
        Deque<ICommandsProcessor> processors = new LinkedList<>();

        if (CommonState.getGameData() == null) {
            processors.push(new MenuCommandsProcessor(gameClient));
        }

        if (CommonState.isCurrentPhase(GamePhaseType.LOBBY)) {
            processors.push(new LobbyCommandsProcessor(gameClient));
        }

        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)) {
            processors.push(new AssembleCommandsProcessor(gameClient));
        }

        if (CommonState.isCurrentPhase(GamePhaseType.ADVENTURE)) {
            processors.push(new AdventureCommandsProcessor(gameClient));
        }

        if (PIRState.isPIRActive()) {
            processors.push(new PIRCommandsProcessor(gameClient));
        }

        return processors;
    }
}
