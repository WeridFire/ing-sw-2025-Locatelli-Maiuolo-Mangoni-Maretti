package src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions;

public class UnsupportedLoadableItemException extends RuntimeException {
  public UnsupportedLoadableItemException(String message) {
    super("UnsupportedLoadableItemException: " + message);
  }
  public UnsupportedLoadableItemException() {
    super("UnsupportedLoadableItemException");
  }
}
