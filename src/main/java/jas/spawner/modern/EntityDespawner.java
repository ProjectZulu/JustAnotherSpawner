package jas.spawner.modern;

import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.CustomSpawner;
import jas.spawner.modern.spawner.creature.handler.LivingHandler;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

public class EntityDespawner {

	CountInfo countInfo = null;

	@SubscribeEvent
	public void countUpdater(WorldTickEvent event) {
		if (event.side != Side.SERVER || event.phase == Phase.END) {
			return;
		}
		World world = event.world;
		updateCountInfo(world);
	}

	private void updateCountInfo(World world) {
		countInfo = CustomSpawner.spawnCounter.countEntities(world);
	}

	@SubscribeEvent
	public void despawner(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityLiving && event.entityLiving.ticksExisted % 60 == 0
				&& !event.entityLiving.worldObj.isRemote) {
			LivingHandlerRegistry livingHandlerRegistry = MVELProfile.worldSettings().livingHandlerRegistry();
			if (countInfo == null) {
				updateCountInfo(event.entityLiving.worldObj);
			}
			@SuppressWarnings("unchecked")
			List<LivingHandler> livingHandlers = livingHandlerRegistry
					.getLivingHandlers((Class<? extends EntityLiving>) event.entityLiving.getClass());
			for (LivingHandler livingHandler : livingHandlers) {
				if (livingHandler != null && livingHandler.getDespawning() != null
						&& livingHandler.getDespawning().isPresent()) {
					livingHandler.despawnEntity((EntityLiving) event.entityLiving, countInfo);
				}
			}
		}
	}

	@SubscribeEvent
	public void entityPersistance(AllowDespawn event) {
		if (!event.entity.worldObj.isRemote) {
			LivingHandlerRegistry livingHandlerRegistry = MVELProfile.worldSettings().livingHandlerRegistry();
			@SuppressWarnings("unchecked")
			List<LivingHandler> livingHandlers = livingHandlerRegistry
					.getLivingHandlers((Class<? extends EntityLiving>) event.entityLiving.getClass());
			for (LivingHandler livingHandler : livingHandlers) {
				if (livingHandler != null && livingHandler.getDespawning() != null
						&& livingHandler.getDespawning().isPresent()) {
					event.setResult(Result.DENY);
				}
			}
		}
	}

	@SubscribeEvent
	public void entityConstructed(EntityConstructing event) {
		if (event.entity instanceof EntityLivingBase) {
			event.entity.registerExtendedProperties(EntityProperties.JAS_PROPERTIES, new EntityProperties());
		}
	}
}
