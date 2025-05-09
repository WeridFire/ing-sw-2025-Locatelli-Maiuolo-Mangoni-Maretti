package it.polimi.ingsw.shipboard;

import it.polimi.ingsw.game.Cheats;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.exceptions.TileWithoutNeighborException;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import org.junit.jupiter.api.BeforeEach;

import java.rmi.RemoteException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShipBoardTest {

    private Game game1;
    private Player player1;
    private ShipBoard shipBoard1;

    @BeforeEach
    void setup() throws PlayerAlreadyInGameException, AlreadyEndedAssemblyException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, RemoteException, OutOfBuildingAreaException {
        game1 = new Game();
        UUID uuid1 = UUID.randomUUID();
        player1 = new Player("player1", uuid1);
        game1.addPlayer(player1.getUsername(), uuid1);
        player1.setShipBoard(shipBoard1);

        // Initialize a ShipBoard
        Cheats.cheatShipboard(game1, player1);
    }
}