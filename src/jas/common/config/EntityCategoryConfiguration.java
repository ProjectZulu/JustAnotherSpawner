package jas.common.config;

import jas.common.DefaultProps;
import jas.common.WorldProperties;

import java.io.File;
import java.util.Locale;

import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.base.Optional;

public class EntityCategoryConfiguration extends Configuration {

    public static final String configName = "CreatureType.cfg";
    public static final String configCategory = "LivingType";

    public EntityCategoryConfiguration(File configDirectory, WorldProperties worldProperties) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName + "/" + configName));
    }

    public Property getCustomCategories(String categoryString) {
        return this.get("Extra Categories", "Additions", categoryString,
                "Format: category name seperated by commas i.e. <CategoryName1>,<CategoryName2>");
    }

    public Property getSpawnRate(String typeID, int spawnRate) {
        return this.get(configCategory + "." + typeID, "Spawn Rate", spawnRate);
    }

    public Property getSpawnCap(String typeID, int maxNumberOfCreature) {
        return this.get(configCategory + "." + typeID, "Creature Spawn Cap", maxNumberOfCreature);
    }

    public Property getDefaultBiomeCap(String typeID, int defaultBiomeCap) {
        return this.get(configCategory + "." + typeID, "Default Biome Cap", defaultBiomeCap,
                "Biome Caps are disabled if default is equal to -1");
    }

    public ConfigCategory getBiomeCaps(String typeID) {
        ConfigCategory category = getCategory((configCategory + "." + typeID + "." + "biomecaps").toLowerCase());
        category.setComment("Format biomeMapping=Cap. See BiomeGroups.cfg for BiomeMappings.");
        return category;
    }

    public Optional<Boolean> isChunkSpawningPresent(String typeID) {
        ConfigCategory category = getCategory(configCategory.toLowerCase(Locale.ENGLISH) + "."
                + typeID.toLowerCase(Locale.ENGLISH));
        String propertyKey = "Do Chunk Spawning";
        if (category.containsKey(propertyKey)) {
            Property prop = category.get(propertyKey);
            Optional<Boolean> result = Optional.of(prop.getBoolean(false));
            category.remove(propertyKey);
            return result;
        }
        return Optional.absent();
    }

    /*
     * ChunkSpawning boolean is replaced with float chunkSpawnChance. This is maintained to read and convert old configs
     */
    @Deprecated
    public Property getChunkSpawning(String typeID, Boolean chunkSpawning) {
        return this.get(configCategory + "." + typeID, "Do Chunk Spawning", chunkSpawning);
    }

    /* String instead of float is used so that we can format the very imprecise float to a user-friendly digits */
    public Property getChunkSpawnChance(String typeID, String chunkSpawnChance) {
        return this.get(configCategory + "." + typeID, "Chunk Spawn Chance", chunkSpawnChance);
    }

    public Property getOptionalTags(String typeID, String optionalParameters) {
        return this.get(configCategory + "." + typeID, "Optional Tags", optionalParameters);
    }
}
