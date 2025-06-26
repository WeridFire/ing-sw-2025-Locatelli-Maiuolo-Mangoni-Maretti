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

/**
 * A singleton JavaFX component that displays a countdown timer and the state of hourglass slots.
 * It is used during the assembly phase of the game.
 */
public class TimerComponent extends TextFlow {

    private static TimerComponent instance;

    /**
     * Gets the singleton instance of the TimerComponent.
     * @return The single instance of this class.
     */
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

    /**
     * Constructs a new TimerComponent.
     * Initializes the timeline and sets the default state.
     */
    public TimerComponent() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        this.setOnTimerFinished(this::defaultBehavior);
        reset();
    }

    /**
     * Formats the given total seconds into a Text object with MM:SS format.
     * The text color changes based on the time remaining to indicate urgency.
     * @param totalSeconds The total seconds to format.
     * @return A {@link Text} object representing the formatted time.
     */
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

    /**
     * Creates an array of Text objects representing the hourglass timer slots.
     * Colors are used to indicate used, current, and available slots.
     * @param timerSlot The index of the current timer slot.
     * @return An array of {@link Text} objects for the slots.
     */
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

    /**
     * Updates the entire text display of the component, including time and slots.
     */
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

    /**
     * Resets the timer to its initial state based on the current game level.
     * Stops the timeline and updates the display.
     */
    public void reset() {
        secondsRemaining = START_SECONDS;
        totalTimerSlots = GameLevelStandards.getTimerSlotsCount(AssembleState.getGameData().getLevel());
        updateText();
        stop();
    }

    /**
     * This method is called every second by the timeline.
     * It decrements the remaining time, updates the text, and calls the finished handler if the timer reaches zero.
     */
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

    /**
     * The default behavior to execute when the timer finishes.
     */
    private void defaultBehavior(){
        //default timer behav?
    }

    /**
     * Stops the timer timeline.
     */
    public void stop() {
        isRunning = false;
        timeline.stop();
    }

    /**
     * Starts the timer. It resets the timer before starting the countdown.
     */
    public void start() {
        reset();  // reset before starting
        isRunning = true;
        updateText();
        timeline.playFromStart();
    }

    /**
     * Sets a callback to be executed when the timer finishes.
     * @param onTimerFinished The {@link Runnable} to execute.
     */
    public void setOnTimerFinished(Runnable onTimerFinished) {
        this.onTimerFinished = onTimerFinished;
    }

    /**
     * Checks if the timer is currently running.
     * @return True if the timer is running, false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }
}