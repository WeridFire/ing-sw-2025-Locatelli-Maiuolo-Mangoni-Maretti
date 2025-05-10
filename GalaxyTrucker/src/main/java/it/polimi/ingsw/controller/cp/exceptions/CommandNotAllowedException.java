package it.polimi.ingsw.controller.cp.exceptions;

import java.util.Arrays;

public class CommandNotAllowedException extends Exception {
	public CommandNotAllowedException(String command, String[] args){
		super("Rejected command: " + command + " (args: " + Arrays.toString(args) + ")");
	}
	public CommandNotAllowedException(String command, String reason){
		super("Rejected command: " + command + " | " + reason);
	}
	public CommandNotAllowedException() {
		super("Invalid command. Use help to view available commands.");
	}
}
