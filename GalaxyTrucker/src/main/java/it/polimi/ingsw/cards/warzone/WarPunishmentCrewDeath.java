package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;

import java.util.function.Consumer;

public class WarPunishmentCrewDeath implements WarPunishment {

    private final int crewAmount;

    public WarPunishmentCrewDeath(int crewAmount) {
        this.crewAmount = crewAmount;
    }

    @Override
    public void apply(Player player, GameData gameData, Consumer<Player> onFinish) {
        gameData.getTaskStorage().addTask(
                new TaskRemoveLoadables(
                        player,
                        30,
                        LoadableType.CREW_SET,
                        crewAmount,
						onFinish
                )
        );
    }
}