package jas.common.spawner.creature.entry;

import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.Properties;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.keys.Key;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsSpawnListSpawning;

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
    /* Auto-refactoring Fails, but pckgName refers to String identifier that represents the BiomeGroup or StructureGroup */
    public final String pckgName;
    public final int minChunkPack;
    public final int maxChunkPack;
    public final String optionalParameters;
    protected OptionalSettingsSpawnListSpawning spawning;

    public OptionalSettingsSpawnListSpawning getOptionalSpawning() {
        return spawning;
    }

    public static final String SpawnListCategoryComment = "Editable Format: SpawnWeight" + DefaultProps.DELIMETER
            + "SpawnPackSize" + DefaultProps.DELIMETER + "MinChunkPackSize" + DefaultProps.DELIMETER
            + "MaxChunkPackSize";

    public SpawnListEntry(Class<? extends EntityLiving> livingClass, String pckgName, int weight, int packSize,
            int minChunkPack, int maxChunkPack, String optionalParameters) {
        super(weight);
        this.livingClass = livingClass;
        this.packSize = packSize;
        this.pckgName = pckgName;
        this.minChunkPack = minChunkPack;
        this.maxChunkPack = maxChunkPack;
        this.optionalParameters = optionalParameters;

        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsSpawnListSpawning(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsSpawnListSpawning("") : spawning;
    }

    public LivingHandler getLivingHandler() {
        return CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass);
    }

    public static void setupConfigCategory(Configuration config) {
        ConfigCategory category = config.getCategory("CreatureSettings.SpawnListEntry".toLowerCase(Locale.ENGLISH));
        category.setComment(SpawnListEntry.SpawnListCategoryComment);
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
                + Integer.toString(maxChunkPack) + optionalParameters;
        Property resultValue = getSpawnEntryProperty(config, defaultValue);

        String[] resultMasterParts = resultValue.getString().split("\\{", 2);
        String[] resultParts = resultMasterParts[0].split("\\" + DefaultProps.DELIMETER);
        if (resultParts.length == 4) {
            int resultSpawnWeight = ParsingHelper.parseFilteredInteger(resultParts[0], packSize, "spawnWeight");
            int resultPackSize = ParsingHelper.parseFilteredInteger(resultParts[1], packSize, "packSize");
            int resultMinChunkPack = ParsingHelper.parseFilteredInteger(resultParts[2], packSize, "minChunkPack");
            int resultMaxChunkPack = ParsingHelper.parseFilteredInteger(resultParts[3], packSize, "maxChunkPack");
            String optionalParameters = resultMasterParts.length == 2 ? "{" + resultMasterParts[1] : "";
            return new SpawnListEntry(livingClass, pckgName, resultSpawnWeight, resultPackSize, resultMinChunkPack,
                    resultMaxChunkPack, optionalParameters);
        } else {
            JASLog.severe(
                    "SpawnListEntry %s was invalid. Data is being ignored and loaded with default settings %s, %s",
                    mobName, packSize, itemWeight);
            resultValue.set(defaultValue);
            return new SpawnListEntry(livingClass, pckgName, itemWeight, packSize, minChunkPack, maxChunkPack, "");
        }
    }

    public void saveToConfig(Configuration config) {
        String defaultValue = Integer.toString(itemWeight) + DefaultProps.DELIMETER + Integer.toString(packSize)
                + DefaultProps.DELIMETER + Integer.toString(minChunkPack) + DefaultProps.DELIMETER
                + Integer.toString(maxChunkPack);
        Property resultValue = getSpawnEntryProperty(config, defaultValue);
        resultValue.set(defaultValue);
    }

    private Property getSpawnEntryProperty(Configuration config, String defaultValue) {
        String mobName = (String) EntityList.classToStringMapping.get(livingClass);

        Property resultValue;
        String categoryKey;
        if (Properties.savedSortCreatureByBiome) {
            categoryKey = "CreatureSettings.SpawnListEntry." + pckgName;
            resultValue = config.get(categoryKey, mobName, defaultValue);
        } else {
            categoryKey = "CreatureSettings.SpawnListEntry." + mobName;
            resultValue = config.get(categoryKey, pckgName, defaultValue);
        }
        ConfigCategory category = config.getCategory(categoryKey.toLowerCase(Locale.ENGLISH));
        category.setComment(SpawnListCategoryComment);
        return resultValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pckgName == null) ? 0 : pckgName.hashCode());
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
        if (pckgName == null) {
            if (other.pckgName != null)
                return false;
        } else if (!pckgName.equals(other.pckgName))
            return false;
        if (livingClass == null) {
            if (other.livingClass != null)
                return false;
        } else if (!livingClass.equals(other.livingClass))
            return false;
        return true;
    }
}
