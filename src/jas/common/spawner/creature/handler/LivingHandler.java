package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.Properties;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.logging.Level;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class LivingHandler {
    public final Class<? extends EntityLiving> entityClass;
    public final String creatureTypeID;
    public final boolean shouldSpawn;
    public final String optionalParameters;
    protected OptionalSettingsSpawning spawning;
    protected OptionalSettingsDespawning despawning;

    public OptionalSettingsDespawning getDespawning() {
        return despawning;
    }

    public LivingHandler(Class<? extends EntityLiving> entityClass, String creatureTypeID, boolean shouldSpawn,
            String optionalParameters) {
        this.entityClass = entityClass;
        this.creatureTypeID = CreatureTypeRegistry.INSTANCE.getCreatureType(creatureTypeID) != null ? creatureTypeID
                : CreatureTypeRegistry.NONE;
        this.shouldSpawn = shouldSpawn;
        this.optionalParameters = optionalParameters;

        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (titletag.equals(Key.spawn.key) || titletag.equals(Key.notSpawn.key)) {
                spawning = new OptionalSettingsSpawning(parsed);
            } else if (titletag.equals(Key.despawn.key) || titletag.equals(Key.notDespawn.key)) {
                despawning = new OptionalSettingsDespawning(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsSpawning("") : spawning;
        despawning = despawning == null ? new OptionalSettingsDespawning("") : despawning;
    }

    public final LivingHandler toCreatureTypeID(String creatureTypeID) {
        return constructInstance(entityClass, creatureTypeID, shouldSpawn, optionalParameters);
    }

    public final LivingHandler toShouldSpawn(boolean shouldSpawn) {
        return constructInstance(entityClass, creatureTypeID, shouldSpawn, optionalParameters);
    }

    public final LivingHandler toOptionalParameters(String optionalParameters) {
        return constructInstance(entityClass, creatureTypeID, shouldSpawn, optionalParameters);
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
    protected LivingHandler constructInstance(Class<? extends EntityLiving> entityClass, String creatureTypeID,
            boolean shouldSpawn, String optionalParameters) {
        return new LivingHandler(entityClass, creatureTypeID, shouldSpawn, optionalParameters);
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
     * @return True if location is valid For entity to spawn, false otherwise
     */
    public final boolean getCanSpawnHere(EntityLiving entity) {
        if (!spawning.isOptionalEnabled()) {
            return isValidLocation(entity, CreatureTypeRegistry.INSTANCE.getCreatureType(creatureTypeID));
        } else {
            return isValidLocation(entity);
        }
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

            boolean canDespawn = !despawning.isInverted();
            if (!despawning.isValidLocation(entity.worldObj, xCoord, yCoord, zCoord)) {
                canDespawn = despawning.isInverted();
            }

            if (canDespawn == false) {
                LivingHelper.setAge(entityplayer, 0);
                return;
            }

            boolean validDistance = despawning.isMidDistance((int) d3, Properties.despawnDist);
            boolean isOfAge = despawning.isValidAge(entity.getAge(), Properties.minDespawnTime);
            boolean instantDespawn = despawning.isMaxDistance((int) d3, Properties.maxDespawnDist);

            if (instantDespawn) {
                entity.setDead();
            } else if (isOfAge && entity.worldObj.rand.nextInt(1 + despawning.getRate() / 3) == 0 && validDistance) {
                JASLog.debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s", entity.getEntityName(),
                        entity.getAge(), despawning.getRate());
                entity.setDead();
            } else if (!validDistance) {
                LivingHelper.setAge(entityplayer, 0);
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
    protected boolean isValidLocation(EntityLiving entity, CreatureType spawnType) {
        return entity.getCanSpawnHere();
    }

    /**
     * Alternative getCanSpawnHere independent of the Entity. By default this provides a way for End-Users to Skip the
     * EntitySpecific check implemented by Modders while keeping the generic bounding box style checks in EntityLiving
     * 
     * @param entity
     * @return True if location is valid For entity to spawn, false otherwise
     */
    private final boolean isValidLocation(EntityLiving entity) {
        int xCoord = MathHelper.floor_double(entity.posX);
        int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
        int zCoord = MathHelper.floor_double(entity.posZ);

        boolean canSpawn = !spawning.isInverted();

        if (!spawning.isValidLocation(entity.worldObj, xCoord, yCoord, zCoord)) {
            canSpawn = spawning.isInverted();
        }

        return canSpawn && entity.worldObj.checkNoEntityCollision(entity.boundingBox)
                && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty()
                && !entity.worldObj.isAnyLiquid(entity.boundingBox);
    }

    /**
     * Creates a new instance of this from configuration using itself as the default
     * 
     * @param config
     * @return
     */
    protected LivingHandler createFromConfig(Configuration config) {
        String mobName = (String) EntityList.classToStringMapping.get(entityClass);

        String defaultValue = creatureTypeID.toUpperCase() + DefaultProps.DELIMETER + Boolean.toString(shouldSpawn)
                + optionalParameters;

        Property resultValue = config.get("CreatureSettings.LivingHandler", mobName, defaultValue);

        String[] resultMasterParts = resultValue.getString().split("\\{", 2);
        String[] resultParts = resultMasterParts[0].split("\\" + DefaultProps.DELIMETER);

        if (resultParts.length == 4) {
            String resultCreatureType = ParsingHelper.parseCreatureTypeID(resultParts[0], creatureTypeID,
                    "creatureTypeID");
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
            String resultCreatureType = ParsingHelper.parseCreatureTypeID(resultParts[0], creatureTypeID,
                    "creatureTypeID");
            boolean resultShouldSpawn = ParsingHelper.parseBoolean(resultParts[1], shouldSpawn, "ShouldSpawn");
            LivingHandler resultHandler = this.toCreatureTypeID(resultCreatureType).toShouldSpawn(resultShouldSpawn);
            return resultMasterParts.length == 2 ? resultHandler.toOptionalParameters("{" + resultMasterParts[1])
                    : resultHandler;
        } else {
            JASLog.severe(
                    "LivingHandler Entry %s was invalid. Data is being ignored and loaded with default settings %s, %s",
                    mobName, creatureTypeID, shouldSpawn);
            resultValue.set(defaultValue);
            return new LivingHandler(entityClass, creatureTypeID, shouldSpawn, "");
        }
    }
}
