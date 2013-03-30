package jas.common;

import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class EntityDespawner {
    
    @ForgeSubscribe
    public void despawner(LivingUpdateEvent event) {
        if (event.entityLiving.ticksExisted % 40 + event.entity.worldObj.rand.nextInt(20) == 0) {
            LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(event.entityLiving
                    .getClass());
            if (livingHandler != null && livingHandler.forceDespawn) {
                livingHandler.despawnEntity(event.entityLiving);
            }
        }
    }
}
