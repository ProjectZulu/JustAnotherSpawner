package jas.common.spawner.creature.entry;

import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandomItem;

//TODO: Large Constructor could probably use Factory / Or Split packSize into Its Own Immutable Class
public class SpawnListEntry extends WeightedRandomItem {
    public final Class<? extends EntityLiving> livingClass;
    public final int packSize;
    public final String biomeName;
    public final int minChunkPack;
    public final int maxChunkPack;

    public SpawnListEntry(Class<? extends EntityLiving> livingClass, String biomeName, int weight, int packSize,
            int minChunkPack, int maxChunkPack) {
        super(weight);
        this.livingClass = livingClass;
        this.packSize = packSize;
        this.biomeName = biomeName;
        this.minChunkPack = minChunkPack;
        this.maxChunkPack = maxChunkPack;
    }

    public LivingHandler getLivingHandler() {
        return CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass);
    }
}
