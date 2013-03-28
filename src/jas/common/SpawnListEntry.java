package jas.common;

import net.minecraft.util.WeightedRandomItem;

public class SpawnListEntry extends WeightedRandomItem{
    public final String livingHandlerID;
    public final int packSize;

    public SpawnListEntry(String livingHandlerID, int weight, int packSize) {
        super(weight);
        this.livingHandlerID = livingHandlerID;
        this.packSize = packSize;
    }
    
    public LivingHandler getLivingHandler(){
        return CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingHandlerID);
    }
}
