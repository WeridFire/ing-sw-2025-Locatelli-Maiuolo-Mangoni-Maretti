package it.polimi.ingsw.player.exceptions;

public class NoTileInHandException extends Exception {
	public NoTileInHandException() {
		super("You don't have a tile in your hand.");
	}
}
