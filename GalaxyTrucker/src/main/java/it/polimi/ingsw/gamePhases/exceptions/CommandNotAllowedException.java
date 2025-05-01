package it.polimi.ingsw.gamePhases.exceptions;

public class CommandNotAllowedException extends Exception {
	public CommandNotAllowedException(String command, String reason){
		super("Rejected command: " + command + " | " + reason);
	}
	public CommandNotAllowedException() {
		super("Invalid command. Use help to view available commands.");
	}
}
