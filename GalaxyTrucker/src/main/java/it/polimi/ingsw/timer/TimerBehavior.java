package src.main.java.it.polimi.ingsw.timer;

/**
 * An interface that defines the behavior the timer.
 */
public interface TimerBehavior {

    /**
     * Called periodically as the timer ticks.
     * Define actions that should occur at regular intervals
     * while the timer is running.
     */
    void onTick();

    /**
     * Called when the timer has completed its countdown or reached its end condition.
     * Define actions that should occur when the timer finishes.
     */
    void onTimerEnd();

}