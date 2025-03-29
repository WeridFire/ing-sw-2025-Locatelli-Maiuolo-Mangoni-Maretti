package src.main.java.it.polimi.ingsw.gamePhases;

import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.enums.GameState;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.gamePhases.exceptions.NoMoreCardsException;

import java.util.UUID;

public class AdventureGamePhase extends PlayableGamePhase{

    /**Card that determins the andventure*/
    private Card card;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameData      The game data.
     */
    public AdventureGamePhase(UUID gameId, GamePhaseType gamePhaseType, GameData gameData, Card card) {
        super(gameId, gamePhaseType, gameData);
    }

    @Override
    public void playLoop() {
        card.playEffect(gameData);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onTimerEnd() {

    }
}
