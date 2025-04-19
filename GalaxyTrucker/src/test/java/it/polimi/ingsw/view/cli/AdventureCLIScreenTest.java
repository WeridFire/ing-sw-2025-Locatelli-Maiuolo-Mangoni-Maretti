package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.util.GameLevelStandards;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AdventureCLIScreenTest {

    private List<Player> preparePlayers(int nPlayers, GameLevel gameLevel) {
        List<Player> players = new ArrayList<>(nPlayers);
        for (int i = 0; i < nPlayers; i++) {
            players.add(new Player("player " + (i + 1), UUID.randomUUID()));
            players.getLast().setShipBoard(ShipBoard.create(gameLevel, i));
        }

        List<Integer> positions = new ArrayList<>(GameLevelStandards.getFlightBoardParkingLots(gameLevel));
        for (Player player : players) {
            player.setPosition(positions.removeFirst());
        }
        return players;
    }

    @Test
    public void testBoardLevelOne() {
        AdventureCLIScreen c = new AdventureCLIScreen();
        List<Player> players = preparePlayers(3, GameLevel.ONE);

        System.out.println("\nStarting positions:\n");
        System.out.println(c.getBoardFrame(GameLevel.ONE, players));

        players.get(1).setPosition(players.get(0).getPosition() + 1);
        players.sort(Comparator.comparingInt(Player::getOrder));
        System.out.println("\nThe second player advanced until it reached one position more than the leader:\n");
        System.out.println(c.getBoardFrame(GameLevel.ONE, players));

        players.get(2).setPosition(players.get(0).getPosition() + 7);
        players.sort(Comparator.comparingInt(Player::getOrder));
        System.out.println("\nThe third player advanced until it reached 7 positions more than the new leader:\n");
        System.out.println(c.getBoardFrame(GameLevel.ONE, players));
    }

    @Test
    public void testBoardLevelTwo() {
        AdventureCLIScreen c = new AdventureCLIScreen();
        List<Player> players = preparePlayers(4, GameLevel.TWO);
        System.out.println(c.getBoardFrame(GameLevel.TWO, players));
    }
}
