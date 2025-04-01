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

	/**
	 * This function sets and run the turn itself. After a turn is set, it will automatically run. Once the turn has
	 * finished running (either happening due to timeout or player taking action) it will reset the handler and remove
	 * the previous turn, whatever it is. Also it will delete the generic reference pointer
	 * @param genericPIR The PIR to run and wait for it to finish.
	 */
	private void setGenericReference(PIR genericPIR){
		this.genericReference = genericPIR;
		try {
			genericPIR.run();
			this.removeLoadables = null;
			this.activateTiles = null;
			this.choice = null;
			this.addLoadables = null;
			this.genericReference = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setAndRunTurn(PIRActivateTiles activateTiles) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.activateTiles = activateTiles;
		setGenericReference(activateTiles);
	}

	public void setAndRunTurn(PIRAddLoadables addLoadables) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.addLoadables = addLoadables;
		setGenericReference(addLoadables);
	}

	public void setAndRunTurn(PIRChoice choice) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.choice = choice;
		setGenericReference(choice);
	}

	public void setAndRunTurn(PIRRemoveLoadables removeLoadables) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.removeLoadables = removeLoadables;
		setGenericReference(removeLoadables);
	}

	/**
	 *
	 * @return the player the current turn is dedicated to.
	 */
	public Player getCurrentPlayer() {
		if(!isAnyTurnActive()){
			return null;
		}
		return genericReference.getCurrentPlayer();
	}

	/**
	 *
	 * @return The type of the current turn.
	 */
	public PIRType getType() {
		//basically nullcheck generic reference
		if(!isAnyTurnActive()){
			return null;
		}
		return genericReference.getPlayerTurnType();
	}


	/**
	 * This function will force end a turn. It can be called by any player, but it will check ofcourse that the
	 * caller is the one the turn is dedicated to. If that is the case the function will force the turn to end.
	 * @param player The player sending the command to end the turn.
	 * @throws WrongPlayerTurnException If the player is not who the turn is for.
	 */
	public void endTurn(Player player) throws WrongPlayerTurnException {
		if(!isAnyTurnActive()) {
			return;
		}
		if(!getCurrentPlayer().equals(player)){
			throw new WrongPlayerTurnException(getCurrentPlayer(), player, getType());
		}
		genericReference.endTurn();
		/*
		We don't need in here to nullify generic reference! In fact, check what happens when genericReference ends
		and the caller of genericReference.run() resumes. This happens in this class PIRHandler#setGenericReference.
		As you can see in there the nullification of the reference is already handled.
		 */
	}
}
