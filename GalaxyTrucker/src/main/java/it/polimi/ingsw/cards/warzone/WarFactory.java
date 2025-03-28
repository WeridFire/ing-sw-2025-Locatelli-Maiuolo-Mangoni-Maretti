package src.main.java.it.polimi.ingsw.cards.warzone;

import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WarFactory {
	public static WarCriteria createThrustCriteria() {
		//TODO: change the method used to also require player to specify his desired enabled thrusters.
		return new WarCriteria(Comparator.comparingDouble(p -> p.getShipBoard().getVisitorCalculateFirePower().getBaseFirePower()));
	}

	public static WarCriteria createFireCriteria() {
		//TODO: change the method used to also require player to specify his desired enabled turrets.
		return new WarCriteria(Comparator.comparingDouble(p -> p.getShipBoard().getVisitorCalculateFirePower().getBaseFirePower())); // Placeholder
	}

	public static WarCriteria createCrewCriteria() {
		return new WarCriteria(Comparator.comparingInt(p -> p.getShipBoard().getVisitorCalculateCargoInfo()
				.getCrewInfo().countAll(LoadableType.CREW_SET))); // Placeholder
	}

	public static Consumer<Player> createLostDaysPunishment(int lostDays) {
		return player -> Card.movePlayer(player, lostDays);
	}

	public static Consumer<Player> createCrewDeathPunishment(int crewAmount) {
		//TODO: implement method so that it asks the player where they wants their crew to be removed.
		return (player) -> player.addCredits(1);
	}

	public static Consumer<Player> createProjectilePunishment(Projectile[] projectiles) {
		return (player) -> {};
		//TODO: implement hitting the player accordingly to the projectiles.
	}

	public static Consumer<Player> createLostGoodsPunishment(int lostGoods) {
		return (player) -> {};
		//TBD : implement losing cargo from the ship.
	}
}
