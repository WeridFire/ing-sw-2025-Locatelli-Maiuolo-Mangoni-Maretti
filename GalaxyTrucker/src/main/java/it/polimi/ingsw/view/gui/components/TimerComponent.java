package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.GameLevelStandards;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class TimerComponent extends TextFlow {

    private static TimerComponent instance;

    public static TimerComponent getInstance() {
        if (instance == null) {
            instance = new TimerComponent();
        }
        return instance;
    }

    private static final int START_SECONDS = Default.HOURGLASS_SECONDS;
    private final Timeline timeline;

    private int secondsRemaining;
    private Runnable onTimerFinished;
    private boolean isRunning = false;
    private int totalTimerSlots;

    public TimerComponent() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        this.setOnTimerFinished(this::defaultBehavior);
        reset();
    }

    private Text formatTime(int totalSeconds) {
        Color emphasis = totalSeconds > 15 ? null : (totalSeconds > 5 ? Color.DARKORANGE : Color.RED);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        Text text = new Text(String.format("%02d:%02d", minutes, secs));
        if (emphasis != null) {
            text.setFill(emphasis);
        }
        return text;
    }

    private Text[] formatSlots(int timerSlot) {
        Text[] slots = new Text[totalTimerSlots];
        for (int i = 0; i < timerSlot; i++) {
            slots[i] = new Text("●");
            slots[i].setFill(Color.GRAY);
        }
        slots[timerSlot] = new Text("●");
        slots[timerSlot].setFill(isRunning ? Color.GREEN : Color.RED);
        for (int i = timerSlot + 1; i < totalTimerSlots; i++) {
            slots[i] = new Text("●");
            slots[i].setFill(Color.LIGHTGRAY);
        }
        return slots;
    }

    private void updateText() {
        Integer timerSlot = AssembleState.getTimerSlotIndex();
        Text[] texts;
        if (timerSlot == null) {
            texts = new Text[1];
            texts[0] = new Text("Timer not available for this game level");
        } else {
            texts = new Text[3 + totalTimerSlots];
            texts[0] = new Text("time left: ");
            texts[1] = formatTime(secondsRemaining);
            texts[2] = new Text(" | hourglass slot: ");
            int i = 3;
            for (Text slotText: formatSlots(timerSlot)) {
                texts[i++] = slotText;
            }
        }

        this.getChildren().clear();
        for (Text text : texts) {
            this.getChildren().add(text);
        }
    }

    public void reset() {
        secondsRemaining = START_SECONDS;
        totalTimerSlots = GameLevelStandards.getTimerSlotsCount(AssembleState.getGameData().getLevel());
        updateText();
        stop();
    }

    private void updateTimer() {
        secondsRemaining--;
        if (secondsRemaining <= 0) {
            secondsRemaining = 0;
            stop();
        }
        updateText();

        if (!isRunning) {
            if (onTimerFinished != null) {
                onTimerFinished.run();
            }
        }
    }

    private void defaultBehavior(){
        //default timer behav?
    }

    public void stop() {
        isRunning = false;
        timeline.stop();
    }

    public void start() {
        reset();  // reset before starting
        isRunning = true;
        updateText();
        timeline.playFromStart();
    }

    public void setOnTimerFinished(Runnable onTimerFinished) {
        this.onTimerFinished = onTimerFinished;
    }

    public boolean isRunning() {
        return isRunning;
    }
}