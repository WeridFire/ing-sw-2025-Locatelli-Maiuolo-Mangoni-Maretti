package it.polimi.ingsw.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskStorage {

	private final List<Task> previousTasks;

	private final List<Task> pendingTasks;

	private final UUID gameId;

	public TaskStorage(UUID gameId) {
		this.previousTasks = new ArrayList<Task>();
		this.pendingTasks = new ArrayList<Task>();
		this.gameId = gameId;
	}


	public void addTask(Task task){
		task.setGameId(gameId);
		pendingTasks.add(task);
	}


	public void checkTasks(){
		synchronized(pendingTasks){
			pendingTasks.forEach((task)->{
				if(task.checkCondition()){
					pendingTasks.remove(task);
					previousTasks.add(task);
				}
			});
		}
	}




}
