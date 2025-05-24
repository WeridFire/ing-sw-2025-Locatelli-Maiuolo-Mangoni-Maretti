package it.polimi.ingsw.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskStorage {

	private final List<Task> previousTasks;

	private final List<Task> pendingTasks;

	private final UUID gameId;

	/**
	 * Storage for containing all the pending and past tasks of a game.
	 * @param gameId The game associated to the task storage.
	 */
	public TaskStorage(UUID gameId) {
		this.previousTasks = new ArrayList<Task>();
		this.pendingTasks = new ArrayList<Task>();
		this.gameId = gameId;
	}

	/**
	 * Adds a new task to the pending tasks queue.
	 * @param task the task to be added.
	 */
	public void addTask(Task task){
		task.setGameId(gameId);
		pendingTasks.add(task);
	}

	/**
	 * Functions that gets called periodically by the main thread.
	 * Iterates through each pending task in the game and checks for it completion, calling the
	 * finish function and removing the current task.
	 */
	public void checkTasks(){
		synchronized(pendingTasks){
			pendingTasks.forEach((task)->{
				if(task.checkCondition()){
					task.finish();
					pendingTasks.remove(task);
					previousTasks.add(task);
				}
			});
		}
	}




}
