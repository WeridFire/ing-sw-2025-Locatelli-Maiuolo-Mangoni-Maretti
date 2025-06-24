package it.polimi.ingsw.model.gamePhases;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.model.game.Cheats;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.game.exceptions.ColorAlreadyInUseException;
import it.polimi.ingsw.model.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.model.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.model.gamePhases.ScoreGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.AlreadyPickedPosition;
import it.polimi.ingsw.model.gamePhases.exceptions.IllegalStartingPositionIndexException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.exceptions.NoShipboardException;
import it.polimi.ingsw.model.player.exceptions.TooManyItemsInHandException;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;

class ScoreScreenGamePhaseTest {

    private Player[] players;
    private ScoreGamePhase scoreScreenGamePhase;

    public void setup(GameLevel gameLevel) throws
            GameAlreadyRunningException,
            ColorAlreadyInUseException,
            PlayerAlreadyInGameException,
            AlreadyEndedAssemblyException,
            RemoteException,
            TooManyItemsInHandException,
            IllegalStartingPositionIndexException,
            NoShipboardException,
            AlreadyPickedPosition
    {
        final int totPlayers = 4;
        final String[] coolNames = new String[] { "alpha", "beta", "gamma", "delta" };
        players = new Player[totPlayers];

        GameData gd = new GameData(UUID.randomUUID());
        gd.setLevel(gameLevel);

        // players association to game
        for (int i = 0; i < totPlayers; i++) {
            MainCabinTile.Color color = MainCabinTile.Color.fromPlayerIndex(i);
            players[i] = new Player(coolNames[i], UUID.randomUUID(), color);
            players[i].disconnect();
            players[i].setShipBoard(ShipBoard.create(gameLevel, color));
            gd.getUnorderedPlayers().add(players[i]);
        }

        // simulate assemble
        for (int i = 0; i < totPlayers; i++) {
            Cheats.randomFillShipboard(players[i].getShipBoard(), TilesFactory.createPileTiles());
            players[i].getShipBoard().endAssembly();
        }

        scoreScreenGamePhase = new ScoreGamePhase(gd);
    }

    private void printScores(Map<Player, Float> sortedScores) {

        System.out.println("\nPlayer Scores (Descending Order):");
        System.out.println(  "--------------------------------");

        int rank = 0;
        Float prevScore = null;
        int sameRankCount = 0;

        for (Map.Entry<Player, Float> entry : sortedScores.entrySet()) {
            float currentScore = entry.getValue();

            // If score is different from previous, update rank
            if (prevScore != null && currentScore == prevScore) {
                sameRankCount++;    // Increment tie counter
            } else {
                rank += 1 + sameRankCount;  // Apply accumulated ties
                sameRankCount = 0;      // Reset counter
            }

            System.out.printf("%d. %s: %.2f%n",
                    rank,
                    entry.getKey().getUsername(),
                    currentScore);

            prevScore = currentScore;
        }
    }

    @Test
    public void testCalculateScores() {
        GameLevel level = GameLevel.TWO;
        try {
            setup(level);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println(" # for level " + level);
        int[] positions = new int[] { 17, -2, 4, 13 };
        int[] credits = new int[] { 0, 4, 3, 4};
        for (int i = 0; i < players.length; i++) {
            System.out.println("player " + players[i].getUsername() + " is in position " + positions[i]);
            System.out.println("  exposed connectors: " + players[i].getShipBoard().getExposedConnectorsCount());
            System.out.println("  credits: " + credits[i]);

            players[i].setPosition(positions[i]);
            players[i].addCredits(credits[i]);
        }

        printScores(scoreScreenGamePhase.calculateScores());
    }
}