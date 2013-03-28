package jas.common;

import java.util.Locale;

import net.minecraft.entity.EntityLiving;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class CreatureHandlerConfiguration {

    public static LivingHandler getLivingHandlerSettings(Configuration config, Class<? extends EntityLiving> livingClass, String fullMobName) {
        String creatureTypeID = CreatureTypeRegistry.NONE;
        boolean useModLocationCheck = true;
        boolean shouldSpawn = false;

        String defaultUseModCheck = Boolean.toString(useModLocationCheck);
        String defaultShouldSpawn = Boolean.toString(shouldSpawn);
        
        
        String handlerString = creatureTypeID + "." + defaultUseModCheck + "." + defaultShouldSpawn;
        Property handleProperty = config.get("CreatureSettings.LivingHandler", fullMobName, handlerString);
        String[] resultParts = handleProperty.getString().split("\\.");
        if (resultParts.length == 3) {
            String resultCreatureType = resultParts[0];
            if(!CreatureTypeRegistry.NONE.equalsIgnoreCase(resultCreatureType) && CreatureTypeRegistry.INSTANCE.getCreatureType(resultCreatureType) == null){
                JASLog.warning(
                        "Error parsing entry %s. CreatureType of %s was unreadable. Value will be set to default %s",
                        fullMobName, resultCreatureType, creatureTypeID);
                resultCreatureType = creatureTypeID;
            }
            String resultLocationCheck = resultParts[1];
            if(!resultLocationCheck.equalsIgnoreCase("true") && !resultLocationCheck.equalsIgnoreCase("false")){
                JASLog.warning(
                        "Error parsing entry %s. UseModLocationCheck of %s was unreadable. Value will be set to default %s",
                        fullMobName, resultCreatureType, defaultUseModCheck);
                resultLocationCheck = defaultUseModCheck;
            }
            String resultShouldSpawn = resultParts[2];
            if(!resultShouldSpawn.equalsIgnoreCase("true") && !resultShouldSpawn.equalsIgnoreCase("false")){
                JASLog.warning(
                        "Error parsing entry %s. ShouldSpawn of %s was unreadable. Value will be set to default %s",
                        fullMobName, resultCreatureType, defaultUseModCheck);
                resultShouldSpawn = defaultUseModCheck;
            }

            return new LivingHandler(livingClass, resultCreatureType, Boolean.parseBoolean(resultLocationCheck),
                    Boolean.parseBoolean(resultShouldSpawn));

        } else {
            JASLog.severe(
                    "LivingHandler Entry %s was invalid. Data is being ignored and loaded with default settings %s, %s, %s",
                    fullMobName, creatureTypeID, defaultUseModCheck, defaultShouldSpawn);
            return new LivingHandler(livingClass, creatureTypeID, useModLocationCheck, shouldSpawn);
        }
    }
    
    /**
     * Used to Initialize Categeory Comments
     */
    public static void setupCategories(Configuration config) {
        ConfigCategory category = config.getCategory("CreatureSettings.LivingHandler".toLowerCase(Locale.ENGLISH));
        category.setComment("Editable Format: CreatureType.UseModLocationCheck.ShouldSpawn");        
    }
}
