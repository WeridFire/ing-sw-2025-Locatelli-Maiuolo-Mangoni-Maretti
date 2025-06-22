package it.polimi.ingsw.controller.commandsProcessors;

import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
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

    @Override
    public List<String> getAvailableCommands() {
        return List.of("refresh|Refresh the game list.",
                "join|Join an existing game.",
                "create|Create a new game.",
                "resume|Resume a paused game.");
    }

    private MainCabinTile.Color lastOptionsColor = null;

    private boolean validateLastOptionsColor(MainCabinTile.Color[] availableColors, String lastOptionsColorString) {
        String helper;
        if (availableColors == null || availableColors.length == 0) {
            helper = null;
            availableColors = new MainCabinTile.Color[]{ null };
        } else {
            helper = Arrays.toString(availableColors);
        }

        if (lastOptionsColorString == null) {  // set default as first available color
            lastOptionsColor = availableColors[0];
        }
        else if (lastOptionsColorString.isEmpty()) {  // "-c" but not specified color after
            view.showWarning("Usage for optional parameter 'color' is [-c | --color <color>]."
                    + "\nPlease use a valid color" + (helper != null ? ", among the following: " + helper : "") + ".");
            return false;
        }
        else {
            lastOptionsColorString = lastOptionsColorString.toUpperCase();
            try {
                lastOptionsColor = MainCabinTile.Color.valueOf(lastOptionsColorString);
                if (!Set.of(availableColors).contains(lastOptionsColor)) {
                    throw new IllegalArgumentException();  // caught below
                }
            } catch (IllegalArgumentException e) {
                view.showWarning("Invalid color '" + lastOptionsColorString + "'.\nPlease use a valid color"
                        + (helper != null ? ", among the following: " + helper : "") + ".");
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {
        HashMap<String, String> cmdWithOptionColor = CommandOptionsParser.parse(command, args, List.of(
                new CommandOptionsParser.OptionFinder(Set.of("-c", "--color"),
                        "color", null)
        ));
        int cmdWithOptionColorArgsLength = CommandOptionsParser.getCommandArgs(cmdWithOptionColor).length;
        String lastOptionsColorString = cmdWithOptionColor.get("color");

        switch (command) {
            case "" : return true;  // valid onVoid

            case "refresh" : return true;

            case "join" :
                // check uuid and username presence
                if (cmdWithOptionColorArgsLength != 2) {
                    view.showWarning("Usage: join <uuid> <username> [-c | --color <color>]");
                    return false;
                }
                // check uuid validity
                UUID uuid;
                try {
                    uuid = UUID.fromString(args[0]);
                } catch (IllegalArgumentException e) {
                    view.showError("Invalid UUID: [" + args[0] + "]. Please enter a valid game UUID.");
                    return false;
                }
                // check color validity
                return validateLastOptionsColor(MenuState.getAvailableColorsForGame(uuid), lastOptionsColorString);

            case "resume":
                if (cmdWithOptionColorArgsLength != 1) {
                    view.showWarning("Usage: resume <uuid>");
                    return false;
                }
                UUID resumeUuid;
                try {
                    uuid = UUID.fromString(args[0]);
                } catch (IllegalArgumentException e) {
                    view.showError("Invalid UUID: [" + args[0] + "]. Please enter a valid game UUID.");
                    return false;
                }
                return true;

            case "create" :
                // check username presence
                if (cmdWithOptionColorArgsLength != 1) {
                    view.showWarning("Usage: create <username> [-c | --color <color>]");
                    return false;
                }
                // check color validity
                return validateLastOptionsColor(MainCabinTile.Color.values(), lastOptionsColorString);

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
                    args[1],
                    lastOptionsColor);
            case "create" -> server.createGame(client,
                    args[0],
                    lastOptionsColor);
            case "resume" -> server.resumeGame(client, UUID.fromString(args[0]));
        }
    }
}
