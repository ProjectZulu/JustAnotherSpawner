package jas.spawner.modern.eventspawn;

import jas.common.helper.MVELHelper;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.eventspawn.EventSpawnRegistry.EventSpawn;
import jas.spawner.modern.eventspawn.context.BlockContext;
import jas.spawner.modern.eventspawn.context.LivingDeathContext;
import jas.spawner.modern.eventspawn.context.PlayerSleepContext;

import java.io.Serializable;
import java.util.List;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventSpawnTrigger {
	public enum EventTrigger {
		LIVING_DEATH, BLOCK_BREAK, SLEEP, LUNAR;
	}

	private MVELProfile profile;

	public EventSpawnTrigger(MVELProfile profile) {
		this.profile = profile;
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		List<EventSpawn> list = profile.worldSettings().eventSpawnRegistry()
				.getEventsForTrigger(EventTrigger.LIVING_DEATH);
		if (!list.isEmpty()) {
			Object context = new LivingDeathContext(event);
			for (EventSpawn eventSpawn : list) {
				SpawnBuilder toSpawn = execute(eventSpawn.expression(), context);
				if (toSpawn != null) {
					toSpawn.spawn(event.entity.worldObj, profile.worldSettings().livingGroupRegistry());
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		List<EventSpawn> list = profile.worldSettings().eventSpawnRegistry()
				.getEventsForTrigger(EventTrigger.BLOCK_BREAK);
		if (!list.isEmpty()) {
			Object context = new BlockContext(event);
			for (EventSpawn eventSpawn : list) {
				SpawnBuilder toSpawn = execute(eventSpawn.expression(), context);
				if (toSpawn != null) {
					toSpawn.spawn(event.world, profile.worldSettings().livingGroupRegistry());
				}
			}
		}
	}

	@SubscribeEvent
	public void onSleep(PlayerSleepInBedEvent event) {
		List<EventSpawn> list = profile.worldSettings().eventSpawnRegistry().getEventsForTrigger(EventTrigger.SLEEP);
		if (!list.isEmpty()) {
			Object context = new PlayerSleepContext(event);
			for (EventSpawn eventSpawn : list) {
				SpawnBuilder toSpawn = execute(eventSpawn.expression(), context);
				if (toSpawn != null) {
					toSpawn.spawn(event.entity.worldObj, profile.worldSettings().livingGroupRegistry());
				}
			}
		}
	}

	public SpawnBuilder execute(Serializable expression, Object context, String... error) {
		return MVELHelper.typedExecuteExpression(SpawnBuilder.class, expression, context, "");
	}
}
