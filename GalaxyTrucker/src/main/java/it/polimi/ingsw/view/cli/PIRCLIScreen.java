package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.playerInput.PIRType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class PIRCLIScreen extends CLIScreen {

	public PIRCLIScreen() {
		super("turn", false);
	}

	private PIRType getPIRType(){
		return getLastUpdate()
				.getCurrentGame()
				.getPIRHandler()
				.getPlayerPIR(getLastUpdate().getClientPlayer())
				.getPIRType();
	}

	@Override
	protected boolean switchConditions() {
		//NOTE! Until PIRHandler is fixed to not be transient (so make it serializable) this will
		//return error inevitably
		return getLastUpdate()
				.getCurrentGame()
				.getPIRHandler()
				.getPlayerPIR(getLastUpdate().getClientPlayer()) != null;
	}

	@Override
	protected void processCommand(String command, String[] args) throws RemoteException {
		switch(command){
			case "choose": //solves YesNoChoice and MultipleChoice
			case "activate": //solves ActivateTiles
			case "load": //solves AddLoadables
			case "remove": //solves RemoveLoadables
			default:
				setScreenMessage("Invalid command. Use help to view available commands.");
				break;
		}
	}

	@Override
	void printScreenSpecificCommands() {
		List<String> availableCommands = new ArrayList<>();
		switch(getPIRType()){
			case CHOICE -> availableCommands.add("choice|Choose an option with the corresponding ID.");
			case ACTIVATE_TILE -> availableCommands.add("activate|Choose the coordinates of tiles to activate.");
			case ADD_CARGO -> availableCommands.add("load|Choose the coordinates and the cargo to load.");
			case REMOVE_CARGO -> availableCommands.add("remove|Choose the coordinates and the items to remove");
		}

		printCommands("turn", availableCommands.toArray(String[]::new));
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		return null;
	}
}
