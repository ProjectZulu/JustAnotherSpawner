package jas.common;

import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class EntityDespawner {

    @ForgeSubscribe
    public void despawner(LivingUpdateEvent event) {
        if (event.entityLiving.ticksExisted % 60 == 0 && !event.entityLiving.worldObj.isRemote) {
            LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(event.entityLiving
                    .getClass());

            if (livingHandler != null && livingHandler.getDespawning() != null
                    && livingHandler.getDespawning().isOptionalEnabled()) {
                livingHandler.despawnEntity(event.entityLiving);
            }
        }
    }
}
