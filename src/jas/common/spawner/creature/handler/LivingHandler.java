package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.EntityProperties;
import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.keys.Key;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsDespawning;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsSpawning;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.Locale;
import java.util.logging.Level;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class LivingHandler {

    public static final String LivingHandlerCategoryComment = "Editable Format: CreatureType" + DefaultProps.DELIMETER
            + "ShouldSpawn" + "{TAG1:PARAM1,value:PARAM2,value}{TAG2:PARAM1,value:PARAM2,value}";

    public final String groupID;
    public final String creatureTypeID;
    public final boolean shouldSpawn;
    public final String optionalParameters;
    public final CreatureTypeRegistry creatureTypeRegistry;
    protected OptionalSettingsSpawning spawning;
    protected OptionalSettingsDespawning despawning;

    public OptionalSettingsDespawning getDespawning() {
        return despawning;
    }

    public LivingHandler(CreatureTypeRegistry creatureTypeRegistry, String livingGroupID, String creatureTypeID,
            boolean shouldSpawn, String optionalParameters) {
        this.creatureTypeRegistry = creatureTypeRegistry;
        this.groupID = livingGroupID;
        this.creatureTypeID = creatureTypeRegistry.getCreatureType(creatureTypeID) != null ? creatureTypeID
                : CreatureTypeRegistry.NONE;
        this.shouldSpawn = shouldSpawn;
        this.optionalParameters = optionalParameters;

        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsSpawning(parsed);
            } else if (Key.despawn.keyParser.isMatch(titletag)) {
                despawning = new OptionalSettingsDespawning(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsSpawning("") : spawning;
        despawning = despawning == null ? new OptionalSettingsDespawning("") : despawning;
    }

    public final LivingHandler toCreatureTypeID(String creatureTypeID) {
        return constructInstance(groupID, creatureTypeID, shouldSpawn, optionalParameters);
    }

    public final LivingHandler toShouldSpawn(boolean shouldSpawn) {
        return constructInstance(groupID, creatureTypeID, shouldSpawn, optionalParameters);
    }

    public final LivingHandler toOptionalParameters(String optionalParameters) {
        return constructInstance(groupID, creatureTypeID, shouldSpawn, optionalParameters);
    }

    /**
     * Used internally to create a new Instance of LivingHandler. MUST be Overriden by Subclasses so that they are not
     * replaced with Parent. Used to Allow subclasses to Include their own Logic, but maintain same data structure.
     * 
     * Should create a new instance of class using parameters provided in the constructor.
     * 
     * @param typeID
     * @param maxNumberOfCreature
     * @param spawnMedium
     * @param spawnRate
     * @param chunkSpawning
     */
    protected LivingHandler constructInstance(String livingGroupID, String creatureTypeID, boolean shouldSpawn,
            String optionalParameters) {
        return new LivingHandler(creatureTypeRegistry, livingGroupID, creatureTypeID, shouldSpawn, optionalParameters);
    }

    public final int getLivingCap() {
        Integer cap = spawning.getEntityCap();
        return cap != null ? cap : 0;
    }

    /**
     * Replacement Method for EntitySpecific getCanSpawnHere(). Allows Handler to Override Creature functionality. This
     * both ensures that a Modder can implement their own logic indepenently of the modded creature and that end users
     * are allowed to customize their experience
     * 
     * @param entity Entity being Spawned
     * @param spawnListEntry SpawnListEntry the Entity belongs to
     * @return True if location is valid For entity to spawn, false otherwise
     */
    public final boolean getCanSpawnHere(EntityLiving entity, SpawnListEntry spawnListEntry) {
        boolean canLivingSpawn = isValidLiving(entity);
        boolean canSpawnListSpawn = isValidSpawnList(entity, spawnListEntry);

        if (spawning.getOperand() == Operand.AND || spawnListEntry.getOptionalSpawning().getOperand() == Operand.AND) {
            return canLivingSpawn && canSpawnListSpawn;
        } else {
            return canLivingSpawn || canSpawnListSpawn;
        }
    }

    /**
     * Evaluates if this Entity in its current location / state would be capable of despawning eventually
     */
    public final boolean canDespawn(EntityLiving entity) {
        if (!getDespawning().isOptionalEnabled()) {
            return LivingHelper.canDespawn(entity);
        }
        EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
        int xCoord = MathHelper.floor_double(entity.posX);
        int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
        int zCoord = MathHelper.floor_double(entity.posZ);

        if (entityplayer != null) {
            double d0 = entityplayer.posX - entity.posX;
            double d1 = entityplayer.posY - entity.posY;
            double d2 = entityplayer.posZ - entity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            boolean canDespawn = !despawning.isInverted();
            if (!despawning.isValidLocation(entity.worldObj, entity, xCoord, yCoord, zCoord)) {
                canDespawn = despawning.isInverted();
            }

            if (canDespawn == false) {
                return false;
            }

            boolean validDistance = despawning.isMidDistance((int) d3, JustAnotherSpawner.worldSettings()
                    .worldProperties().despawnDist);
            boolean instantDespawn = despawning.isMaxDistance((int) d3, JustAnotherSpawner.worldSettings()
                    .worldProperties().maxDespawnDist);

            if (instantDespawn) {
                return true;
            } else if (validDistance) {
                return true;
            } else if (!validDistance) {
                return false;
            }
        }
        return false;
    }

    /**
     * Called by Despawn to Manually Attempt to Despawn Entity
     * 
     * @param entity
     */
    public final void despawnEntity(EntityLiving entity) {
        EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
        int xCoord = MathHelper.floor_double(entity.posX);
        int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
        int zCoord = MathHelper.floor_double(entity.posZ);

        LivingHelper.setPersistenceRequired(entity, true);
        if (entityplayer != null) {
            double d0 = entityplayer.posX - entity.posX;
            double d1 = entityplayer.posY - entity.posY;
            double d2 = entityplayer.posZ - entity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            EntityProperties entityProps = (EntityProperties) entity
                    .getExtendedProperties(EntityProperties.JAS_PROPERTIES);
            entityProps.incrementAge(60);

            boolean canDespawn = !despawning.isInverted();
            if (!despawning.isValidLocation(entity.worldObj, entity, xCoord, yCoord, zCoord)) {
                canDespawn = despawning.isInverted();
            }

            if (canDespawn == false) {
                entityProps.resetAge();
                return;
            }

            boolean validDistance = despawning.isMidDistance((int) d3, JustAnotherSpawner.worldSettings()
                    .worldProperties().despawnDist);
            boolean isOfAge = despawning.isValidAge(entityProps.getAge(), JustAnotherSpawner.worldSettings()
                    .worldProperties().minDespawnTime);
            boolean instantDespawn = despawning.isMaxDistance((int) d3, JustAnotherSpawner.worldSettings()
                    .worldProperties().maxDespawnDist);

            if (instantDespawn) {
                entity.setDead();
            } else if (isOfAge && entity.worldObj.rand.nextInt(1 + despawning.getRate() / 3) == 0 && validDistance) {
                JASLog.debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s", entity.getEntityName(),
                        entityProps.getAge(), despawning.getRate());
                entity.setDead();
            } else if (!validDistance) {
                entityProps.resetAge();
            }
        }
    }

    /**
     * Represents the 'Modders Choice' for Creature SpawnLocation.
     * 
     * @param entity
     * @param spawnType
     * @return
     */
    protected boolean isValidLocation(EntityLiving entity) {
        return entity.getCanSpawnHere();
    }

    public final boolean isValidLiving(EntityLiving entity) {
        if (!spawning.isOptionalEnabled()) {
            return isValidLocation(entity);
        }

        int xCoord = MathHelper.floor_double(entity.posX);
        int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
        int zCoord = MathHelper.floor_double(entity.posZ);

        boolean canLivingSpawn = !spawning.isInverted();
        if (!spawning.isValidLocation(entity.worldObj, entity, xCoord, yCoord, zCoord)) {
            canLivingSpawn = spawning.isInverted();
        }

        return canLivingSpawn && entity.worldObj.checkNoEntityCollision(entity.boundingBox)
                && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty();
    }

    public final boolean isValidSpawnList(EntityLiving entity, SpawnListEntry spawnListEntry) {
        if (!spawnListEntry.getOptionalSpawning().isOptionalEnabled()) {
            return false;
        }

        int xCoord = MathHelper.floor_double(entity.posX);
        int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
        int zCoord = MathHelper.floor_double(entity.posZ);

        boolean canSpawnListSpawn = !spawnListEntry.getOptionalSpawning().isInverted();
        if (!spawnListEntry.getOptionalSpawning().isValidLocation(entity.worldObj, entity, xCoord, yCoord, zCoord)) {
            canSpawnListSpawn = spawnListEntry.getOptionalSpawning().isInverted();
        }

        return canSpawnListSpawn && entity.worldObj.checkNoEntityCollision(entity.boundingBox)
                && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty();
    }

    public static void setupConfigCategory(Configuration config) {
        ConfigCategory category = config.getCategory("CreatureSettings.LivingHandler".toLowerCase(Locale.ENGLISH));
        category.setComment(LivingHandler.LivingHandlerCategoryComment);
    }

    /**
     * Creates a new instance of this from configuration using itself as the default
     * 
     * @param config
     * @return
     */
    protected LivingHandler createFromConfig(LivingConfiguration config) {
        // String mobName = (String) EntityList.classToStringMapping.get(entityClass);

        String defaultValue = creatureTypeID.toUpperCase() + DefaultProps.DELIMETER + Boolean.toString(shouldSpawn)
                + optionalParameters;

        Property resultValue = config.getLivingHandler(groupID, defaultValue);

        String[] resultMasterParts = resultValue.getString().split("\\{", 2);
        String[] resultParts = resultMasterParts[0].split("\\" + DefaultProps.DELIMETER);

        if (resultParts.length == 4) {
            /* Legacy Converter To Convert Old Format Remove as of 1.0.0 or as soon as it becomes burdensome */
            String resultCreatureType = ParsingHelper.parseCreatureTypeID(creatureTypeRegistry, resultParts[0],
                    creatureTypeID, "creatureTypeID");
            boolean resultShouldSpawn = ParsingHelper.parseBoolean(resultParts[1], shouldSpawn, "ShouldSpawn");
            boolean resultForceDespawn = ParsingHelper.parseBoolean(resultParts[2], false, "forceDespawn");
            boolean resultLocationCheck = ParsingHelper.parseBoolean(resultParts[3], true, "LocationCheck");

            String resultString = resultCreatureType + "-" + resultShouldSpawn;
            if (resultLocationCheck == false) {
                resultString = resultString.concat("{spawn}");
            }
            if (resultForceDespawn == true) {
                resultString = resultString.concat("{despawn}");
            }
            resultValue.set(resultString);
            LivingHandler resultHandler = this.toCreatureTypeID(resultCreatureType).toShouldSpawn(resultShouldSpawn);
            return resultMasterParts.length == 2 ? resultHandler.toOptionalParameters("{"
                    + resultValue.getString().split("\\{", 2)[1]) : resultHandler;
        } else if (resultParts.length == 2) {
            String resultCreatureType = ParsingHelper.parseCreatureTypeID(creatureTypeRegistry, resultParts[0],
                    creatureTypeID, "creatureTypeID");
            boolean resultShouldSpawn = ParsingHelper.parseBoolean(resultParts[1], shouldSpawn, "ShouldSpawn");
            LivingHandler resultHandler = this.toCreatureTypeID(resultCreatureType).toShouldSpawn(resultShouldSpawn);
            return resultMasterParts.length == 2 ? resultHandler.toOptionalParameters("{" + resultMasterParts[1])
                    : resultHandler;
        } else {
            JASLog.severe(
                    "LivingHandler Entry %s was invalid. Data is being ignored and loaded with default settings %s, %s",
                    groupID, creatureTypeID, shouldSpawn);
            resultValue.set(defaultValue);
            return new LivingHandler(creatureTypeRegistry, groupID, creatureTypeID, shouldSpawn, "");
        }
    }

    public void saveToConfig(LivingConfiguration config) {
        String currentValue = creatureTypeID.toUpperCase() + DefaultProps.DELIMETER + Boolean.toString(shouldSpawn)
                + optionalParameters;
        config.getLivingHandler(groupID, currentValue).set(currentValue);
    }
}
