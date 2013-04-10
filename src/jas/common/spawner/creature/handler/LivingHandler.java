package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class LivingHandler {
    public final Class<? extends EntityLiving> entityClass;
    public final String creatureTypeID;
    @Deprecated
    public final boolean useModLocationCheck;
    public final boolean shouldSpawn;
    @Deprecated
    public final boolean forceDespawn;

    public final String optionalParameters;
    protected OptionalSettingsSpawning spawning;
    protected OptionalSettingsDespawning despawning;

    public OptionalSettingsDespawning getDespawning() {
        return despawning;
    }

    public LivingHandler(Class<? extends EntityLiving> entityClass, String creatureTypeID, boolean useModLocationCheck,
            boolean shouldSpawn, boolean forceDespawn, String optionalParameters) {
        this.entityClass = entityClass;
        this.useModLocationCheck = useModLocationCheck;
        this.creatureTypeID = CreatureTypeRegistry.INSTANCE.getCreatureType(creatureTypeID) != null ? creatureTypeID
                : CreatureTypeRegistry.NONE;
        this.shouldSpawn = shouldSpawn;
        this.forceDespawn = forceDespawn;
        this.optionalParameters = optionalParameters;

        for (String string : optionalParameters.split("\\{")) {
            if (string.replace("}", "").split("\\:", 2)[0].equalsIgnoreCase("spawn")) {
                spawning = new OptionalSettingsSpawning(string);
            } else if (string.replace("}", "").split("\\:", 2)[0].equalsIgnoreCase("despawn")) {
                despawning = new OptionalSettingsDespawning(string);
            }
        }
    }

    public final LivingHandler toCreatureTypeID(String creatureTypeID) {
        return constructInstance(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn,
                optionalParameters);
    }

    public final LivingHandler toUseModLocationCheck(boolean useModLocationCheck) {
        return constructInstance(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn,
                optionalParameters);
    }

    public final LivingHandler toShouldSpawn(boolean shouldSpawn) {
        return constructInstance(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn,
                optionalParameters);
    }

    public final LivingHandler toForceDespawn(boolean forceDespawn) {
        return constructInstance(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn,
                optionalParameters);
    }

    public final LivingHandler toOptionalParameters(String optionalParameters) {
        return constructInstance(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn,
                optionalParameters);
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
            boolean useModLocationCheck, boolean shouldSpawn, boolean forceDespawn, String optionalParameters) {
        return new LivingHandler(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn,
                optionalParameters);
    }

    /**
     * Replacement Method for EntitySpecific isCreatureType. Allows Handler specific
     * 
     * @param entity
     * @param creatureType
     * @return
     */
    public boolean isEntityOfType(Entity entity, CreatureType creatureType) {
        return creatureTypeID.equals(CreatureTypeRegistry.NONE) ? false : CreatureTypeRegistry.INSTANCE
                .getCreatureType(creatureTypeID).equals(creatureType);
    }

    public boolean isEntityOfType(Class<? extends EntityLiving> entity, CreatureType creatureType) {
        return creatureTypeID.equals(CreatureTypeRegistry.NONE) ? false : CreatureTypeRegistry.INSTANCE
                .getCreatureType(creatureTypeID).equals(creatureType);
    }

    /**
     * Replacement Method for EntitySpecific getCanSpawnHere(). Allows Handler to Override Creature functionality. This
     * both ensures that a Modder can implement their own logic indepenently of the modded creature and that end users
     * are allowed to customize their experience
     * 
     * @return True if location is valid For entity to spawn, false otherwise
     */
    public final boolean getCanSpawnHere(EntityLiving entity) {
        if (!spawning.overrideLocationCheck()) {
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

            boolean canDespawn = true;
            if (!despawning.isValidLightLevel(entity.worldObj, xCoord, yCoord, zCoord)
                    || !despawning.isValidBlock(entity.worldObj, xCoord, yCoord, zCoord)) {
                canDespawn = false;
            }

            if (canDespawn && entity.getAge() > 600 && entity.worldObj.rand.nextInt(1 + despawning.getRate() / 3) == 0
                    && d3 >= 1024.0D) {
                JASLog.debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s", entity.getEntityName(), entity.getAge(),
                        despawning.getRate());
                entity.setDead();
            } else if (d3 < 1024.0D) {
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

        if (!spawning.isValidBlock(entity.worldObj.getBlockId(xCoord, yCoord - 1, zCoord),
                entity.worldObj.getBlockMetadata(xCoord, yCoord - 1, zCoord))) {
            return false;
        } else if (!spawning.isValidLightLevel(entity.worldObj.getSavedLightValue(EnumSkyBlock.Sky, xCoord, yCoord,
                zCoord))) {
            return false;
        } else {
            return entity.worldObj.checkIfAABBIsClear(entity.boundingBox)
                    && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty()
                    && !entity.worldObj.isAnyLiquid(entity.boundingBox);
        }
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
                + DefaultProps.DELIMETER + Boolean.toString(forceDespawn) + DefaultProps.DELIMETER
                + Boolean.toString(useModLocationCheck);

        Property resultValue = config.get("CreatureSettings.LivingHandler", mobName, defaultValue);

        String[] resultMasterParts = resultValue.getString().split("\\{", 2);
        String[] resultParts = resultMasterParts[0].split("\\" + DefaultProps.DELIMETER);

        if (resultParts.length == 4) {
            String resultCreatureType = ParsingHelper.parseCreatureTypeID(resultParts[0], creatureTypeID,
                    "creatureTypeID");
            boolean resultShouldSpawn = ParsingHelper.parseBoolean(resultParts[1], shouldSpawn, "ShouldSpawn");
            boolean resultForceDespawn = ParsingHelper.parseBoolean(resultParts[2], forceDespawn, "forceDespawn");
            boolean resultLocationCheck = ParsingHelper.parseBoolean(resultParts[3], useModLocationCheck,
                    "LocationCheck");
            LivingHandler resultHandler = this.toCreatureTypeID(resultCreatureType)
                    .toUseModLocationCheck(resultLocationCheck).toShouldSpawn(resultShouldSpawn)
                    .toForceDespawn(resultForceDespawn);
            return resultMasterParts.length == 2 ? resultHandler.toOptionalParameters(resultMasterParts[1])
                    : resultHandler;
        } else {
            JASLog.severe(
                    "LivingHandler Entry %s was invalid. Data is being ignored and loaded with default settings %s, %s, %s, %s",
                    mobName, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn);
            resultValue.set(defaultValue);
            return new LivingHandler(entityClass, creatureTypeID, useModLocationCheck, shouldSpawn, forceDespawn, "");
        }
    }
}
