package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;

public class WarFactory {
	public static WarCriteria createThrustCriteria() {
		return new WarCriteriaPower(PowerType.THRUST);
	}

	public static WarCriteria createFireCriteria() {
		return new WarCriteriaPower(PowerType.FIRE);
	}

	public static WarCriteria createCrewCriteria() {
		return new WarCriteriaCrew();
	}

	public static WarPunishment createLostDaysPunishment(int lostDays) {
		return new WarPunishmentLoseFlightDays(lostDays);
	}

	public static WarPunishment createCrewDeathPunishment(int crewAmount) {
		return new WarPunishmentCrewDeath(crewAmount);
	}

	public static WarPunishment createProjectilePunishment(Projectile[] projectiles) {
		return new WarPunishmentProjectile(projectiles);
	}

	public static WarPunishment createLostGoodsPunishment(int lostGoods) {
		return new WarPunishmentLoseGoods(lostGoods);
	}
}
