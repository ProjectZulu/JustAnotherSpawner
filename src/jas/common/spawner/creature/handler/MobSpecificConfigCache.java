package jas.common.spawner.creature.handler;

import jas.common.WorldProperties;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.creature.entry.SpawnListEntry;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.common.Configuration;

import com.google.common.base.CharMatcher;

public class MobSpecificConfigCache {
    /* Map of Entity modID to their Respective Configuration File, cleared immediately After Saving */
    private final HashMap<String, LivingConfiguration> modConfigCache = new HashMap<String, LivingConfiguration>();

    private WorldProperties worldProperties;

    public MobSpecificConfigCache(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    public LivingConfiguration getLivingEntityConfig(File configDir, String groupID) {
        boolean universalCFG = worldProperties.savedUniversalDirectory;
        if (universalCFG) {
            if (modConfigCache.get(worldProperties.saveName + "Universal") == null) {
                LivingConfiguration config = new LivingConfiguration(configDir, "Universal", worldProperties);
                config.load();
                LivingHandler.setupConfigCategory(config);
                SpawnListEntry.setupConfigCategory(config);
                modConfigCache.put(worldProperties.saveName + "Universal", config);
                return config;
            }
            return modConfigCache.get(worldProperties.saveName + "Universal");
        } else {
            String modID;
            String[] mobNameParts = groupID.split("\\.");
            if (mobNameParts.length >= 2) {
                String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
                modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
            } else {
                modID = "Vanilla";
            }

            if (modConfigCache.get(worldProperties.saveName + modID) == null) {
                LivingConfiguration config = new LivingConfiguration(configDir, modID, worldProperties);
                config.load();
                LivingHandler.setupConfigCategory(config);
                SpawnListEntry.setupConfigCategory(config);
                modConfigCache.put(worldProperties.saveName + modID, config);
            }
            return modConfigCache.get(worldProperties.saveName + modID);
        }
    }

    public void saveAndCloseConfigs() {
        for (Configuration config : modConfigCache.values()) {
            config.save();
        }
        modConfigCache.clear();
    }
}
