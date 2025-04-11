package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.GameState;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.NoMoreCardsException;
import it.polimi.ingsw.player.Player;

import java.util.UUID;

public class AdventureGamePhase extends PlayableGamePhase{

    /**Card that determins the andventure*/
    private final Card card;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameData      The game data.
     */
    public AdventureGamePhase(UUID gameId, GameData gameData, Card card) {
        super(gameId, GamePhaseType.ADVENTURE, gameData);
        this.card = card;
    }

    @Override
    public void playLoop() {
        card.playEffect(gameData);
    }

    @Override
    public void startTimer(Player p) {

    }

}
