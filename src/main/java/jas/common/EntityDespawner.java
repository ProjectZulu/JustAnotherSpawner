package jas.common;

import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityDespawner {

    @SubscribeEvent
    public void despawner(LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityLiving && event.entityLiving.ticksExisted % 60 == 0
                && !event.entityLiving.worldObj.isRemote) {
            LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
            @SuppressWarnings("unchecked")
            List<LivingHandler> livingHandlers = livingHandlerRegistry
                    .getLivingHandlers((Class<? extends EntityLiving>) event.entityLiving.getClass());
            for (LivingHandler livingHandler : livingHandlers) {
                if (livingHandler != null && livingHandler.getDespawning() != null
                        && livingHandler.getDespawning().isPresent()) {
                    livingHandler.despawnEntity((EntityLiving) event.entityLiving);
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
