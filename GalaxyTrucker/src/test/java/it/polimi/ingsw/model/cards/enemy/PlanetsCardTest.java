package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.cards.AbandonedShipCard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.planets.Planet;
import it.polimi.ingsw.model.cards.planets.PlanetsCard;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class PlanetsCardTest {

    private GameClientMock[] clients;
    private AdventureGamePhase adventureGamePhase;
    private Game game;
    AtomicReference<Throwable> error = new AtomicReference<>();

    Planet planet1 =  new Planet(List.of(new LoadableType[]{
            LoadableType.RED_GOODS,
            LoadableType.RED_GOODS}));

    PlanetsCard planetsCard = new PlanetsCard(new Planet[]{
            planet1,
            new Planet(List.of(new LoadableType[]{
                    LoadableType.RED_GOODS,
                    LoadableType.BLUE_GOODS,
                    LoadableType.BLUE_GOODS})),
            new Planet(List.of(new LoadableType[]{
                    LoadableType.YELLOW_GOODS})),},2, "GT-cards_I_IT_0113.jpg", 0);

    @Test
    public void PlayEffectTest() throws InterruptedException, IncorrectGamePhaseTypeException {
        Card card = planetsCard;
        clients = setupUntilAdventurePhase(2, error, card);
        game = GamesHandler.getInstance().getGames().getFirst();
        Thread.sleep(1000);
        assertEquals(game.getGameData().getDeck().getCurrentCard().getTitle(), card.getTitle());

        for(GameClientMock client : clients) {
            Thread.sleep(20);
            client.simulateCommand("endTurn");
        }
        //Land on the 1st planet
        Thread.sleep(1000);
        clients[0].simulateCommand("choose", "1");
        Thread.sleep(1000);
        clients[0].simulateCommand("choose", "1");
        Thread.sleep(1000);
        clients[0].simulateCommand("allocate", "(9,7) RED_GOODS 2");
        Player player = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[0].getClientUUID()));
        int cargo = player.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().countAll(LoadableType.CARGO_SET);
        clients[0].simulateCommand("endTurn");
        player.getShipBoard().resetVisitors();
        int newCargo = player.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CARGO_SET);
        assertEquals(newCargo, cargo+2);
        Thread.sleep(1000);
        assert game.getGameData().getCurrentGamePhaseType() == GamePhaseType.ADVENTURE;
    }

    @Test
    void testGetPlanets() {
        assertNull(planetsCard.getPlanet(-1));
        assertEquals(planet1, planetsCard.getPlanet(0));
    }

    @Test
    void testGetCLIRepresentation() {
        List<LoadableType> l = new ArrayList<>();
        l.add(LoadableType.RED_GOODS);
        l.add(LoadableType.BLUE_GOODS);
        l.add(LoadableType.GREEN_GOODS);
        l.add(LoadableType.YELLOW_GOODS);

        // Create a pirate card with specific properties for testing visualization
        PlanetsCard testcard = new PlanetsCard(
                new Planet[]{
                        new Planet(
                            l.stream().toList()
                        ),
                        new Planet(
                                l.stream().toList()
                                ),
                        new Planet(
                                l.stream().toList()
                        ),
                        new Planet(
                                l.stream().toList()
                        ),
                },
                5,
                "",
                3
        );

        // Get the CLI representation
        CLIFrame frame = testcard.getCLIRepresentation();

        System.out.println(frame);
    }
}
