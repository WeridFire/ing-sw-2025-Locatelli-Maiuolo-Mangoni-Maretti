package it.polimi.ingsw.cards;

import it.polimi.ingsw.cards.enemy.PiratesCard;
import it.polimi.ingsw.cards.enemy.SlaversCard;
import it.polimi.ingsw.cards.enemy.SmugglersCard;
import it.polimi.ingsw.cards.meteorstorm.MeteorSwarmCard;
import it.polimi.ingsw.cards.planets.Planet;
import it.polimi.ingsw.cards.planets.PlanetsCard;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.cards.warzone.WarFactory;
import it.polimi.ingsw.cards.warzone.WarLevel;
import it.polimi.ingsw.cards.warzone.WarZoneCard;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;

import java.util.ArrayList;
import java.util.List;

public class DeckFactory {
	public static ArrayList<Card> createTutorialDeck() {
		ArrayList<Card> deckPool = new ArrayList<>();
		deckPool.add(new WarZoneCard(new WarLevel[] {
				new WarLevel(WarFactory.createCrewCriteria(), WarFactory.createLostDaysPunishment(3)),
				new WarLevel(WarFactory.createThrustCriteria(), WarFactory.createCrewDeathPunishment(2)),
				new WarLevel(WarFactory.createFireCriteria(), WarFactory.createProjectilePunishment(
						new Projectile[] {
								Projectile.createLightCannonFire(Direction.SOUTH),
								Projectile.createLightCannonFire(Direction.SOUTH)
						}
				))
		}, "GT-cards_I_IT_0116.jpg", 0));
		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.RED_GOODS})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS})),
			},
			2, "GT-cards_I_IT_0113.jpg", 0));
		deckPool.add(new OpenSpaceCard("GT-cards_I_IT_015.jpg", 0));
		deckPool.add(new AbandonedShipCard(3, 1, 4,
				"GT-cards_I_IT_0118.jpg", 0));
		deckPool.add(new AbandonedStationCard(new LoadableType[]{
				LoadableType.YELLOW_GOODS,
				LoadableType.GREEN_GOODS}, 1, 5, "GT-cards_I_IT_0119.jpg", 0));
		deckPool.add(new StarDustCard("GT-cards_I_IT_014.jpg", 0));
		deckPool.add(new SmugglersCard(2, new LoadableType[]{
				LoadableType.YELLOW_GOODS,
				LoadableType.GREEN_GOODS,
				LoadableType.BLUE_GOODS},
				4, 1, "GT-cards_I_IT_012.jpg", 0));
		deckPool.add(new MeteorSwarmCard(new Projectile[]{
				Projectile.createLargeMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.WEST),
				Projectile.createSmallMeteor(Direction.EAST)
		}, "GT-cards_I_IT_019.jpg", 0));
		return deckPool;
	}


	public static ArrayList<Card> createLevelOneDeck(){
		ArrayList<Card> deckPool = createTutorialDeck();
		deckPool.forEach(c -> c.setLevel(1));
		deckPool.add(new OpenSpaceCard("GT-cards_I_IT_016.jpg", 1));
		deckPool.add(new OpenSpaceCard("GT-cards_I_IT_017.jpg", 1));
		deckPool.add(new OpenSpaceCard("GT-cards_I_IT_018.jpg", 1));

		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS,
						LoadableType.YELLOW_GOODS}))
		},
				2, "GT-cards_I_IT_0114.jpg", 1));

		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.YELLOW_GOODS,
						LoadableType.BLUE_GOODS})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.GREEN_GOODS
				})),
		},3, "GT-cards_I_IT_0112.jpg", 1));

		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS,
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS
				})),
		},1, "GT-cards_I_IT_0115.jpg", 1));


		deckPool.add(new SlaversCard(3, 5, 6, 1,
				"GT-cards_I_IT_01.jpg", 1));

		deckPool.add(new PiratesCard(4, new Projectile[]{
				Projectile.createLightCannonFire(Direction.NORTH),
				Projectile.createHeavyCannonFire(Direction.NORTH),
				Projectile.createLightCannonFire(Direction.NORTH),
		}, 5, 1, "GT-cards_I_IT_013.jpg", 1));

		deckPool.add(new MeteorSwarmCard(new Projectile[]{
				Projectile.createLargeMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createLargeMeteor(Direction.NORTH)
		}, "GT-cards_I_IT_0111.jpg", 1));

		deckPool.add(new MeteorSwarmCard(new Projectile[]{
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.WEST),
				Projectile.createSmallMeteor(Direction.EAST),
				Projectile.createSmallMeteor(Direction.SOUTH),
		}, "GT-cards_I_IT_0110.jpg", 1));

		deckPool.add(new AbandonedShipCard(2, 1, 3,
				"GT-cards_I_IT_0117.jpg", 1));
		deckPool.add(new AbandonedStationCard(new LoadableType[]{
				LoadableType.RED_GOODS,
				LoadableType.RED_GOODS}, 1, 6, "GT-cards_I_IT_0120.jpg", 1));


		return deckPool;
	}

	public static ArrayList<Card> createLevelTwoDeck(){
		ArrayList<Card> deckPool = new ArrayList<>();
		deckPool.add(new WarZoneCard(new WarLevel[]{
				new WarLevel(WarFactory.createFireCriteria(), WarFactory.createLostDaysPunishment(4)),
				new WarLevel(WarFactory.createThrustCriteria(), WarFactory.createLostGoodsPunishment(3)),
				new WarLevel(WarFactory.createCrewCriteria(), WarFactory.createProjectilePunishment(
						new Projectile[] {
								Projectile.createLightCannonFire(Direction.NORTH),
								Projectile.createLightCannonFire(Direction.EAST),
								Projectile.createLightCannonFire(Direction.WEST),
								Projectile.createHeavyCannonFire(Direction.SOUTH)
						}))
		}, "GT-cards_II_IT_0116.jpg", 2));
		deckPool.add(new PlanetsCard(new Planet[] {
				new Planet(List.of(new LoadableType[]{
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS,
						LoadableType.YELLOW_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS
				}))
		},3, "GT-cards_II_IT_0115.jpg", 2));
		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.RED_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS
				}))
		}, 3 , "GT-cards_II_IT_0113.jpg", 2));
		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.RED_GOODS,
						LoadableType.RED_GOODS,
						LoadableType.YELLOW_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.RED_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS,
						LoadableType.BLUE_GOODS
				}))
		}, 4, "GT-cards_II_IT_0112.jpg", 2));
		deckPool.add(new PlanetsCard(new Planet[]{
				new Planet(List.of(new LoadableType[]{
						LoadableType.RED_GOODS,
						LoadableType.YELLOW_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS,
						LoadableType.GREEN_GOODS,
						LoadableType.BLUE_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.GREEN_GOODS,
						LoadableType.GREEN_GOODS
				})),
				new Planet(List.of(new LoadableType[]{
						LoadableType.YELLOW_GOODS
				}))
		},2, "GT-cards_II_IT_0114.jpg", 2));
		deckPool.add(new OpenSpaceCard("GT-cards_II_IT_016.jpg", 2));
		deckPool.add(new OpenSpaceCard("GT-cards_II_IT_017.jpg", 2));
		deckPool.add(new OpenSpaceCard("GT-cards_II_IT_018.jpg", 2));
		deckPool.add(new EpidemicCard("GT-cards_II_IT_015.jpg", 2));
		deckPool.add(new StarDustCard("GT-cards_II_IT_014.jpg", 2));
		deckPool.add(new PiratesCard(7, new Projectile[]{
				Projectile.createHeavyCannonFire(Direction.NORTH),
				Projectile.createLightCannonFire(Direction.NORTH),
				Projectile.createHeavyCannonFire(Direction.NORTH),
		}, 6, 2, "GT-cards_II_IT_013.jpg", 2));
		deckPool.add(new SlaversCard(4,8,7, 2, "GT-cards_II_IT_01.jpg", 2 ));
		deckPool.add(new SmugglersCard(3, new LoadableType[]{
				LoadableType.RED_GOODS,
				LoadableType.YELLOW_GOODS,
				LoadableType.YELLOW_GOODS
		} , 8, 1, "GT-cards_II_IT_012.jpg", 2));
		deckPool.add(new MeteorSwarmCard(new Projectile[]{
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createLargeMeteor(Direction.WEST),
				Projectile.createSmallMeteor(Direction.WEST),
				Projectile.createSmallMeteor(Direction.WEST)
		}, "GT-cards_II_IT_019.jpg", 2));
		deckPool.add(new MeteorSwarmCard(new Projectile[] {
				Projectile.createLargeMeteor(Direction.NORTH),
				Projectile.createLargeMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.SOUTH),
				Projectile.createSmallMeteor(Direction.SOUTH)
		}, "GT-cards_II_IT_0110.jpg", 2));
		deckPool.add(new MeteorSwarmCard(new Projectile[]{
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createSmallMeteor(Direction.NORTH),
				Projectile.createLargeMeteor(Direction.EAST),
				Projectile.createSmallMeteor(Direction.EAST),
				Projectile.createSmallMeteor(Direction.EAST)
		},"GT-cards_II_IT_0111.jpg", 2));
		deckPool.add(new AbandonedStationCard(new LoadableType[] {
				LoadableType.YELLOW_GOODS,
				LoadableType.YELLOW_GOODS,
				LoadableType.GREEN_GOODS
		}, 2, 8, "GT-cards_II_IT_0120.jpg", 2));
		deckPool.add(new AbandonedStationCard(new LoadableType[] {
				LoadableType.RED_GOODS,
				LoadableType.YELLOW_GOODS
		}, 1, 7, "GT-cards_II_IT_0119.jpg", 2));
		deckPool.add(new AbandonedShipCard(5, 2, 8, "GT-cards_II_IT_0118.jpg", 2));
		deckPool.add(new AbandonedShipCard(4,1,6, "GT-cards_II_IT_0117.jpg", 2));

		return deckPool;
	}
}
