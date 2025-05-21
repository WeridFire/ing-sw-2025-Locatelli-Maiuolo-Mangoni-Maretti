package it.polimi.ingsw.view.gui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class TimerComponent extends Label {

    private int seconds;
    private Timeline timeline;

    public TimerComponent() {
        this.seconds = 0;
        this.setText(formatTime(seconds));

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimer() {
        seconds++;
        this.setText(formatTime(seconds));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public void reset() {
        seconds = 0;
        this.setText(formatTime(seconds));
    }

    public void stop() {
        timeline.stop();
    }

    public void start() {
        timeline.play();
    }
}