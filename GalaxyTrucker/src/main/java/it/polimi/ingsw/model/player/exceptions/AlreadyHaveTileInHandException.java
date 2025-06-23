package it.polimi.ingsw.model.player.exceptions;

public class AlreadyHaveTileInHandException extends TooManyItemsInHandException {
	public AlreadyHaveTileInHandException(String s) {
		super(s);
	}
	public AlreadyHaveTileInHandException() {
		this("You are already holding a tile.");
	}
}
