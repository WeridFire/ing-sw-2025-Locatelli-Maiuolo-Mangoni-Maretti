package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.cp.*;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.gui.utils.AlertUtils;

import java.util.Deque;
import java.util.LinkedList;

// TODO: create GUI view

public class GUIView extends View {

    private MenuCommandsProcessor cpMenu;  // menu
    private LobbyCommandsProcessor cpLobby;  // lobby
    private AssembleCommandsProcessor cpAssemble;  // assemble
    private AdventureCommandsProcessor cpAdventure;  // flight and adventure cards
    private PIRCommandsProcessor cpPIR;  // input

    public GUIView(GameClient gameClient){
        super(gameClient);
    }

    @Override
    public void _init() {
        cpMenu = new MenuCommandsProcessor(gameClient);
        cpLobby = new LobbyCommandsProcessor(gameClient);
        cpAssemble = new AssembleCommandsProcessor(gameClient);
        cpAdventure = new AdventureCommandsProcessor(gameClient);
        cpPIR = new PIRCommandsProcessor(gameClient);
    }

    @Override
    public void _onUpdate(ClientUpdate update) {
        /*
        System.out.println("GUI function: onUpdate -> " + update.getClientUUID()
                + " | refresh: " + update.isRefreshRequired());
        */
        // TODO: implement (e.g. transform any active YerNoChoice pir into confirmation Alert, and more...)
    }

    @Override
    public void run() {
        // System.out.println("GUI function: run");
    }

    @Override
    public void onVoid() {
        // System.out.println("GUI function: onVoid");
        onRefresh();
    }

    @Override
    protected void _onRefresh() {
        // System.out.println("GUI function: onRefresh");
    }

    @Override
    public void onScreen(String screenName) {
        System.out.println("GUI function: onScreen -> " + screenName);
        // TODO: implement
        onRefresh();
    }

    @Override
    public void onHelp() {
        System.out.println("GUI function: onHelp");
        // TODO: implement
        onRefresh();
    }

    @Override
    public void showInfo(String title, String content) {
        // System.out.println("GUI function: showInfo -> [" + title + "] >> " + content);
        AlertUtils.showInfo(title, content);
        onRefresh();
    }

    @Override
    public void showWarning(String title, String content) {
        // System.out.println("GUI function: showWarning -> [" + title + "] >> " + content);
        AlertUtils.showWarning(title, content);
        onRefresh();
    }

    @Override
    public void showError(String title, String content) {
        // System.out.println("GUI function: showError -> [" + title + "] >> " + content);
        AlertUtils.showError(title, content);
        onRefresh();
    }

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
