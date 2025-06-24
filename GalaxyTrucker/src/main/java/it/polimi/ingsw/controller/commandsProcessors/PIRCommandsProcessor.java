package it.polimi.ingsw.controller.commandsProcessors;

import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.PIRActivateTiles;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.model.playerInput.PIRs.PIRRemoveLoadables;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PIRCommandsProcessor extends PhaseCommandsProcessor {
    public PIRCommandsProcessor(GameClient gameClient) {
        super(gameClient);
    }

    @Override
    public List<String> getAvailableCommands() {
        List<String> availableCommands = new ArrayList<>();
        if (!PIRState.isPIRActive()) {
            return availableCommands;
        }

        switch (PIRState.getActivePIRType()) {
            case CHOICE -> availableCommands.add("choose <option_number>|Choose an option with the corresponding ID.");
            case ACTIVATE_TILE -> availableCommands.add("activate (row,col), (row,col), ...|Choose the coordinates of tiles to activate.");
            case ADD_CARGO -> {
                availableCommands.add("allocate (row,col) <LoadableType> <amount>|Allocate cargo to a specific coordinate.");
                availableCommands.add("confirm|Submit the current cargo allocation to the server.");
            }
            case REMOVE_CARGO -> {
                availableCommands.add("remove (row,col) <LoadableType> <amount>|Mark cargo at a specific coordinate for removal.");
                availableCommands.add("confirm|Submit the current cargo removal to the server.");
            }
            case null, default -> { }  // should never happen
        }

        // Add common commands available for all PIR types
        availableCommands.add("endTurn|End your turn forcefully.");

        return availableCommands;
    }

    private boolean validateNonActivePIRType(PIRType pirType) {
        PIRType activePIRType = PIRState.getActivePIRType();
        if (activePIRType != pirType){
            view.showWarning("This command is not available for a PIR of type " + activePIRType);
            return true;
        } else {
            return false;
        }
    }

    private String formatOptionsWithIndices(String[] options) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < options.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append("[").append(i).append("] ").append(options[i]);
        }
        return result.toString();
    }

    private boolean validateChooseCommand(String[] args) {
        if (validateNonActivePIRType(PIRType.CHOICE)) return false;

        PIRMultipleChoice pir = (PIRMultipleChoice) PIRState.getActivePIR();
        if (pir == null) return false;
        if (args.length != 1) {
            view.showWarning("Invalid argument. Specify the ID of the choice you'd like to make.");
            return false;
        }

        try {
            int choice = Integer.parseInt(args[0]);
            if(choice < 0 || choice >= pir.getPossibleOptions().length){
                view.showWarning("Invalid integer. The integer should be one of the following options: " +
                        formatOptionsWithIndices(pir.getPossibleOptions()));
                return false;
            }
        } catch(NumberFormatException e) {
            view.showWarning("Invalid integer. The integer should be one of the following options: " +
                    formatOptionsWithIndices(pir.getPossibleOptions()));
            return false;
        }

        return true;
    }

    private boolean validateActivateCommand(String[] args) {
        if (validateNonActivePIRType(PIRType.ACTIVATE_TILE)) return false;

        PIRActivateTiles activatePir = (PIRActivateTiles) PIRState.getActivePIR();
        if (activatePir == null) return false;
        Set<Coordinates> highlightMask = activatePir.getHighlightMask();

        if (args.length == 0) {
            view.showWarning("Invalid input. Please specify coordinates in the format: (row,col), (row,col), ...");
            return false;
        }

        // Join all args to handle cases where spaces might be present between coordinates
        String fullInput = String.join(" ", args);

        // simulate parsing the coordinates
        Matcher matcher = Pattern.compile("\\((\\d+),(\\d+)\\)").matcher(fullInput);
        boolean foundAny = false;
        while (matcher.find()) {
            foundAny = true;
            try {
                int row = Integer.parseInt(matcher.group(1));
                int col = Integer.parseInt(matcher.group(2));
                Coordinates coord = new Coordinates(row, col);
                // Check if coordinate is in the highlight mask
                if (!highlightMask.contains(coord)) {
                    view.showWarning("Coordinate " + coord + " is not a valid selection. It's not in the highlight mask.");
                    return false;
                }
            } catch (NumberFormatException e) {
                view.showWarning("Invalid coordinate format. Please use integers for x and y values.");
                return false;
            }
        }

        if (!foundAny) {
            view.showWarning("No valid coordinates provided. Please specify coordinates from the highlight mask.");
            return false;
        }

        return true;
    }
    private void executeActivateCommand(String[] args) throws RemoteException {
        Set<Coordinates> selectedCoordinates = new HashSet<>();
        Matcher matcher = Pattern.compile("\\((\\d+),(\\d+)\\)").matcher(String.join(" ", args));
        while (matcher.find()) {
            selectedCoordinates.add(new Coordinates(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            ));
        }
        server.pirActivateTiles(client, selectedCoordinates);
    }

    private boolean validateAllocateCommand(String[] args) {
        if (validateNonActivePIRType(PIRType.ADD_CARGO)) return false;

        PIRAddLoadables pirAdd = (PIRAddLoadables) PIRState.getActivePIR();
        if (pirAdd == null) return false;
        Set<Coordinates> pirHighlightMask = pirAdd.getHighlightMask();
        List<LoadableType> floatingLoadables = pirAdd.getFloatingLoadables();

        if (args.length != 3) {
            view.showWarning("Invalid arguments. Usage: allocate (row,col) <LoadableType> <amount>");
            return false;
        }

        Coordinates coord = parseCoordinates(args[0], pirHighlightMask);
        if (coord == null) return false;

        // Parse LoadableType
        LoadableType loadableType = parseLoadableType(args[1]);
        if (loadableType == null) return false;

        // Parse amount
        int amount = parseAmount(args[2]);
        if(amount <= 0) return false;

        // Check if requested loadables are available in floating loadables
        int availableCount = countLoadableType(floatingLoadables, loadableType);

        if(availableCount < amount) {
            view.showWarning("Not enough " + loadableType + " available. Available: " + availableCount + ", Requested: " + amount);
            return false;
        }

        // Add to local cargo
        addToLocalCargo(coord, loadableType, amount);

        // Check if all loadables have been allocated
        int remaining = floatingLoadables.size() - countTotalAllocated();

        if (remaining > 0) {
            view.showInfo("Added " + amount + " " + loadableType + " to " + coord + ". " +
                    remaining + " loadables remaining to allocate. Use >confirm to confirm the choice.");
            return false;
        }

        return true;
    }
    private void executeAllocateCommand() throws RemoteException {
        Map<Coordinates, List<LoadableType>> localCargo = PIRState.getLocalCargo();
        server.pirAllocateLoadables(client, localCargo);
        view.showInfo("All requested items marked for allocation. Confirming.");
        localCargo.clear();  // Reset after sending to server
    }

    private boolean validateConfirmCommand() {
        if (validateNonActivePIRType(PIRType.ADD_CARGO)) return false;
        if (validateNonActivePIRType(PIRType.REMOVE_CARGO)) return false;

        // Check if localCargo is empty
        if (PIRState.getLocalCargo().isEmpty()) {
            view.showError("Nothing to confirm. Please allocate items first using the 'allocate' command.");
            return false;
        }

        return true;
    }

    private void executeConfirmCommand() throws RemoteException {
        Map<Coordinates, List<LoadableType>> localCargo = PIRState.getLocalCargo();
        // Submit to the server based on PIR type
        PIRType activePIRType = PIRState.getActivePIRType();
        if (activePIRType == PIRType.ADD_CARGO) {
            server.pirAllocateLoadables(client, localCargo);
            view.showInfo("Loadables allocation confirmed and sent to server.");
        }
        else {  // PIRType.REMOVE_CARGO
            server.pirRemoveLoadables(client, localCargo);
            view.showInfo("Loadables removal confirmed and sent to server.");
        }
        // Clear the local cargo after submission
        localCargo.clear();
    }

    private boolean validateRemoveCommand(String[] args) {
        if (validateNonActivePIRType(PIRType.REMOVE_CARGO)) return false;

        PIRRemoveLoadables pirRemove = (PIRRemoveLoadables) PIRState.getActivePIR();
        if (pirRemove == null) return false;
        Set<Coordinates> removeHighlightMask = pirRemove.getHighlightMask();
        Set<LoadableType> allowedCargo = pirRemove.getAllowedCargo();
        int amountToRemove = pirRemove.getAmountToRemove();

        if (args.length != 3) {
            view.showWarning("Invalid arguments. Usage: remove (row,col) <LoadableType> <amount>");
            return false;
        }

        Coordinates removeCoord = parseCoordinates(args[0], removeHighlightMask);
        if (removeCoord == null) return false;

        // Parse LoadableType
        LoadableType removeLoadableType = parseLoadableType(args[1]);
        if(removeLoadableType == null) return false;

        // Check if loadable type is allowed to be removed
        if(!allowedCargo.contains(removeLoadableType)){
            view.showWarning("LoadableType " + removeLoadableType +
                    " is not allowed to be removed. Allowed types: " + allowedCargo);
            return false;
        }

        // Parse amount
        int removeAmount = parseAmount(args[2]);
        if(removeAmount <= 0) return false;

        // Calculate current total items scheduled for removal
        int currentTotalRemoval = countTotalAllocated();

        // Check if adding this removal would exceed the amount to remove
        if(currentTotalRemoval + removeAmount > amountToRemove) {
            view.showWarning("Cannot remove " + removeAmount + " more items. You can only remove " +
                    (amountToRemove - currentTotalRemoval) + " more items.");
            return false;
        }

        // Add to local cargo for removal
        addToLocalCargo(removeCoord, removeLoadableType, removeAmount);

        view.showInfo("Marked " + removeAmount + " " + removeLoadableType + " at coordinate " + removeCoord + " for removal.");

        // Check if we've reached the total amount to remove
        currentTotalRemoval += removeAmount;
        int remaining = amountToRemove - currentTotalRemoval;

        if (remaining > 0) {
            view.showInfo("Marked " + removeAmount + " " + removeLoadableType + " at " + removeCoord + " for removal. " +
                    remaining + " more items need to be selected for removal. Use >confirm to confirm the choice.");
            return false;
        }

        return true;
    }

    private void executeRemoveCommand() throws RemoteException {
        Map<Coordinates, List<LoadableType>> localCargo = PIRState.getLocalCargo();
        server.pirRemoveLoadables(client, localCargo);
        view.showInfo("All requested items marked for removal. Confirming.");
        localCargo.clear();  // Reset after sending to server
    }

    private void executeEndTurnCommand() throws RemoteException {
        server.pirForceEndTurn(client);
        PIRState.getLocalCargo().clear();
    }

    // Helper methods for parsing and validation
    private Coordinates parseCoordinates(String coordString, Set<Coordinates> highlightMask) {
        Pattern coordPattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
        Matcher coordMatcher = coordPattern.matcher(coordString);

        if(!coordMatcher.matches()){
            view.showWarning("Invalid coordinate format. Use format: (row,col)");
            return null;
        }

        int x = Integer.parseInt(coordMatcher.group(1));
        int y = Integer.parseInt(coordMatcher.group(2));
        Coordinates coord = new Coordinates(x, y);

        // Check if coordinate is in highlight mask
        if(!highlightMask.contains(coord)){
            view.showWarning("Coordinate " + coord + " is not a valid location. It's not in the highlight mask.");
            return null;
        }
        return coord;
    }

    private LoadableType parseLoadableType(String loadableTypeStr) {
        try {
            return LoadableType.valueOf(loadableTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            view.showWarning("Invalid LoadableType. Available types: " + Arrays.toString(LoadableType.values()));
            return null;
        }
    }

    private int parseAmount(String amountStr) {
        try {
            int amount = Integer.parseInt(amountStr);
            if(amount <= 0) {
                view.showWarning("Amount must be a positive integer.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            view.showWarning("Invalid amount. Please provide a positive integer.");
            return -1;
        }
    }

    private int countLoadableType(List<LoadableType> loadables, LoadableType type) {
        int count = 0;
        for(LoadableType lt : loadables) {
            if(lt == type) {
                count++;
            }
        }
        return count;
    }

    private void addToLocalCargo(Coordinates coord, LoadableType loadableType, int amount) {
        Map<Coordinates, List<LoadableType>> localCargo = PIRState.getLocalCargo();
        if (!localCargo.containsKey(coord)) {
            //TODO: before putting, we should cast the tile at the container position to a container tile,
            // and make sure that the remote content + the local content is not filling up the whole container.
            // the check already gets done server-side, but might be optimal to add this on the client aswell.
            localCargo.put(coord, new ArrayList<>());
        }

        for (int i = 0; i < amount; i++) {
            localCargo.get(coord).add(loadableType);
        }
    }

    private int countTotalAllocated() 	{
        int totalAllocated = 0;
        for (List<LoadableType> allocated : PIRState.getLocalCargo().values()) {
            totalAllocated += allocated.size();
        }
        return totalAllocated;
    }

    @Override
    protected boolean validateCommand(String command, String[] args) throws CommandNotAllowedException {
        if (PIRState.getActivePIRType() == PIRType.DELAY) {
            return true;  // any input closes a delay
        }
        else {
            return switch(command){
                case "" -> true;  // valid onVoid
                case "choose" -> validateChooseCommand(args);
                case "activate" -> validateActivateCommand(args);
                case "allocate" -> validateAllocateCommand(args);
                case "confirm" -> validateConfirmCommand();
                case "remove" -> validateRemoveCommand(args);
                case "endTurn" -> true;
                default -> throw new CommandNotAllowedException(command, args);
            };
        }
    }

    @Override
    protected void performCommand(String command, String[] args) throws RemoteException {
        if (PIRState.getActivePIRType() == PIRType.DELAY) {
            executeEndTurnCommand();  // any input closes a delay
        }
        else {
            switch(command){
                case "" -> view.onRefresh();  // on simple enter refresh
                case "choose" -> server.pirSelectMultipleChoice(client, Integer.parseInt(args[0]));
                case "activate" -> executeActivateCommand(args);
                case "allocate" -> executeAllocateCommand();
                case "confirm" -> executeConfirmCommand();
                case "remove" -> executeRemoveCommand();
                case "endTurn" -> executeEndTurnCommand();
            }
        }
    }
}
