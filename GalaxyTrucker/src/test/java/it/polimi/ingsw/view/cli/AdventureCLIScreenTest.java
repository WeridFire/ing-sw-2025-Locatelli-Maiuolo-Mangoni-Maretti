package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.GameLevelStandards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AdventureCLIScreenTest {

    private GameClient mockClient;

    @BeforeEach
    public void setup() throws AlreadyRunningServerException, InterruptedException, NotBoundException, IOException {
        if (!GameServer.isRunning()) {
            GameServer.start();
        }
        mockClient = GameClient.create(Default.USE_RMI, Default.HOST, Default.PORT(Default.USE_RMI), Default.USE_GUI);
    }

    private List<Player> preparePlayers(int nPlayers, GameLevel gameLevel) {
        List<Player> players = new ArrayList<>(nPlayers);
        for (int i = 0; i < nPlayers; i++) {
            players.add(new Player("player " + (i + 1), UUID.randomUUID(), MainCabinTile.Color.fromPlayerIndex(i)));
            players.getLast().setShipBoard(ShipBoard.create(gameLevel, MainCabinTile.Color.fromPlayerIndex(i)));
        }

        List<Integer> positions = new ArrayList<>(GameLevelStandards.getFlightBoardParkingLots(gameLevel));
        for (Player player : players) {
            player.setPosition(positions.removeFirst());
        }
        return players;
    }

    @Test
    public void testBoardLevelOne() {
        AdventureCLIScreen c = new AdventureCLIScreen(mockClient);
        List<Player> players = preparePlayers(3, GameLevel.ONE);

        System.out.println("\nStarting positions:\n");
        System.out.println(c.getBoardFrame(GameLevel.ONE, players, null));

        players.get(1).setPosition(players.get(0).getPosition() + 1);
        players.sort(Comparator.comparingInt(Player::getOrder));
        System.out.println("\nThe second player advanced until it reached one position more than the leader:\n");
        System.out.println(c.getBoardFrame(GameLevel.ONE, players, null));

        players.get(2).setPosition(players.get(0).getPosition() + 7);
        players.sort(Comparator.comparingInt(Player::getOrder));
        System.out.println("\nThe third player advanced until it reached 7 positions more than the new leader:\n");
        System.out.println(c.getBoardFrame(GameLevel.ONE, players, null));
    }

    @Test
    public void testBoardLevelTwo() {
        AdventureCLIScreen c = new AdventureCLIScreen(mockClient);
        List<Player> players = preparePlayers(4, GameLevel.TWO);
        System.out.println(c.getBoardFrame(GameLevel.TWO, players, null));
    }
}
