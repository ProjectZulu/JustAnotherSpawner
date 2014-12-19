package jas.modern;

import jas.modern.spawner.CountInfo;
import jas.modern.spawner.CustomSpawner;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityDespawner {

    @SubscribeEvent
    public void despawner(LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityLiving && event.entityLiving.ticksExisted % 60 == 0
                && !event.entityLiving.worldObj.isRemote) {
            LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
            CountInfo info = CustomSpawner.determineCountInfo(event.entityLiving.worldObj);
            @SuppressWarnings("unchecked")
            List<LivingHandler> livingHandlers = livingHandlerRegistry
                    .getLivingHandlers((Class<? extends EntityLiving>) event.entityLiving.getClass());
            for (LivingHandler livingHandler : livingHandlers) {
                if (livingHandler != null && livingHandler.getDespawning() != null
                        && livingHandler.getDespawning().isPresent()) {
                    livingHandler.despawnEntity((EntityLiving) event.entityLiving, info);
                }
            }
        }
    }

	@SubscribeEvent
	public void entityPersistance(AllowDespawn event) {
		LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
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
    
    @SubscribeEvent
    public void entityConstructed(EntityConstructing event) {
        if (event.entity instanceof EntityLivingBase) {
            event.entity.registerExtendedProperties(EntityProperties.JAS_PROPERTIES, new EntityProperties());
        }
    }
}
