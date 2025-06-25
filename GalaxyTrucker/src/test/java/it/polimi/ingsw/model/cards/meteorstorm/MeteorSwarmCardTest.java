package it.polimi.ingsw.model.cards.meteorstorm;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;
import static org.junit.jupiter.api.Assertions.*;

class MeteorSwarmCardTest {

    private GameClientMock[] clients;
    private AdventureGamePhase adventureGamePhase;
    private Game game;
    AtomicReference<Throwable> error = new AtomicReference<>();
    MeteorSwarmCard meteorSwarmCard = new MeteorSwarmCard(new Projectile[]{
            Projectile.createLargeMeteor(Direction.NORTH),
            Projectile.createSmallMeteor(Direction.NORTH),
            Projectile.createLargeMeteor(Direction.NORTH)
    }, "GT-cards_I_IT_0111.jpg", 1);;

    @Test
    void testPlayEffect() throws InterruptedException {
        Card card = meteorSwarmCard;
        clients = setupUntilAdventurePhase(2, error, card);
        game = GamesHandler.getInstance().getGames().getFirst();
        Thread.sleep(1000);
        assertEquals(game.getGameData().getDeck().getCurrentCard().getTitle(), card.getTitle());

        for(GameClientMock client : clients) {
            Thread.sleep(20);
            client.simulateCommand("endTurn");
        }
    }

}