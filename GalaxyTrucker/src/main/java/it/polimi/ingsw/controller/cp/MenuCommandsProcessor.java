package it.polimi.ingsw.controller.cp;

import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public class MenuCommandsProcessor extends PhaseCommandsProcessor {
    public MenuCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    @Override
    public List<String> getAvailableCommands() {
        return List.of("refresh|Refresh the game list.",
                "join|Join an existing game.",
                "create|Create a new game.");
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {
        switch (command) {
            case "" : return true;  // valid onVoid

            case "refresh" : return true;

            case "join" :
                if (args.length != 2) {
                    view.showWarning("Usage: join <uuid> <username>");
                    return false;
                }
                return true;

            case "create" :
                if (args.length != 1) {
                    view.showWarning("Usage: create <username>");
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
            case "refresh" -> server.ping(client);
            case "join" -> server.joinGame(client,
                            UUID.fromString(args[0]),
                            args[1]);
            case "create" -> server.createGame(client, args[0]);
        }
    }
}
