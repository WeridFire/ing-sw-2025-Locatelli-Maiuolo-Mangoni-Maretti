package it.polimi.ingsw.controller.commandsProcessors;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.EasterEgg;
import it.polimi.ingsw.util.GameLevelStandards;
import it.polimi.ingsw.view.cli.ANSI;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AssembleCommandsProcessor extends PhaseCommandsProcessor {
    public AssembleCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    private boolean validateIsAssemblyEnded() {
        if (AssembleState.isEndedAssembly()) {
            view.showWarning("You've already finished assembling your majestic ship!\n" +
                    "Wait for the other players to complete their surely more mediocre work.");
            return true;
        } else {
            return false;
        }
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
        TileSkeleton tileInHand = AssembleState.getTileInHand();

        if(!Objects.equals(AssembleState.getPlayer().getSpectating(), AssembleState.getPlayer().getUsername())){
            availableCommands.add("stop-spectating|Go back to your shipboard.");
        }
        availableCommands.add("spectate <" + getSpectatablePlayersUsernames() +">|Spectate another player's shipboard.");


        if (!AssembleState.isEndedAssembly()) {

            // note: last timerflip only if assemble phase ended for this player
            availableCommands.add("timerflip|Flips the hourglass of the game.");

            boolean hasCardGroupInHand = AssembleState.getCardGroupInHand().isPresent();
            boolean areThereCardGroups = !AssembleState.listOfAvailableCardGroups().isEmpty();

            if (tileInHand == null && !hasCardGroupInHand) {
                // can take tile
                availableCommands.add("draw|Draws a tile from the covered tiles.");
                availableCommands.add("pick <id>|Pick in hand the tile with ID <id>.");
                // or group of cards
                if (areThereCardGroups && AssembleState.isPlacedOneTile()) {
                    availableCommands.add("showcg <id>|Pick and show the card group with ID <id>.");
                }
                // or finish assembly
                availableCommands.add("finish|Force end of assembly.");
            }
            else if (tileInHand != null) {
                // can only act with the tile in hand
                availableCommands.add("discard|Discard the tile you have in hand.");
                availableCommands.add("reserve|Reserve the tile you have in hand.");
                availableCommands.add("rotate <direction>|Rotate the tile you have in hand.");
                availableCommands.add("place <row> <column>|Place the tile from your hand onto your shipboard.");
            }
            else /* hasCardGroupInHand */ {
                // can only act with the card group in hand
                availableCommands.add("hidecg|Set the card group from your hand back to the shared board.");
            }

        }

        return availableCommands;
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {

        final String occupiedHand = AssembleState.getOccupiedHandMessage();
        final TileSkeleton tileInHand = AssembleState.getTileInHand();

        switch (command) {
            case "" : return true;  // valid onVoid

            case "timerflip" : return true;  // TODO: client side checks for timerflip

            case "draw" :
                if (validateIsAssemblyEnded()) return false;
                if (occupiedHand != null) {
                    view.showError("You can't draw a tile with a " + occupiedHand + " in hand.");
                    return false;
                }
                return true;

            case "discard" :
                if (validateIsAssemblyEnded()) return false;
                // TODO: client side checks for discard
                return true;

            case "reserve" :
                if (validateIsAssemblyEnded()) return false;
                // TODO: client side checks for reserve
                return true;

            case "rotate" :  // only client side -> always return false, to avoid propagating to the server
                if (validateIsAssemblyEnded()) return false;
                // Note: this command overrides the private field, not needing confirmation from the server
                if (args.length == 1) {
                    Rotation rotation = Rotation.fromString(args[0]);
                    if (rotation == Rotation.NONE) {
                        view.showWarning("Rotation '" + args[0] + "' not recognized.\nAllowed rotations are:"
                                + "<left|l> | <right|r> | <opposite|o>");
                        return false;
                    }
                    if (tileInHand == null) {
                        view.showWarning("You have no tile in your hand.");
                        return false;
                    }
                    try {
                        tileInHand.rotateTile(rotation);
                        view.onRefresh();  // only because it's client side command
                        return false;
                    } catch (FixedTileException e) {
                        view.showError("The tile in your hand has already been placed.");  // should never happen
                        return false;
                    }
                }
                else {
                    view.showWarning("Usage: rotate <direction>");
                    return false;
                }

            case "place" :
                if (validateIsAssemblyEnded()) return false;
                if (args.length == 2) {
                    Coordinates coordinates;
                    try {
                        int row = Integer.parseInt(args[0]);
                        int column = Integer.parseInt(args[1]);
                        coordinates = new Coordinates(row, column);
                    } catch (NumberFormatException e) {
                        view.showError("Invalid coordinates. Please enter valid coordinates: " +
                                "integer number for both row and column.");
                        return false;
                    }
                    // TODO: client side checks for place, e.g. coordinates represent valid empty place on the shipboard
                    if (tileInHand == null) {
                        view.showWarning("You have no tile in your hand.");
                        return false;
                    }
                }
                else {
                    view.showWarning("Usage: place <row> <column>");
                    return false;
                }
                return true;

            case "pick" :
                if (validateIsAssemblyEnded()) return false;
                if (occupiedHand != null) {
                    view.showWarning("You can't pick a tile with a " + occupiedHand + " in hand.");
                    return false;
                }
                if (args.length == 1) {
                    Integer id = validateInteger(args[0], "ID");
                    if (id == null) return false;
                    // TODO: client side checks for pick, e.g. valid id
                }
                else {
                    view.showWarning("Usage: pick <id>");
                    return false;
                }
                return true;

            case "finish" :
                if (validateIsAssemblyEnded()) return false;
                if (args.length <= 1) {
                    if (AssembleState.getPlayer().getShipBoard() == null) {
                        view.showError("You don't have a shipboard.");
                        return false;
                    }
                    // this finish is done by the player, not forced by the end of time
                    // -> check for no tiles / cards groups in hand
                    if (occupiedHand != null) {
                        view.showWarning("You can't finish with a " + occupiedHand + " in hand.");
                        return false;
                    }
                    if (args.length == 1) {  // preferred position specified
                        Integer preferredPosition = validateInteger(args[0], "preferred position");
                        if (preferredPosition == null) return false;
                        // TODO: client side checks for finish, e.g. valid preferredPosition
                    }
                }
                else {
                    view.showWarning("Usage: finish [<starting position>: default->first free]");
                    return false;
                }
                return true;

            case "showcg" :
                if (validateIsAssemblyEnded()) return false;
                if (!AssembleState.isPlacedOneTile()) {
                    view.showWarning("You have to place a tile before taking a group of cards.");
                    return false;
                }
                if (occupiedHand != null) {
                    view.showWarning("You can't take a group of cards with a " + occupiedHand + " in hand.");
                    return false;
                }
                if (args.length == 1) {
                    Integer id = validateInteger(args[0], "ID");
                    if (id == null) return false;
                    // TODO: client side checks for showcg, e.g. valid id
                }
                else {
                    view.showWarning("Usage: showcg <id>");
                    return false;
                }
                return true;

            case "hidecg" :
                if (validateIsAssemblyEnded()) return false;
                if (AssembleState.getCardGroupInHand().isEmpty()) {
                    view.showWarning("You have no group of cards in your hand.");
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
                view.showWarning("Could not find a player with username: " + args[0]);
                return false;

            case "easteregg":  // only client side -> always return false, to avoid propagating to the server
                if (AssembleState.isEndedAssembly() && args.length >= 1) {
                    StringBuilder name = new StringBuilder(args[0]);
                    for (int i = 1; i < args.length; i++) name.append(' ').append(args[i]);
                    view.showInfo(ANSI.BACKGROUND_BLACK + ANSI.GREEN +
                            EasterEgg.getRandomJoke(name.toString()) + ANSI.RESET);
                } else {  // act like this command does not exist
                    view.showWarning("Invalid command. Use help to view available commands.");
                }
                return false;

            // refuses unavailable commands
            default: throw new CommandNotAllowedException(command, args);
        }

    }

    @Override
    protected void performCommand(String command, String[] args) throws RemoteException {
        switch (command) {
            case "" -> view.onRefresh();  // on simple enter refresh
            case "timerflip" -> server.flipHourglass(client);
            case "draw" -> server.drawTile(client);
            case "discard" -> server.discardTile(client);
            case "reserve" -> server.reserveTile(client);
            case "place" -> server.placeTile(client,
                    new Coordinates(Integer.parseInt(args[0]), Integer.parseInt(args[1])),
                    AssembleState.getTileInHand().getAppliedRotation());
            case "pick" -> server.pickTile(client, Integer.parseInt(args[0]));
            case "finish" -> server.finishAssembling(client,
                    (args.length == 0) ? null : Integer.parseInt(args[0]));
            case "showcg" -> server.showCardGroup(client, Integer.parseInt(args[0]));
            case "hidecg" -> server.hideCardGroup(client);
            case "stop-spectating" -> server.spectatePlayerShipboard(client, AssembleState.getPlayer().getUsername());
            case "spectate" -> server.spectatePlayerShipboard(client, args[0]);
        }
    }
}
