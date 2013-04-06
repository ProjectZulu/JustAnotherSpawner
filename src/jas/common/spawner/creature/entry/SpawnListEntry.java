package jas.common.spawner.creature.entry;

import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.Properties;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingRegsitryHelper;

import java.util.Locale;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandomItem;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

/**
 * Every SpawnListEntry is assumed Unique for a EntityLivingClass given biome Spawn.
 * 
 * It should be noted that Technically, f(Class, Biome, CreatureType) --> SpawnList, but since f(Class) --> CreatureType
 * then f(Class, Biome) --> SpawnList
 */
// TODO: Large Constructor could probably use Factory / Or Split packSize into Its Own Immutable Class
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

    /**
     * Creates a new instance of this from configuration using itself as the default
     * 
     * @param config
     * @return
     */
    public SpawnListEntry createFromConfig(Configuration config) {
        String mobName = (String) EntityList.classToStringMapping.get(livingClass);

        String defaultValue = Integer.toString(itemWeight) + DefaultProps.DELIMETER + Integer.toString(packSize)
                + DefaultProps.DELIMETER + Integer.toString(minChunkPack) + DefaultProps.DELIMETER
                + Integer.toString(maxChunkPack);

        Property resultValue;
        String categoryKey;
        if (Properties.sortCreatureByBiome) {
            categoryKey = "CreatureSettings.SpawnListEntry." + biomeName;
            resultValue = config.get(categoryKey, mobName, defaultValue);
        } else {
            categoryKey = "CreatureSettings.SpawnListEntry." + mobName;
            resultValue = config.get(categoryKey, biomeName, defaultValue);
        }
        ConfigCategory category = config.getCategory(categoryKey.toLowerCase(Locale.ENGLISH));
        category.setComment(CreatureHandlerRegistry.SpawnListCategoryComment);

        String[] resultParts = resultValue.getString().split("\\" + DefaultProps.DELIMETER);
        if (resultParts.length == 4) {
            int resultSpawnWeight = LivingRegsitryHelper.parseInteger(resultParts[0], packSize, "spawnWeight");
            int resultPackSize = LivingRegsitryHelper.parseInteger(resultParts[1], packSize, "packSize");
            int resultMinChunkPack = LivingRegsitryHelper.parseInteger(resultParts[2], packSize, "minChunkPack");
            int resultMaxChunkPack = LivingRegsitryHelper.parseInteger(resultParts[3], packSize, "maxChunkPack");
            return new SpawnListEntry(livingClass, biomeName, resultSpawnWeight, resultPackSize, resultMinChunkPack,
                    resultMaxChunkPack);
        } else {
            JASLog.severe(
                    "SpawnListEntry %s was invalid. Data is being ignored and loaded with default settings %s, %s",
                    mobName, packSize, itemWeight);
            resultValue.set(defaultValue);
            return new SpawnListEntry(livingClass, biomeName, itemWeight, packSize, minChunkPack, maxChunkPack);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((biomeName == null) ? 0 : biomeName.hashCode());
        result = prime * result + ((livingClass == null) ? 0 : livingClass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpawnListEntry other = (SpawnListEntry) obj;
        if (biomeName == null) {
            if (other.biomeName != null)
                return false;
        } else if (!biomeName.equals(other.biomeName))
            return false;
        if (livingClass == null) {
            if (other.livingClass != null)
                return false;
        } else if (!livingClass.equals(other.livingClass))
            return false;
        return true;
    }
}
