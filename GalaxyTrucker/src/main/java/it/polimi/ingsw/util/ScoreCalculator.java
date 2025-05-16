package it.polimi.ingsw.util;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.CargoHoldTile;
import it.polimi.ingsw.shipboard.visitors.CalculatorCargoInfo;

import java.io.Serializable;

public class ScoreCalculator implements Serializable {

    private static final int BLUE_GOODS_MULTIPLIER = 1;
    private static final int GREEN_GOODS_MULTIPLIER = 2;
    private static final int YELLOW_GOODS_MULTIPLIER = 3;
    private static final int RED_GOODS_MULTIPLIER = 4;

    /**
     * Counts all the goods and adds up the value
     * then adds credits to the score
     * then subtracts a point for each lost tile
     * @param p player of which we are calculating the score
     * @return the player's score
     */
    public static float calculateScore(Player p){
        float score = 0;
        int goodsCount;
        CalculatorCargoInfo<CargoHoldTile> goodsInfo;

        //Add the goods to the score
        goodsInfo = p.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo();

        goodsCount = goodsInfo.count(LoadableType.BLUE_GOODS);
        score += goodsCount * BLUE_GOODS_MULTIPLIER;

        goodsCount = goodsInfo.count(LoadableType.GREEN_GOODS);
        score += goodsCount * GREEN_GOODS_MULTIPLIER;

        goodsCount = goodsInfo.count(LoadableType.YELLOW_GOODS);
        score += goodsCount * YELLOW_GOODS_MULTIPLIER;

        goodsCount = goodsInfo.count(LoadableType.RED_GOODS);
        score += goodsCount * RED_GOODS_MULTIPLIER;

        //Each good is worth half if the player is out of the race
        if(p.isEndedFlight()){
            score = score / 2;
        }

        //Add credits
        score += p.getCredits();

        //Pay for repairs
        score -= p.getLostTiles().size();

        return score;
    }
}
