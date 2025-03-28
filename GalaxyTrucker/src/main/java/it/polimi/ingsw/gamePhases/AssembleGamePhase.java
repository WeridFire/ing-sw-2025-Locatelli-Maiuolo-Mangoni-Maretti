package src.main.java.it.polimi.ingsw.gamePhases;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.enums.GameState;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import src.main.java.it.polimi.ingsw.gamePhases.exceptions.NoMoreTimerResetLeftException;
import src.main.java.it.polimi.ingsw.timer.Timer;

import java.util.UUID;

public class AssembleGamePhase extends PlayableGamePhase{

    private int howManyTimerRotationsLeft;

    private final Timer timer;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameData      The game data.
     */
    public AssembleGamePhase(UUID gameId, GamePhaseType gamePhaseType, GameData gameData) throws IncorrectGamePhaseTypeException {
        super(gameId, gamePhaseType, gameData);

        if (!gamePhaseType.equals(GamePhaseType.ASSEMBLE)){
            throw new IncorrectGamePhaseTypeException("Assemble gamePhase type is not coherent");
        }

        if (gameData.getLevel().equals(GameLevel.TESTFLIGHT)){
            this.howManyTimerRotationsLeft = -1;
        } else if (gameData.getLevel().equals(GameLevel.TWO)){
            this.howManyTimerRotationsLeft = 3;
        }

        this.timer = GamesHandler.getInstance().getGame(gameId).getTimer();
    }

    @Override
    public void playLoop() {
        if (howManyTimerRotationsLeft < 0){
            // testflight logic
            return;
        }
        // level two game logic
        // metto server in ascolto

        //possono arrivare 2 cose:
            //gira clessidra
            //pacchetti relativi al mettere e togliere tessere dalla shipboard

        timer.start();
        howManyTimerRotationsLeft--;
    }

    @Override
    public void onTick() {
        //TBD some JavaFX things probably
    }

    @Override
    public void onTimerEnd() {
        if (howManyTimerRotationsLeft == 0){
            //close server logic

        }
    }

    private void resetTimer() throws NoMoreTimerResetLeftException {
        if (howManyTimerRotationsLeft == 0){
            throw new NoMoreTimerResetLeftException("No more timer reset");
        }
        timer.restart();
    }

}
