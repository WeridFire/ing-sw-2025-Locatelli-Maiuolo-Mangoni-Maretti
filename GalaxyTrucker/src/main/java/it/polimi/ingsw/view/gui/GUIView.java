package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.cp.*;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.IView;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

// TODO: create GUI view

public class GUIView implements IView {

    private final GameClient gameClient;
    private final ViewCommandsProcessor commandsProcessor;
    private final List<Consumer<ClientUpdate>> listenersOnUpdate;

    private boolean isInitialized = false;

    private MenuCommandsProcessor cpMenu;  // menu
    private LobbyCommandsProcessor cpLobby;  // lobby
    private AssembleCommandsProcessor cpAssemble;  // assemble
    private AdventureCommandsProcessor cpAdventure;  // flight and adventure cards
    private PIRCommandsProcessor cpPIR;  // input

    public GUIView(GameClient gameClient){
        this.gameClient = gameClient;
        commandsProcessor = new ViewCommandsProcessor(this);
        listenersOnUpdate = new ArrayList<>();
    }

    @Override
    public void init() {
        if (isInitialized) return;

        cpMenu = new MenuCommandsProcessor(gameClient);
        cpLobby = new LobbyCommandsProcessor(gameClient);
        cpAssemble = new AssembleCommandsProcessor(gameClient);
        cpAdventure = new AdventureCommandsProcessor(gameClient);
        cpPIR = new PIRCommandsProcessor(gameClient);

        isInitialized = true;
    }

    @Override
    public void onUpdate(ClientUpdate update) {
        System.out.println("GUI function: onUpdate -> " + update.getClientUUID());
        // TODO: implement

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

    @Override
    public void run() {
        System.out.println("GUI function: run");
        // TODO: implement
    }

    @Override
    public void onVoid() {
        System.out.println("GUI function: onVoid");
        // TODO: implement
    }

    @Override
    public void onRefresh() {
        System.out.println("GUI function: onRefresh");
        // TODO: implement
    }

    @Override
    public void onPing() {
        System.out.println("GUI function: onPing");
        // TODO: implement
    }

    @Override
    public void onScreen(String screenName) {
        System.out.println("GUI function: onScreen -> " + screenName);
        // TODO: implement
    }

    @Override
    public void onHelp() {
        System.out.println("GUI function: onHelp");
        // TODO: implement
    }

    @Override
    public void onDebug() {
        System.out.println("GUI function: onDebug");
        // TODO: implement
    }

    @Override
    public void onCheat(String cheatName) {
        System.out.println("GUI function: onCheat -> " + cheatName);
        // TODO: implement
    }

    @Override
    public void showInfo(String title, String content) {
        System.out.println("GUI function: showInfo -> [" + title + "] >> " + content);
        // TODO: implement
    }

    @Override
    public void showWarning(String title, String content) {
        System.out.println("GUI function: showWarning -> [" + title + "] >> " + content);
        // TODO: implement
    }

    @Override
    public void showError(String title, String content) {
        System.out.println("GUI function: showError -> [" + title + "] >> " + content);
        // TODO: implement
    }

    @Override
    public ViewCommandsProcessor getCommandsProcessor() {
        return commandsProcessor;
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
