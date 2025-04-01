package it.polimi.ingsw.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A Timer class that manages a countdown using scheduled tasks.
 * It can execute periodic actions and trigger an event when the timer ends.
 */
public class Timer {
    /**
     * The duration of the timer in milliseconds.
     */
    private int duration;

    /**
     * The interval for each tick in milliseconds.
     */
    private final int tick = 1000;

    /**
     * The behavior that defines actions to be executed during the timer's lifecycle.
     */
    private TimerBehavior behavior;

    /**
     * Flag indicating whether the timer is currently running.
     */
    private boolean running;

    /**
     * The scheduled executor service to manage the timer's ticks.
     */
    private ScheduledExecutorService scheduler;

    /**
     * Constructs a Timer with a default duration of 10 seconds.
     */
    public Timer() {
        this.duration = 10 * tick; // default duration is 10 secs
        this.running = false;
    }

    /**
     * Gets the duration of the timer.
     *
     * @return the duration in milliseconds
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the timer.
     *
     * @param duration the new duration in milliseconds
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Sets the behavior of the timer.
     *
     * @param behavior the behavior to execute during the timer's lifecycle
     */
    public void setBehavior(TimerBehavior behavior) {
        this.behavior = behavior;
    }

    /**
     * Starts the timer, triggering periodic ticks and executing the defined behavior.
     * If the timer reaches its duration, it stops and invokes the onTimerEnd event.
     */
    public void start() {
        if (running) return;
        running = true;
        scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(new Runnable() {
            private int elapsedTime = 0;

            @Override
            public void run() {
                if (elapsedTime >= duration) {
                    stop();
                    if (behavior != null) behavior.onTimerEnd();
                    return;
                }
                if (behavior != null) behavior.onTick();
                elapsedTime += tick;
            }
        }, 0, tick, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the timer and terminates the scheduled execution.
     */
    public void stop() {
        if (!running) return;
        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Stops and restarts the Timer.
     */
    public void restart(){
        if (!running) return;
        this.stop();
        this.start();
    }

    /**
     * Checks if the timer is currently running.
     *
     * @return true if the timer is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
