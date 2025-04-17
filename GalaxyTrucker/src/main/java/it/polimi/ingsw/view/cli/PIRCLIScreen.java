package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.playerInput.PIRType;
import it.polimi.ingsw.playerInput.PIRs.*;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PIRCLIScreen extends CLIScreen {

	private final Map<Coordinates, List<LoadableType>> localCargo = new HashMap<>();

	private PIR getActivePIR(){
		if(!switchConditions()){
			return null;
		}
		return getLastUpdate()
				.getCurrentGame()
				.getPIRHandler()
				.getPlayerPIR(getLastUpdate().getClientPlayer());
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

	public PIRCLIScreen() {
		super("turn", false, 10);
	}

	@Override
	protected boolean switchConditions() {
		//NOTE! Until PIRHandler is fixed to not be transient (so make it serializable) this will
		//return error inevitably
		return getLastUpdate().getCurrentGame() != null &&
				getLastUpdate().getCurrentGame().getPIRHandler() != null &&
				getLastUpdate().getCurrentGame().getPIRHandler().getPlayerPIR(getLastUpdate()
						.getClientPlayer()) != null;
	}

	@Override
	protected void processCommand(String command, String[] args) throws RemoteException {
		if(getActivePIR() == null){
			return; //to ignore intellij complaining about null
		}

		switch(command){
			case "choose" -> handleChooseCommand(args);
			case "activate" -> handleActivateCommand(args);
			case "allocate" -> handleAllocateCommand(args);
			case "confirm" -> handleConfirmCommand();
			case "remove" -> handleRemoveCommand(args);
			case "endTurn" -> handleEndTurnCommand();
			default -> setScreenMessage("Invalid command. Use help to view available commands.");
		}
	}

	private void handleChooseCommand(String[] args) throws RemoteException {
		if(getActivePIR().getPIRType() != PIRType.CHOICE){
			setScreenMessage("This command is not available for a PIR of type " + getActivePIR().getPIRType());
			return;
		}

		PIRMultipleChoice pir = (PIRMultipleChoice) getActivePIR();
		if(args.length != 1){
			setScreenMessage("Invalid argument. Specify the ID of the choice you'd like to make.");
			return;
		}

		try{
			int choice = Integer.parseInt(args[0]);
			if(choice < 0 || choice >= pir.getPossibleOptions().length){
				setScreenMessage("Invalid integer. The integer should be one of the following options: " +
						formatOptionsWithIndices(pir.getPossibleOptions()));
				return;
			}
			getServer().pirSelectMultipleChoice(getClient(), choice);
		}catch(NumberFormatException e){
			setScreenMessage("Invalid integer. The integer should be one of the following options: " +
					formatOptionsWithIndices(pir.getPossibleOptions()));
		}
	}

	private void handleActivateCommand(String[] args) throws RemoteException {
		if(getActivePIR().getPIRType() != PIRType.ACTIVATE_TILE){
			setScreenMessage("This command is not available for a PIR of type " + getActivePIR().getPIRType());
			return;
		}

		PIRActivateTiles activatePir = (PIRActivateTiles) getActivePIR();
		Set<Coordinates> highlightMask = activatePir.getHighlightMask();

		if(args.length == 0){
			setScreenMessage("Invalid input. Please specify coordinates in the format: (x,y), (x,y), ...");
			return;
		}

		// Join all args to handle cases where spaces might be present between coordinates
		String fullInput = String.join(" ", args);

		// Parse the coordinates
		Set<Coordinates> selectedCoordinates = new HashSet<>();
		Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
		Matcher matcher = pattern.matcher(fullInput);

		boolean foundAny = false;
		while (matcher.find()) {
			foundAny = true;
			try {
				int x = Integer.parseInt(matcher.group(1));
				int y = Integer.parseInt(matcher.group(2));
				Coordinates coord = new Coordinates(x, y);

				// Check if coordinate is in the highlight mask
				if (!highlightMask.contains(coord)) {
					setScreenMessage("Coordinate " + coord + " is not a valid selection. It's not in the highlight mask.");
					return;
				}

				selectedCoordinates.add(coord);
			} catch (NumberFormatException e) {
				setScreenMessage("Invalid coordinate format. Please use integers for x and y values.");
				return;
			}
		}

		if (!foundAny) {
			setScreenMessage("Invalid format. Coordinates must be in the format: (x,y), (x,y), ...");
			return;
		}

		if (selectedCoordinates.isEmpty()) {
			setScreenMessage("No valid coordinates provided. Please specify coordinates from the highlight mask.");
			return;
		}

		// All checks passed, send to server
		getServer().pirActivateTiles(getClient(), selectedCoordinates);
	}

	private void handleAllocateCommand(String[] args) throws RemoteException {
		if(getActivePIR().getPIRType() != PIRType.ADD_CARGO){
			setScreenMessage("This command is not available for a PIR of type " + getActivePIR().getPIRType());
			return;
		}

		PIRAddLoadables pirAdd = (PIRAddLoadables) getActivePIR();
		Set<Coordinates> pirHighlightMask = pirAdd.getHighlightMask();
		List<LoadableType> floatingLoadables = pirAdd.getFloatingLoadables();

		if(args.length != 3){
			setScreenMessage("Invalid arguments. Usage: allocate (x,y) <LoadableType> <amount>");
			return;
		}

		Coordinates coord = parseCoordinates(args[0]);
		if(coord == null) {
			return;
		}

		// Check if coordinate is in highlight mask
		if(!pirHighlightMask.contains(coord)){
			setScreenMessage("Coordinate " + coord + " is not a valid location. It's not in the highlight mask.");
			return;
		}

		// Parse LoadableType
		LoadableType loadableType = parseLoadableType(args[1]);
		if(loadableType == null) {
			return;
		}

		// Parse amount
		int amount = parseAmount(args[2]);
		if(amount <= 0) {
			return;
		}

		// Check if requested loadables are available in floating loadables
		int availableCount = countLoadableType(floatingLoadables, loadableType);

		if(availableCount < amount) {
			setScreenMessage("Not enough " + loadableType + " available. Available: " + availableCount + ", Requested: " + amount);
			return;
		}

		// Add to local cargo
		addToLocalCargo(coord, loadableType, amount);

		// Check if all loadables have been allocated
		int totalAllocated = countTotalAllocated();

		if(totalAllocated == floatingLoadables.size()) {
			getServer().pirAllocateLoadables(getClient(), localCargo);
			localCargo.clear(); // Reset after sending to server
		} else {
			setScreenMessage("Added " + amount + " " + loadableType + " to " + coord + ". " +
					(floatingLoadables.size() - totalAllocated) + " loadables remaining to allocate.");
		}
	}

	private void handleConfirmCommand() throws RemoteException {
		if(getActivePIR().getPIRType() != PIRType.ADD_CARGO && getActivePIR().getPIRType() != PIRType.REMOVE_CARGO){
			setScreenMessage("This command is only available for PIR types ADD_LOADABLES or REMOVE_LOADABLES. Current type: " + getActivePIR().getPIRType());
			return;
		}

		// Check if localCargo is empty
		if(localCargo.isEmpty()){
			setScreenMessage("Nothing to confirm. Please allocate items first using the 'allocate' command.");
			return;
		}

		// Submit to the server based on PIR type
		if(getActivePIR().getPIRType() == PIRType.ADD_CARGO){
			getServer().pirAllocateLoadables(getClient(), localCargo);
			setScreenMessage("Loadables allocation confirmed and sent to server.");
		} else { // PIRType.REMOVE_CARGO
			getServer().pirRemoveLoadables(getClient(), localCargo);
			setScreenMessage("Loadables removal confirmed and sent to server.");
		}

		// Clear the local cargo after submission
		localCargo.clear();
	}

	private void handleRemoveCommand(String[] args) throws RemoteException {
		if(getActivePIR().getPIRType() != PIRType.REMOVE_CARGO){
			setScreenMessage("This command is not available for a PIR of type " + getActivePIR().getPIRType());
			return;
		}

		PIRRemoveLoadables pirRemove = (PIRRemoveLoadables) getActivePIR();
		Set<Coordinates> removeHighlightMask = pirRemove.getHighlightMask();
		Set<LoadableType> allowedCargo = pirRemove.getAllowedCargo();
		int amountToRemove = pirRemove.getAmountToRemove();

		if(args.length != 3){
			setScreenMessage("Invalid arguments. Usage: remove (x,y) <LoadableType> <amount>");
			return;
		}

		Coordinates removeCoord = parseCoordinates(args[0]);
		if(removeCoord == null) {
			return;
		}

		// Check if coordinate is in highlight mask
		if(!removeHighlightMask.contains(removeCoord)){
			setScreenMessage("Coordinate " + removeCoord + " is not a valid location. It's not in the highlight mask.");
			return;
		}

		// Parse LoadableType
		LoadableType removeLoadableType = parseLoadableType(args[1]);
		if(removeLoadableType == null) {
			return;
		}

		// Check if loadable type is allowed to be removed
		if(!allowedCargo.contains(removeLoadableType)){
			setScreenMessage("LoadableType " + removeLoadableType + " is not allowed to be removed. Allowed types: " + allowedCargo);
			return;
		}

		// Parse amount
		int removeAmount = parseAmount(args[2]);
		if(removeAmount <= 0) {
			return;
		}

		// Calculate current total items scheduled for removal
		int currentTotalRemoval = countTotalAllocated();

		// Check if adding this removal would exceed the amount to remove
		if(currentTotalRemoval + removeAmount > amountToRemove) {
			setScreenMessage("Cannot remove " + removeAmount + " more items. You can only remove " +
					(amountToRemove - currentTotalRemoval) + " more items.");
			return;
		}

		// Add to local cargo for removal
		addToLocalCargo(removeCoord, removeLoadableType, removeAmount);

		setScreenMessage("Marked " + removeAmount + " " + removeLoadableType + " at coordinate " + removeCoord + " for removal.");

		// Check if we've reached the total amount to remove
		currentTotalRemoval += removeAmount;
		if(currentTotalRemoval == amountToRemove) {
			getServer().pirRemoveLoadables(getClient(), localCargo);
			setScreenMessage("All requested items marked for removal. Confirming.");
			localCargo.clear(); // Reset after sending to server
		} else {
			setScreenMessage("Marked " + removeAmount + " " + removeLoadableType + " at " + removeCoord + " for removal. " +
					(amountToRemove - currentTotalRemoval) + " more items need to be selected for removal. " +
					"Use >confirm to confirm the choice.");
		}
	}

	private void handleEndTurnCommand() throws RemoteException {
		getServer().pirForceEndTurn(getClient());
		localCargo.clear();
	}

	// Helper methods for parsing and validation
	private Coordinates parseCoordinates(String coordString) {
		Pattern coordPattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
		Matcher coordMatcher = coordPattern.matcher(coordString);

		if(!coordMatcher.matches()){
			setScreenMessage("Invalid coordinate format. Use format: (x,y)");
			return null;
		}

		int x = Integer.parseInt(coordMatcher.group(1));
		int y = Integer.parseInt(coordMatcher.group(2));
		return new Coordinates(x, y);
	}

	private LoadableType parseLoadableType(String loadableTypeStr) {
		try {
			return LoadableType.valueOf(loadableTypeStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			setScreenMessage("Invalid LoadableType. Available types: " + Arrays.toString(LoadableType.values()));
			return null;
		}
	}

	private int parseAmount(String amountStr) {
		try {
			int amount = Integer.parseInt(amountStr);
			if(amount <= 0) {
				setScreenMessage("Amount must be a positive integer.");
				return -1;
			}
			return amount;
		} catch (NumberFormatException e) {
			setScreenMessage("Invalid amount. Please provide a positive integer.");
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
		if(!localCargo.containsKey(coord)) {
			//TODO: before putting, we should cast the tile at the container position to a container tile,
			// and make sure that the remote content + the local content is not filling up the whole container.
			// the check already gets done server-side, but might be optimal to add this on the client aswell.
			localCargo.put(coord, new ArrayList<>());
		}

		for(int i = 0; i < amount; i++) {
			localCargo.get(coord).add(loadableType);
		}
	}

	private int countTotalAllocated() 	{
		int totalAllocated = 0;
		for(List<LoadableType> allocated : localCargo.values()) {
			totalAllocated += allocated.size();
		}
		return totalAllocated;
	}

	@Override
	protected List<String> getScreenSpecificCommands() {
		List<String> availableCommands = new ArrayList<>();
		if(getActivePIR() == null){
			return availableCommands;
		}

		switch(getActivePIR().getPIRType()){
			case CHOICE -> availableCommands.add("choose <option_number>|Choose an option with the corresponding ID.");
			case ACTIVATE_TILE -> availableCommands.add("activate (x,y), (x,y), ...|Choose the coordinates of tiles to activate.");
			case ADD_CARGO -> {
				availableCommands.add("allocate (x,y) <LoadableType> <amount>|Allocate cargo to a specific coordinate.");
				availableCommands.add("confirm|Submit the current cargo allocation to the server.");
			}
			case REMOVE_CARGO -> {
				availableCommands.add("remove (x,y) <LoadableType> <amount>|Mark cargo at a specific coordinate for removal.");
				availableCommands.add("confirm|Submit the current cargo removal to the server.");
			}
		}

		// Add common commands available for all PIR types
		availableCommands.add("endTurn|End your turn forcefully.");

		return availableCommands;
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		return null;
	}
}