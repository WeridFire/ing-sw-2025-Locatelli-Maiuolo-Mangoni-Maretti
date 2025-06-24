package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.commandsProcessors.*;
import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.view.gui.GUIView;

import java.util.*;

public class ViewMock extends GUIView {

    private final String mockName;
    private State mockState;
    private UUID clientUUID;

    private final List<String> infos = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public ViewMock(String mockName) {
        super();
        this.mockName = mockName;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }

    @Override
    public void _init() {
        super._init();
        mockState = gameClient.getLinkedState();
    }

    @Override
    public void _onUpdate(ClientUpdate update) {
        clientUUID  = update.getClientUUID();
        if(isPIRActive(update.getCurrentGame())){
            PIR pir = update.getCurrentGame().getPIRHandler().getPlayerPIR(update.getClientPlayer());
            String message = "";
            if(pir.getPIRType() == PIRType.DELAY){
                message = ((PIRDelay) pir).getMessage();
            }else if (pir.getPIRType() == PIRType.CHOICE){
                message = ((PIRMultipleChoice) pir).getChoiceMessage();
            }
            System.out.println(mockName + ">> received PIR: "
                    + pir.getPIRType() + "|" + message);
        }
    }

    @Override
    public void onVoid() {
        System.out.println(mockName + " >> void");
        onRefresh();
    }

    @Override
    protected void _onRefresh() {
    }

    @Override
    public void onScreen(String screenName) {
        System.out.println(mockName + " >> screen -> " + screenName);
        onRefresh();
    }

    @Override
    public void onHelp() {
        System.out.println(mockName + " >> help");
        onRefresh();
    }

    @Override
    public void run() {
        System.out.println(mockName + " >> run");
    }

    @Override
    public void showInfo(String title, String content) {
        System.out.println(mockName + " INFO >> " + title + " - " + content);
        infos.add(title + "_" + content);
        onRefresh();
    }
    public List<String> getInfos() {
        return infos;
    }

    @Override
    public void showWarning(String title, String content) {
        System.out.println(mockName + " WARNING >> " + title + " - " + content);
        warnings.add(title + "_" + content);
        onRefresh();
    }
    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public void showError(String title, String content) {
        System.out.println(mockName + " ERROR >> " + title + " - " + content);
        errors.add(title + "_" + content);
        onRefresh();
    }
    public List<String> getErrors() {
        return errors;
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