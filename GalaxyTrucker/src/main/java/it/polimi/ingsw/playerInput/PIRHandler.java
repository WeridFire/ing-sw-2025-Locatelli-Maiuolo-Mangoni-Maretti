package it.polimi.ingsw.playerInput;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;

public class PIRHandler {

	private PIRActivateTiles activateTiles = null;
	private PIRAddLoadables addLoadables = null;
	private PIRChoice choice = null;
	private PIRRemoveLoadables removeLoadables = null;
	private PIR genericReference;
	/**
	 *
	 * @return true if any turn in the player input request is set, or null if none is not set.
	 */
	private boolean isAnyTurnActive(){
		//if first is not null, second is usually never null but check anyways
		return genericReference == null || genericReference.getPlayerTurnType() == null;
	}

	/**
	 *
	 * @return the PIR of activating tiles, or null if the current turn doesn't match this type.
	 */
	public PIRActivateTiles getActivateTiles() throws InputNotSupportedException {
		if(activateTiles == null){
			throw new InputNotSupportedException(getType());
		}
		return activateTiles;
	}

	/**
	 *
	 * @return the PIR of adding loadables, or null if the current turn doesn't match this type.
	 */
	public PIRAddLoadables getAddLoadables() throws InputNotSupportedException {
		if(addLoadables == null){
			throw new InputNotSupportedException(getType());
		}
		return addLoadables;
	}

	/**
	 *
	 * @return the PIR of making a 2 ways choice, or null if the current turn doesn't match this type.
	 */
	public PIRChoice getChoice() throws InputNotSupportedException {
		if(choice == null){
			throw new InputNotSupportedException(getType());
		}
		return choice;
	}

	/**
	 *
	 * @return the PIR of removing loadables, or null if the current turn doesn't match this type.
	 */
	public PIRRemoveLoadables getRemoveLoadables() throws InputNotSupportedException {
		if(removeLoadables == null){
			throw new InputNotSupportedException(getType());
		}
		return removeLoadables;
	}

	private void setGenericReference(PIR genericPIR){
		this.genericReference = genericPIR;
	}

	public void setTurn(PIRActivateTiles activateTiles) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		setGenericReference(activateTiles);
		this.activateTiles = activateTiles;
	}

	public void setTurn(PIRAddLoadables addLoadables) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		setGenericReference(addLoadables);
		this.addLoadables = addLoadables;
	}

	public void setTurn(PIRChoice choice) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		setGenericReference(choice);
		this.choice = choice;
	}

	public void setTurn(PIRRemoveLoadables removeLoadables) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		setGenericReference(removeLoadables);
		this.removeLoadables = removeLoadables;
	}

	public Player getCurrentPlayer() {
		if(!isAnyTurnActive()){
			return null;
		}
		return genericReference.getCurrentPlayer();
	}

	public PIRType getType() {
		//basically nullcheck generic reference
		if(!isAnyTurnActive()){
			return null;
		}
		return genericReference.getPlayerTurnType();
	}

	public void endTurn(Player player) throws WrongPlayerTurnException {
		if(!isAnyTurnActive()) {
			return;
		}

		if(!getCurrentPlayer().equals(player)){
			throw new WrongPlayerTurnException(getCurrentPlayer(), player, getType());
		}
		genericReference.endTurn();
	}
}
