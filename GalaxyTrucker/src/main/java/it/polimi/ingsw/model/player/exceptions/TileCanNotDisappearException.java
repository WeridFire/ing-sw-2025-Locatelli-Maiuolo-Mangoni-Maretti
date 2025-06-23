package it.polimi.ingsw.model.player.exceptions;

public class TileCanNotDisappearException extends Exception {
    public TileCanNotDisappearException(String message) {
        super(message);
    }
    public TileCanNotDisappearException() {
      this("A tile can not disappear");
    }
}
