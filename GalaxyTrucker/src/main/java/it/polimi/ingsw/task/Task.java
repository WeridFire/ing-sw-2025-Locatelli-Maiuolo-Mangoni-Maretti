package it.polimi.ingsw.task;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRType;
import it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class Task implements ICLIPrintable {

	/**
	 * The gameID associated to this task.
	 */
	private UUID gameId;

	/**
	 * The username of the player associated to this task. Can be null.
	 */
	private String player;

	/**
	 * The duration of the task, after which it will be autocompleted
	 */
	private int duration;

	/**
	 * The expiration of the task, set based on the duration upon registration of the task to the task handler.
	 */
	private int expiration;

	private TaskType taskType;




	public Task(String player, int duration, TaskType taskType){
		this.player = player;
		this.duration = duration;
		this.taskType = taskType;
	}

	/**
	 * Sets the gameID of the game associated to this task. This should be called only when finalizing the task and
	 * registering it onto the task handler.
	 * @param gameId the gameID
	 */
	protected void setGameId(UUID gameId){
		this.gameId = gameId;
	}

	protected void setExpiration(int currentEpoch){
		this.expiration = currentEpoch + duration;
	}

	protected void checkForTurn(String checkingPlayer) throws WrongPlayerTurnException {
		if(!Objects.equals(this.player, player)){
			throw new WrongPlayerTurnException(player, checkingPlayer, this.taskType);
		}
	}

	public static int getEpochTimestamp(){
		return (int) (System.currentTimeMillis() / 1000);
	}

	/**
	 * Function that checks some task requirements for completion, and then returns if it is complete or not.
	 * @return whether if the task has been completed or not.
	 */
	public boolean checkCondition(){
		return false;
	}

	public int getDuration(){
		return this.duration;
	}

	public int getExpiration(){
		return this.expiration;
	}

	public void finish(){
		return;
	}

	public TaskType getTaskType(){
		return this.taskType;
	}

	public UUID getGameId(){
		return this.gameId;
	}

	public String getUsername(){
		return this.getUsername();
	}

	public void makeChoice(Player player, int choice) throws WrongPlayerTurnException, InputNotSupportedException {
		throw new InputNotSupportedException(getTaskType());
	}



}
