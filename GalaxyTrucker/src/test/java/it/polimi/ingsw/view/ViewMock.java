package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.cp.*;
import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.view.gui.GUIView;

import java.util.Deque;
import java.util.LinkedList;

public class ViewMock extends GUIView {

    private final String mockName;
    private State mockState;

    public ViewMock(String mockName) {
        super();
        this.mockName = mockName;
    }

    @Override
    public void _init() {
        super._init();
        mockState = gameClient.getLinkedState();
    }

    @Override
    public void _onUpdate(ClientUpdate update) {
        System.out.println(mockName + " >> update");
        /*
        System.out.println("Mock View function: onUpdate [" + update + "] for client " + gameClient + " -> "
                + mockName + " [" + update.getClientUUID() + "]"
                + " | refresh: " + update.isRefreshRequired());

         */
    }

    @Override
    public void onVoid() {

    }

    @Override
    protected void _onRefresh() {
        System.out.println(mockName + " >> refresh");
    }

    @Override
    public void onScreen(String screenName) {

    }

    @Override
    public void onHelp() {

    }

    @Override
    public void run() {
        System.out.println(mockName + " >> run");
    }

    @Override
    public void showInfo(String title, String content) {

    }

    @Override
    public void showWarning(String title, String content) {

    }

    @Override
    public void showError(String title, String content) {

    }

    private Player getPlayer() {
        return mockState.getLastUpdate().getClientPlayer();
    }

    private boolean isPIRActive(GameData gameData) {
        return gameData != null &&
                gameData.getPIRHandler() != null &&
                gameData.getPIRHandler().getPlayerPIR(getPlayer()) != null;
    }

    private boolean isCurrentPhase(GameData gameData, GamePhaseType gamePhaseType) {
        return gameData != null && gameData.getCurrentGamePhaseType() == gamePhaseType;
    }

    @Override
    public Deque<ICommandsProcessor> getCommandsProcessors() {
        Deque<ICommandsProcessor> processors = new LinkedList<>();

        GameData gameData = mockState.getLastUpdate().getCurrentGame();

        if (gameData == null) {
            processors.push(new MenuCommandsProcessor(gameClient));
        }

        if (isCurrentPhase(gameData, GamePhaseType.LOBBY)) {
            processors.push(new LobbyCommandsProcessor(gameClient));
        }

        if (isCurrentPhase(gameData, GamePhaseType.ASSEMBLE)) {
            processors.push(new AssembleCommandsProcessor(gameClient));
        }

        if (isCurrentPhase(gameData, GamePhaseType.ADVENTURE)) {
            processors.push(new AdventureCommandsProcessor(gameClient));
        }

        if (isPIRActive(gameData)) {
            processors.push(new PIRCommandsProcessor(gameClient));
        }

        return processors;
    }
}