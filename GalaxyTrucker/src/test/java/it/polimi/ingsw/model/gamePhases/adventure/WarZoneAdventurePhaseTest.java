package it.polimi.ingsw.model.gamePhases.adventure;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.model.cards.AbandonedShipCard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.warzone.*;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;

public class WarZoneAdventurePhaseTest {
    private GameClientMock[] clients;
    private AdventureGamePhase adventureGamePhase;
    private Game game;
    AtomicReference<Throwable> error = new AtomicReference<>();


    @Test
    public void WarZoneGamePhaseTest() throws InterruptedException {
        final String TEXTURE_NAME = "Simple War Zone Card Test";
        Card card = new WarZoneCard(new WarLevel[] {
                new WarLevel(new WarCriteriaCrew(), new WarPunishmentCrewDeath(2))
        }, TEXTURE_NAME, 0);
        clients = setupUntilAdventurePhase(2, error, card);
        game = GamesHandler.getInstance().getGames().getFirst();
        Thread.sleep(500);
        assert Objects.equals(game.getGameData().getDeck().getCurrentCard().getTextureName(), TEXTURE_NAME);
        //everyone approves the new card notification
        Player player = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[0].getClientUUID()));
        int crew = player.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        for(GameClientMock client : clients) {
            Thread.sleep(20);
            client.simulateCommand("endTurn");
        }
        Thread.sleep(500);
        int newCrew = player.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        assert newCrew == crew-2;
        Thread.sleep(1000);
        assert (game.getGameData().getCurrentGamePhaseType() == GamePhaseType.ADVENTURE)
                && (game.getGameData().getPlayersInFlight().size() == 2);
    }

}
