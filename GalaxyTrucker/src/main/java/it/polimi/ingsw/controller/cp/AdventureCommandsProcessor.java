package it.polimi.ingsw.controller.cp;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
import java.util.List;

public class AdventureCommandsProcessor extends PhaseCommandsProcessor {
    public AdventureCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    @Override
    public List<String> getAvailableCommands() {
        return List.of("endFlight|request to end the flight before the next adventure");
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {
        switch (command) {
            case "" : return true;  // valid onVoid

            case "endFlight" :
                Player player = CommonState.getPlayer();
                if (player.isEndedFlight()) {
                    view.showWarning("You have already ended the flight.");
                    return false;
                }
                return true;

            // refuses unavailable commands
            default: throw new CommandNotAllowedException(command, args);
        }
    }

    @Override
    protected void performCommand(String command, String[] args) throws RemoteException {
        switch (command) {
            case "" -> view.onRefresh();  // on simple enter refresh
            case "endFlight" -> {
                server.requestEndFlight(client, null);
                view.showInfo("The request to end the flight has been registered.\n" +
                        "You will end the flight right before the next Adventure.");
            }
        }
    }
}
