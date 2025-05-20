package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import java.util.UUID;

public class AdventureGamePhase extends PlayableGamePhase {

    /** Card that determines the adventure */
    private final Card card;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameData      The game data.
     * @param card the adventure card to play in this adventure game phase
     */
    public AdventureGamePhase(GameData gameData, Card card) {
        super(GamePhaseType.ADVENTURE, gameData);
        this.card = card;
    }

    @Override
    public void start() {
        try {
            card.playEffect(gameData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExpired() {
        return gameData.getPlayer(p -> gameData.getPIRHandler().isPlayerTurnActive(p)) == null;
    }



}
