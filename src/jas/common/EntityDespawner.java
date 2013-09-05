package jas.common;

import jas.common.spawner.creature.handler.LivingHandler;
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
            LivingHandler livingHandler = JustAnotherSpawner.worldSettings().creatureHandlerRegistry()
                    .getLivingHandler(event.entityLiving.getClass());

            if (livingHandler != null && livingHandler.getDespawning() != null
                    && livingHandler.getDespawning().isOptionalEnabled()) {
                livingHandler.despawnEntity((EntityLiving) event.entityLiving);
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
