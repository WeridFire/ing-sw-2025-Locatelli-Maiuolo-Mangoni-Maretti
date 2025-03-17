package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.cards.enemy.SmugglersCard;
import src.main.java.it.polimi.ingsw.cards.meteorstorm.MeteorStormCard;
import src.main.java.it.polimi.ingsw.cards.planets.Planet;
import src.main.java.it.polimi.ingsw.cards.planets.PlanetsCard;
import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.cards.warzone.WarFactory;
import src.main.java.it.polimi.ingsw.cards.warzone.WarLevel;
import src.main.java.it.polimi.ingsw.cards.warzone.WarZoneCard;
import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.enums.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeckFactory {
	public static ArrayList<Card> createTutorialDeck(UUID gameId) {
		ArrayList<Card> deckPool = new ArrayList<>();
		deckPool.add(new WarZoneCard(new WarLevel[] {
				new WarLevel(WarFactory.createCrewCriteria(), WarFactory.createLostDaysPunishment(3)),
				new WarLevel(WarFactory.createThrustCriteria(), WarFactory.createCrewDeathPunishment(2)),
				new WarLevel(WarFactory.createFireCriteria(), WarFactory.createProjectilePunishment(
						new Projectile[] {
								Projectile.createLightCannonFire(Direction.NORTH),
								Projectile.createLightCannonFire(Direction.NORTH)
						}
				))
		}, "GT-cards_I_IT_0116", 0, gameId));
		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new CargoType[]{
						CargoType.RED_GOODS,
						CargoType.RED_GOODS})),
				new Planet(List.of(new CargoType[]{
						CargoType.RED_GOODS,
						CargoType.BLUE_GOODS,
						CargoType.BLUE_GOODS})),
				new Planet(List.of(new CargoType[]{
						CargoType.YELLOW_GOODS})),
			},
			2, "GT-cards_I_IT_0113.jpg", 0, gameId));
		deckPool.add(new OpenSpaceCard("GT-cards_I_IT_015.jpg", 0, gameId));
		deckPool.add(new AbandonedShipCard(3, 1, 4,
				"GT-cards_I_IT_0118.jpg", 0, gameId));
		deckPool.add(new AbandonedStationCard(new CargoType[]{
				CargoType.YELLOW_GOODS,
				CargoType.GREEN_GOODS}, 1, 5, "GT-cards_I_IT_0119.jpg", 0, gameId));
		deckPool.add(new StarDustCard("GT-cards_I_IT_014.jpg", 0, gameId));
		deckPool.add(new SmugglersCard(2, new CargoType[]{
				CargoType.YELLOW_GOODS,
				CargoType.GREEN_GOODS,
				CargoType.BLUE_GOODS},
				4, 1, "GT-cards_I_IT_012.jpg", 0, gameId));
		deckPool.add(new MeteorStormCard(new Projectile[]{
				Projectile.createLargeMeteor(Direction.SOUTH),
				Projectile.createSmallMeteor(Direction.EAST),
				Projectile.createSmallMeteor(Direction.WEST)
		}, "GT-cards_I_IT_019.jpg", 0, gameId));
		return deckPool;
	}


	public static ArrayList<Card> createLevelOneDeck(UUID gameId){

	}
}
