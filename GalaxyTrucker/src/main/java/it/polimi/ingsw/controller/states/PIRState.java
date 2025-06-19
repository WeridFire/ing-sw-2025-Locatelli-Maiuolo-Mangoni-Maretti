package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.task.TaskType;
import it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PIRState extends CommonState {

    private static final Map<Coordinates, List<LoadableType>> localCargo = new HashMap<>();
    public static Map<Coordinates, List<LoadableType>> getLocalCargo() {
        return localCargo;
    }

    public static boolean isTaskActive() {
        return getGameData() != null &&
                getGameData().getTaskStorage() != null &&
                getGameData().getTaskStorage().getPendingTask() != null &&
                getGameData().getTaskStorage().getPendingTask().getPlayer().equals(getPlayer());
    }


    public static TaskType getActiveTaskType() {
        Task pendingTask = getGameData().getTaskStorage().getPendingTask();
        return (pendingTask == null) ? null : pendingTask.getTaskType();
    }

}
