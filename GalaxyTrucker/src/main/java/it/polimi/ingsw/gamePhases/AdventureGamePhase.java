package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

public class AdventureGamePhase extends PlayableGamePhase{

    /**Card that determins the andventure*/
    private final Card card;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameData      The game data.
     * @param card the adventure card to play in {@link #playLoop()}
     */
    public AdventureGamePhase(GameData gameData, Card card) {
        super(GamePhaseType.ADVENTURE, gameData);
        this.card = card;
    }

    @Override
    public void playLoop(){
        card.startCardBehaviour(gameData);
    }

    @Override
    public void startTimer(Player p) {

    }

    @Override
    public void endPhase() {
        Card nextCard = gameData.getDeck().drawNextCard();
        if(nextCard != null){
            gameData.setCurrentGamePhase(new AdventureGamePhase(gameData, nextCard));
        }else{
            gameData.setCurrentGamePhase(new ScoreScreenGamePhase(gameData));
        }
        gameData.getCurrentGamePhase().playLoop();
    }
}
