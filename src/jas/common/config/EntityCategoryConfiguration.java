package jas.common.config;

import jas.common.DefaultProps;
import jas.common.Properties;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class EntityCategoryConfiguration extends Configuration {

    public static final String configName = "CreatureType.cfg";
    public static final String configCategory = "LivingType";

    public EntityCategoryConfiguration(File configDirectory) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + Properties.saveName + "/" + configName));
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

    public Property getChunkSpawning(String typeID, boolean chunkSpawning) {
        return this.get(configCategory + "." + typeID, "Do Chunk Spawning", chunkSpawning);
    }

    public Property getOptionalTags(String typeID, String optionalParameters) {
        return this.get(configCategory + "." + typeID, "Optional Tags", optionalParameters);
    }
}
