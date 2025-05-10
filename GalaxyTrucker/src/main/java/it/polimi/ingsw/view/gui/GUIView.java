package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.cp.*;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.View;

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
        System.out.println("GUI function: onUpdate -> " + update.getClientUUID()
                + " | refresh: " + update.isRefreshRequired());
        // TODO: implement
    }

    @Override
    public void run() {
        System.out.println("GUI function: run");
        // TODO: implement
    }

    @Override
    public void onVoid() {
        System.out.println("GUI function: onVoid");
        onRefresh();
        // TODO: implement
    }

    @Override
    protected void _onRefresh() {
        System.out.println("GUI function: onRefresh");
        // TODO: implement
    }

    @Override
    public void onScreen(String screenName) {
        System.out.println("GUI function: onScreen -> " + screenName);
        onRefresh();
        // TODO: implement
    }

    @Override
    public void onHelp() {
        System.out.println("GUI function: onHelp");
        onRefresh();
        // TODO: implement
    }

    @Override
    public void showInfo(String title, String content) {
        System.out.println("GUI function: showInfo -> [" + title + "] >> " + content);
        onRefresh();
        // TODO: implement
    }

    @Override
    public void showWarning(String title, String content) {
        System.out.println("GUI function: showWarning -> [" + title + "] >> " + content);
        onRefresh();
        // TODO: implement
    }

    @Override
    public void showError(String title, String content) {
        System.out.println("GUI function: showError -> [" + title + "] >> " + content);
        onRefresh();
        // TODO: implement
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
