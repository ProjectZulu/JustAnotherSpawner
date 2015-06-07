package jas.spawner.modern.eventspawn;

import jas.common.helper.MVELHelper;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.eventspawn.EventSpawnRegistry.EventSpawn;
import jas.spawner.modern.eventspawn.context.BlockContext;
import jas.spawner.modern.eventspawn.context.ContextHelper;
import jas.spawner.modern.eventspawn.context.LivingDeathContext;
import jas.spawner.modern.eventspawn.context.PlayerSleepContext;

import java.io.Serializable;
import java.util.List;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventSpawnTrigger {
	public enum EventTrigger {
		LIVING_DEATH, BLOCK_BREAK, SLEEP, BREAK_CROP, BREAK_TREE;
	}

	private MVELProfile profile;

	public EventSpawnTrigger(MVELProfile profile) {
		this.profile = profile;
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if (!event.entity.worldObj.isRemote) {
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
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		if (!event.world.isRemote) {
			if (ContextHelper.isBlockCrop(event.block, event.blockMetadata)) {
				List<EventSpawn> list = profile.worldSettings().eventSpawnRegistry()
						.getEventsForTrigger(EventTrigger.BREAK_CROP);
				if (!list.isEmpty()) {
					Object context = new BlockContext(event);
					for (EventSpawn eventSpawn : list) {
						SpawnBuilder toSpawn = execute(eventSpawn.expression(), context);
						if (toSpawn != null) {
							toSpawn.spawn(event.world, profile.worldSettings().livingGroupRegistry());
						}
					}
				}
			} else if (ContextHelper.isBlockTree(event.world, event.pos.getX(), event.pos.getY(), event.pos.getZ(),
					event.block, event.blockMetadata)) {
				List<EventSpawn> list = profile.worldSettings().eventSpawnRegistry()
						.getEventsForTrigger(EventTrigger.BREAK_TREE);
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
	}

	@SubscribeEvent
	public void onSleep(PlayerSleepInBedEvent event) {
		if (!event.entity.worldObj.isRemote) {
			List<EventSpawn> list = profile.worldSettings().eventSpawnRegistry()
					.getEventsForTrigger(EventTrigger.SLEEP);
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
	}
	
	// Move to Passive Spawn --> make helper expressions; users could then create 'lunar' entity types
	// @SubscribeEvent
	// public void lunarCycle(WorldTickEvent event) {
	// if (event.side == Side.SERVER && event.phase == Phase.END && event.world.getWorldTime() % 24000 == 18000) {
	// JASLog.log().info("[%s, %s]", event.world.getWorldTime() % 24000, event.world.getCurrentMoonPhaseFactor());
	// }
	// }

	public SpawnBuilder execute(Serializable expression, Object context, String... error) {
		return MVELHelper.typedExecuteExpression(SpawnBuilder.class, expression, context, "");
	}
}
