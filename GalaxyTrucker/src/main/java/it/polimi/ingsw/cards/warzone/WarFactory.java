package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WarFactory {
	public static WarCriteria createThrustCriteria() {
		//TODO: change the method used to also require player to specify his desired enabled thrusters.
		return new WarCriteria(Comparator.comparingDouble(p -> p.getShipBoard().getVisitorCalculatePowers().getInfoThrustPower().getBasePower()));
	}

	public static WarCriteria createFireCriteria() {
		//TODO: change the method used to also require player to specify his desired enabled turrets.
		return new WarCriteria(Comparator.comparingDouble(p -> p.getShipBoard().getVisitorCalculatePowers().getInfoFirePower().getBasePower())); // Placeholder
	}

	public static WarCriteria createCrewCriteria() {
		return new WarCriteria(Comparator.comparingInt(p -> p.getShipBoard().getVisitorCalculateCargoInfo()
				.getCrewInfo().countAll(LoadableType.CREW_SET))); // Placeholder
	}

	public static BiConsumer<Player, GameData> createLostDaysPunishment(int lostDays) {
		return (player, game) -> game.movePlayerBackward(player, lostDays);
	}

	public static BiConsumer<Player, GameData>createCrewDeathPunishment(int crewAmount) {
		//TODO: implement method so that it asks the player where they wants their crew to be removed.
		return (player, game) -> player.addCredits(1);
	}

	public static BiConsumer<Player, GameData> createProjectilePunishment(Projectile[] projectiles) {
		return (player, game) -> {};
		//TODO: implement hitting the player accordingly to the projectiles.
	}

	public static BiConsumer<Player, GameData> createLostGoodsPunishment(int lostGoods) {
		return (player, game) -> {};
		//TBD : implement losing cargo from the ship.
	}
}
