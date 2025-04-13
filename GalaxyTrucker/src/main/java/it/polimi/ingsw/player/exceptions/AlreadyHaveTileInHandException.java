package it.polimi.ingsw.player.exceptions;

public class AlreadyHaveTileInHandException extends Exception {
	public AlreadyHaveTileInHandException(String s) {
		super(s);
	}
	public AlreadyHaveTileInHandException() {
		this("You already are holding a tile.");
	}
}
