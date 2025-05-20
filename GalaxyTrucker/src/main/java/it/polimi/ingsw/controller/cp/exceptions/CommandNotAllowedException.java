package it.polimi.ingsw.controller.cp.exceptions;

import java.util.Arrays;

public class CommandNotAllowedException extends Exception {
	public CommandNotAllowedException(String message) {
		super(message);
	}
	public CommandNotAllowedException() {
		this("Invalid command. Use help to view available commands.");
	}
	public CommandNotAllowedException(String command, String[] args){
		this("Rejected command: " + command + " (args: " + Arrays.toString(args) + ")");
	}
	public CommandNotAllowedException(String command, String reason){
		this("Rejected command: " + command + " | " + reason);
	}
}
