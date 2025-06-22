package it.polimi.ingsw.controller.commandsProcessors;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AdventureCommandsProcessor extends PhaseCommandsProcessor {
    public AdventureCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    private String getSpectatablePlayersUsernames(){
        Set<String> playerUsernames = AssembleState.getGameData()
                .getPlayersInFlight()
                .stream()
                .map(Player::getUsername)
                .filter((u) -> !u.equals(AssembleState.getPlayer().getUsername()))
                .collect(Collectors.toSet());
        return String.join("|", playerUsernames);
    }

    @Override
    public List<String> getAvailableCommands() {
        List<String> availableCommands = new ArrayList<>();
        availableCommands.add("endFlight|request to end the flight before the next adventure");

        if(!Objects.equals(AssembleState.getPlayer().getSpectating(), AssembleState.getPlayer().getUsername())){
            availableCommands.add("stop-spectating|Go back to your shipboard.");
        }
        availableCommands.add("spectate <" + getSpectatablePlayersUsernames() +"> |Spectate another player's shipboard.");


        return availableCommands;

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
            case "stop-spectating":
                return true;
            case "spectate":
                if(args.length != 1){
                    view.showWarning("Specify one of the following usernames: <" + getSpectatablePlayersUsernames() + ">");
                    return false;
                }
                if(AssembleState.getGameData().getPlayersInFlight()
                        .stream()
                        .map(Player::getUsername)
                        .anyMatch(username -> username.equals(args[0]))){
                    return true;
                }
                view.showWarning("Could not find a player with username " + args[0]);
                return false;
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
            case "stop-spectating" -> server.spectatePlayerShipboard(client, AssembleState.getPlayer().getUsername());
            case "spectate" -> server.spectatePlayerShipboard(client, args[0]);

        }
    }
}
