package it.polimi.ingsw.view.gui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class TimerComponent extends Label {

    private static TimerComponent instance;

    public static TimerComponent getInstance() {
        if (instance == null) {
            instance = new TimerComponent();
        }
        return instance;
    }

    private static final int START_SECONDS = 30;
    private int secondsRemaining;
    private Timeline timeline;
    private Runnable onTimerFinished;
    private boolean isRunning = false;

    public TimerComponent() {
        this.secondsRemaining = START_SECONDS;
        this.setText(formatTime(secondsRemaining));

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        this.setOnTimerFinished(this::defaultBehavior);
    }

    private void updateTimer() {
        secondsRemaining--;
        this.setText(formatTime(secondsRemaining));
        if (secondsRemaining <= 0) {
            isRunning = false;
            timeline.stop();
            if (onTimerFinished != null) {
                onTimerFinished.run();
            }
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public void reset() {
        secondsRemaining = START_SECONDS;
        this.setText(formatTime(secondsRemaining));
        stop();
    }

    public void stop() {
        timeline.stop();
    }

    public void start() {
        reset(); // resetta prima di partire
        isRunning = true;
        timeline.playFromStart();
    }

    public void setOnTimerFinished(Runnable onTimerFinished) {
        this.onTimerFinished = onTimerFinished;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void defaultBehavior(){
        //default timer behav?
    }
}