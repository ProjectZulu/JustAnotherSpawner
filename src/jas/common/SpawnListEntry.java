package jas.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandomItem;

public class SpawnListEntry extends WeightedRandomItem{
    public final Class<? extends EntityLiving> livingClass;
    public final int packSize;

    public SpawnListEntry(Class<? extends EntityLiving> livingClass, int weight, int packSize) {
        super(weight);
        this.livingClass = livingClass;
        this.packSize = packSize;
    }
    
    public LivingHandler getLivingHandler(){
        return CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass);
    }
}
