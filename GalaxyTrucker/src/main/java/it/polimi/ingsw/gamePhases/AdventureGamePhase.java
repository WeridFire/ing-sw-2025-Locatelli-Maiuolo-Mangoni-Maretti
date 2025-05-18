package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import java.util.UUID;

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
    public void playLoop() throws InterruptedException {
        card.playEffect(gameData);
        synchronized (gameData.getUnorderedPlayers()) {
            if(gameData.getPlayers().isEmpty()){
                GamesHandler gamesHandler = GamesHandler.getInstance();
                gamesHandler.getGames().remove(gamesHandler.getGame(this.gameId));
            }
        }
    }

    @Override
    public void startTimer(Player p) {

    }

}
