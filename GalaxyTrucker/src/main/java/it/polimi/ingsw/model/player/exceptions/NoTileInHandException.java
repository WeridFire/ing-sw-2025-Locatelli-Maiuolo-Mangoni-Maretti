package it.polimi.ingsw.model.player.exceptions;

public class NoTileInHandException extends Exception {
	public NoTileInHandException() {
		super("You don't have a tile in your hand.");
	}
}
