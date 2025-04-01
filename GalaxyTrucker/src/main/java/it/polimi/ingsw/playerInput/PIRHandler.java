package it.polimi.ingsw.playerInput;

public class PIRHandler {

	private PIRActivateTiles activateTiles = null;
	private PIRAddLoadables addLoadables = null;
	private PIRChoice choice = null;
	private PIRRemoveLoadables removeLoadables = null;

	/**
	 *
	 * @return true if any turn in the player input request is set, or null if none is not set.
	 */
	private boolean isAnyTurnActive(){
		return (activateTiles != null || addLoadables != null || choice != null || removeLoadables != null);
	}

	/**
	 *
	 * @return the PIR of activating tiles, or null if the current turn doesn't match this type.
	 */
	public PIRActivateTiles getActivateTiles() {
		return activateTiles;
	}

	/**
	 *
	 * @return the PIR of adding loadables, or null if the current turn doesn't match this type.
	 */
	public PIRAddLoadables getAddLoadables() {
		return addLoadables;
	}


	/**
	 *
	 * @return the PIR of making a 2 ways choice, or null if the current turn doesn't match this type.
	 */
	public PIRChoice getChoice() {
		return choice;
	}


	/**
	 *
	 * @return the PIR of removing loadables, or null if the current turn doesn't match this type.
	 */
	public PIRRemoveLoadables getRemoveLoadables() {
		return removeLoadables;
	}

	public void setTurn(PIRActivateTiles activateTiles) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.activateTiles = activateTiles;
	}

	public void setTurn(PIRAddLoadables addLoadables) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.addLoadables = addLoadables;
	}

	public void setTurn(PIRChoice choice) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.choice = choice;
	}

	public void setTurn(PIRRemoveLoadables removeLoadables) {
		if(isAnyTurnActive()){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.removeLoadables = removeLoadables;
	}
}
