package it.polimi.ingsw.controller.cp;

import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LobbyCommandsProcessor extends PhaseCommandsProcessor {
    public LobbyCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    @Override
    public List<String> getAvailableCommands() {
        List<String> availableCommands = new ArrayList<>(
                List.of("leave|Leave the current lobby.")
        );

        if (LobbyState.isGameLeader()) {
            availableCommands.add("settings|Change the game settings.");
        }

        return availableCommands;
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {
        switch (command) {
            case "": return true;  // valid onVoid

            case "settings":
                if(!LobbyState.isGameLeader()) {
                    view.showError("You must be the game leader to perform this command.");
                    return false;
                }
                boolean validSettingsCommand = args.length == 2;
                if (validSettingsCommand) {
                    switch (args[0].toLowerCase()) {
                        case "level":
                            try {
                                GameLevel.valueOf(args[1].toUpperCase());
                            } catch(IllegalArgumentException e) {
                                view.showWarning("Available levels: " + Arrays.toString(GameLevel.values()));
                                return false;
                            }
                            break;
                        case "minplayers", "requiredplayers":
                            Integer minPlayers = validateInteger(args[1], "minplayers");
                            if (minPlayers == null) return false;
                            if (minPlayers < 2 || minPlayers > 4) {
                                view.showWarning("Minimum number of players must be between 2 and 4.");
                                return false;
                            }
                            break;
                        default: validSettingsCommand = false; break;
                    }
                }
                if (!validSettingsCommand) {
                    view.showWarning("Usage: settings <level <level_name>> | <minplayers <2|3|4>>");
                    return false;
                }
                return true;

            case "leave": return true;

            // refuses unavailable commands
            default: throw new CommandNotAllowedException(command, args);
        }
    }

    @Override
    protected void performCommand(String command, String[] args) throws RemoteException {
        switch (command) {
            case "" -> view.onRefresh();  // on simple enter refresh
            case "settings" -> {
                GameLevel level = LobbyState.getGameLevel();
                int minPlayers = LobbyState.getRequiredPlayers();
                switch (args[0].toLowerCase()) {
                    case "level" -> level = GameLevel.valueOf(args[1].toUpperCase());
                    case "minplayers", "requiredplayers" -> minPlayers = Integer.parseInt(args[1]);
                }
                // if player specified already set settings, we don't bother with sending the update to the server.
                if (level == LobbyState.getGameLevel() && minPlayers == LobbyState.getRequiredPlayers()){
                    view.onRefresh();
                } else {
                    server.updateGameSettings(client, level, minPlayers);
                }
            }
            case "leave" -> server.quitGame(client);
        }
    }
}
