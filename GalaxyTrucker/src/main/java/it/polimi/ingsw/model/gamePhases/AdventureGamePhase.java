package it.polimi.ingsw.model.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.util.Default;

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
        notifyAdventureToPlayers();
        card.playEffect(gameData);
    }

    /**
     * Blocking function that will wait for all the players to get notified about a new adventure card drawn
     * by the leader. Will return once everyone has pressed [Enter] or the cooldown ended for all.
     */
    private void notifyAdventureToPlayers() throws InterruptedException {
        Player leader = gameData.getPlayersInFlight().getFirst();
        if(leader == null){
            return;
        }
        String leaderName = leader.toColoredString("[", "]");
        gameData.getPIRHandler().broadcastPIR(
                gameData
                        .getPlayers(Player::isConnected),
                (player, pirHandler) -> {

                    PIRDelay pirDelay = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
                            "The leader " + leaderName + " has drawn a new Adventure Card: " + card.getTitle(),
                            card.getCLIRepresentation());
                    pirHandler.setAndRunTurn(pirDelay);
                });
    }

}
