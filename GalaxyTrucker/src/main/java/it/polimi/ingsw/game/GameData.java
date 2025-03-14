package src.main.java.it.polimi.ingsw.game;

import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.gamePhases.PlayableGamePhase;
import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;

import java.util.ArrayList;
import java.util.HashMap;

public class GameData {

    private GameLevel level;

    private GamePhaseType currentGamePhaseType;

    private PlayableGamePhase currentGamePhase;

    //private ArrayList<Player> players;

    //private Player = turn;

    //private HashMap<Player, ShipBoard> shipFromPlayer;

    private HashMap<CargoType, Integer> availableGoods;

    private int availableCredits;

    private ArrayList<Card> cards;

    private ArrayList<Tile> tiles;

    // Constructors TBD, waiting other classes

}
