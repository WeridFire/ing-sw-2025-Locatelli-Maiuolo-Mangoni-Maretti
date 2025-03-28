package src.main.java.it.polimi.ingsw.gamePhases;

import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.enums.GameState;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.gamePhases.exceptions.NoMoreCardsException;

import java.util.UUID;

public class AdventureGamePhase extends PlayableGamePhase{
    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameState     The current state of the game.
     * @param gameData      The game data.
     */
    public AdventureGamePhase(UUID gameId, GamePhaseType gamePhaseType, GameState gameState, GameData gameData) {
        super(gameId, gamePhaseType, gameState, gameData);
    }

    @Override
    public void playLoop() {
        //wait for player tacking the car
        gameData.getDeck().drawNextCard();
        if(gameData.getDeck().getTopCard() != null){
            gameData.getDeck().getTopCard().playEffect(gameId);
        }else{
            //TODO: endgame
        }

            //TODO: come lo pensiamo? coi thread?

    }

    @Override
    public void onTick() {

    }

    @Override
    public void onTimerEnd() {

    }
}
