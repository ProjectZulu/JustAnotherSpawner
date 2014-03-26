package jas.common;

import java.util.List;
import java.util.Set;

import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class EntityDespawner {

    @ForgeSubscribe
    public void despawner(LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityLiving && event.entityLiving.ticksExisted % 60 == 0
                && !event.entityLiving.worldObj.isRemote) {
            LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
            @SuppressWarnings("unchecked")
            List<LivingHandler> livingHandlers = livingHandlerRegistry
                    .getLivingHandlers((Class<? extends EntityLiving>) event.entityLiving.getClass());
            for (LivingHandler livingHandler : livingHandlers) {
                if (livingHandler != null && livingHandler.getDespawning() != null
                        && livingHandler.getDespawning().isOptionalEnabled()) {
                    livingHandler.despawnEntity((EntityLiving) event.entityLiving);
                }
            }
        }
    }

    @ForgeSubscribe
    public void entityConstructed(EntityConstructing event) {
        if (event.entity instanceof EntityLivingBase) {
            event.entity.registerExtendedProperties(EntityProperties.JAS_PROPERTIES, new EntityProperties());
        }
    }
}
