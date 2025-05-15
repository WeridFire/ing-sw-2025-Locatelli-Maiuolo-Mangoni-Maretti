package it.polimi.ingsw.controller.cp;

import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.controller.states.MenuState;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.CommandOptionsParser;

import java.rmi.RemoteException;
import java.util.*;

public class MenuCommandsProcessor extends PhaseCommandsProcessor {
    public MenuCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    private HashMap<String, String> lastCreateWithOptions = null;

    @Override
    public List<String> getAvailableCommands() {
        return List.of("refresh|Refresh the game list.",
                "join|Join an existing game.",
                "create|Create a new game.");
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {
        lastCreateWithOptions = null;

        switch (command) {
            case "" : return true;  // valid onVoid

            case "refresh" : return true;

            case "join" :
                if (args.length != 2) {
                    view.showWarning("Usage: join <uuid> <username>");
                    return false;
                }
                try {
                    UUID uuid = UUID.fromString(args[0]);
                } catch (IllegalArgumentException e) {
                    view.showError("Invalid UUID: [" + args[0] + "]. Please enter a valid game UUID.");
                    return false;
                }
                return true;

            case "create" :
                StringBuilder fullCommand = new StringBuilder(command);
                for (String arg : args) fullCommand.append(' ').append(arg);
                lastCreateWithOptions = CommandOptionsParser.parse(fullCommand.toString(), List.of(
                                new CommandOptionsParser.OptionFinder(Set.of("-c", "--color"),
                                        "color", null),
                                new CommandOptionsParser.OptionFinder(Set.of("-p", "--password"),
                                        "password", "")
                        ));
                String[] cmdArgs = CommandOptionsParser.getCommandArgs(lastCreateWithOptions);
                // check username presence
                if (cmdArgs.length != 1) {
                    view.showWarning("Usage: create <username> [-c | --color <"
                            + Arrays.toString(MainCabinTile.Color.values()) + ">] [-p | --password <password>]");
                    return false;
                }
                // check color validity
                String color = lastCreateWithOptions.get("color");
                if (color != null && !color.isEmpty()) {
                    try {
                        MainCabinTile.Color.valueOf(color);
                    } catch (IllegalArgumentException e) {
                        view.showWarning("Usage for optional parameter color is [-c | --color <"
                                + Arrays.toString(MainCabinTile.Color.values()) + ">]");
                        return false;
                    }
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
            case "create" -> server.createGame(client, CommandOptionsParser.getCommandArgs(lastCreateWithOptions)[0],
                    MainCabinTile.Color.valueOf(lastCreateWithOptions.get("color").toUpperCase()));
        }
    }
}
