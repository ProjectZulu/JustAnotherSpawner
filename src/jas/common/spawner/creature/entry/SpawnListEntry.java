package jas.common.spawner.creature.entry;

import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandomItem;

public class SpawnListEntry extends WeightedRandomItem {
    public final Class<? extends EntityLiving> livingClass;
    public final int packSize;
    public final String biomeName;

    public SpawnListEntry(Class<? extends EntityLiving> livingClass, String biomeName, int weight, int packSize) {
        super(weight);
        this.livingClass = livingClass;
        this.packSize = packSize;
        this.biomeName = biomeName;
    }

    public LivingHandler getLivingHandler() {
        return CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass);
    }
}
