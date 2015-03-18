package jas.spawner.modern.spawner;

import jas.spawner.modern.spawner.tags.BaseFunctions;
import jas.spawner.modern.spawner.tags.TimeFunctions;
import net.minecraft.world.World;

public class TimeHelper implements TimeFunctions {
	private World world;

	public TimeHelper(World world) {
		this.world = world;
	}

	/** Time between [0, 24000] */
	public int timeOfDay() {
		return (int) (world.getWorldTime() % 24000);
	}

	/** True if within an hour before and after midnight */
	public boolean isMidnight() {
		return timeOfDay() > 17000 && timeOfDay() < 19000;
	}

	/** True if within an hour before and after afternoon */
	public boolean isAfternoon() {
		return timeOfDay() > 5000 && timeOfDay() < 7000;
	}

	/** True if within an hour before and after dawn */
	public boolean isDawn() {
		return timeOfDay() < 1000 && timeOfDay() > 23000;
	}

	/** True if within an hour before and after dusk */
	public boolean isDusk() {
		return timeOfDay() > 11000 && timeOfDay() < 13000;
	}

	/** Moon phase from [0 == Full Moon, 7 == Waxing Gibbous] */
	public int moonPhase() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime());
	}

	public boolean isFullMoon() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 0;
	}

	public boolean isWaningGibbous() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 1;
	}

	public boolean isLastQuarter() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 2;
	}

	public boolean isWaningCrescent() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 3;
	}

	public boolean isNewMoon() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 4;
	}

	public boolean isWaxingCrescent() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 5;
	}

	public boolean isFirstQuarter() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 6;
	}

	public boolean isWaxingGibbous() {
		return world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) == 7;
	}
}
