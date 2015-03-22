package jas.spawner.legacy.spawner.creature.handler;

import jas.common.JASLog;
import jas.spawner.legacy.EntityProperties;
import jas.spawner.legacy.TAGProfile;
import jas.spawner.legacy.spawner.creature.entry.SpawnListEntry;
import jas.spawner.legacy.spawner.creature.handler.parsing.keys.Key;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettingsDespawning;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettingsPostSpawning;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettingsSpawning;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.spawner.legacy.spawner.creature.type.CreatureTypeRegistry;
import jas.spawner.modern.DefaultProps;

import java.io.File;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.ForgeEventFactory;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.eventhandler.Event.Result;

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
    protected OptionalSettingsPostSpawning postspawning;

    public OptionalSettingsDespawning getDespawning() {
        return despawning;
    }

    public LivingHandler(CreatureTypeRegistry creatureTypeRegistry, LivingHandlerBuilder builder) {
        this.creatureTypeRegistry = creatureTypeRegistry;
        this.groupID = builder.getHandlerId();
        this.creatureTypeID = creatureTypeRegistry.getCreatureType(builder.getCreatureTypeId()) != null ? builder
                .getCreatureTypeId() : CreatureTypeRegistry.NONE;
        this.shouldSpawn = builder.getShouldSpawn();
        this.optionalParameters = builder.getOptionalParameters();
        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsSpawning(parsed);
            } else if (Key.despawn.keyParser.isMatch(titletag)) {
                despawning = new OptionalSettingsDespawning(parsed);
            } else if (Key.postspawn.keyParser.isMatch(titletag)) {
                postspawning = new OptionalSettingsPostSpawning(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsSpawning("") : spawning;
        despawning = despawning == null ? new OptionalSettingsDespawning("") : despawning;
        postspawning = postspawning == null ? new OptionalSettingsPostSpawning("") : postspawning;
    }

    @Deprecated
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
            } else if (Key.postspawn.keyParser.isMatch(titletag)) {
                postspawning = new OptionalSettingsPostSpawning(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsSpawning("") : spawning;
        despawning = despawning == null ? new OptionalSettingsDespawning("") : despawning;
        postspawning = postspawning == null ? new OptionalSettingsPostSpawning("") : postspawning;
    }

    @Deprecated
    public final LivingHandler toCreatureTypeID(String creatureTypeID) {
        return constructInstance(groupID, creatureTypeID, shouldSpawn, optionalParameters);
    }

    @Deprecated
    public final LivingHandler toShouldSpawn(boolean shouldSpawn) {
        return constructInstance(groupID, creatureTypeID, shouldSpawn, optionalParameters);
    }

    @Deprecated
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
    @Deprecated
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
		Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, entity.worldObj, (int) entity.posX,
				(int) entity.posY, (int) entity.posZ);
		if ((canSpawn == Result.ALLOW || canSpawn == Result.DENY)
				&& !(spawning.isOptionalEnabled() || spawnListEntry.getOptionalSpawning().isOptionalEnabled())) {
			return canSpawn == Result.ALLOW;
		} else {
			if (spawning.getOperand() == Operand.AND
					|| spawnListEntry.getOptionalSpawning().getOperand() == Operand.AND) {
				return canLivingSpawn && canSpawnListSpawn;
			} else {
				return canLivingSpawn || canSpawnListSpawn;
			}
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

            boolean instantDespawn = despawning.isMaxDistance((int) d3, TAGProfile.worldSettings()
                    .worldProperties().getGlobal().maxDespawnDist);

            if (instantDespawn) {
                return true;
            } else {
                return true;
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

            boolean validDistance = despawning.isMidDistance((int) d3, TAGProfile.worldSettings()
                    .worldProperties().getGlobal().despawnDist);
            boolean isOfAge = despawning.isValidAge(entityProps.getAge(), TAGProfile.worldSettings()
                    .worldProperties().getGlobal().minDespawnTime);
            boolean instantDespawn = despawning.isMaxDistance((int) d3, TAGProfile.worldSettings()
                    .worldProperties().getGlobal().maxDespawnDist);

            if (instantDespawn) {
                entity.setDead();
            } else if (isOfAge && entity.worldObj.rand.nextInt(1 + despawning.getRate() / 3) == 0 && validDistance) {
                JASLog.log().debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s", entity.getCommandSenderName(),
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

    public final void postSpawnEntity(EntityLiving entity, SpawnListEntry spawnListEntry) {
        if (postspawning.isOptionalEnabled()) {
            int xCoord = MathHelper.floor_double(entity.posX);
            int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
            int zCoord = MathHelper.floor_double(entity.posZ);

            postspawning.isValidLocation(entity.worldObj, entity, xCoord, yCoord, zCoord);
        }

        if (spawnListEntry.getOptionalPostSpawning().isOptionalEnabled()) {
            int xCoord = MathHelper.floor_double(entity.posX);
            int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
            int zCoord = MathHelper.floor_double(entity.posZ);

            spawnListEntry.getOptionalPostSpawning().isValidLocation(entity.worldObj, entity, xCoord, yCoord, zCoord);
        }
    }

    public static File getFile(File configDirectory, String saveName, String fileName) {
        String filePath = DefaultProps.WORLDSETTINGSDIR + saveName + "/" + DefaultProps.ENTITYHANDLERDIR;
        if (fileName != null && !fileName.equals("")) {
            filePath = filePath.concat(fileName).concat(".cfg");
        }
        return new File(configDirectory, filePath);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LivingHandler other = (LivingHandler) obj;
        return groupID.equals(other.groupID);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
        return result;
    }
}
