package jas.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class LivingHandler {
    public final Class<? extends EntityLiving> entityClass;
    public final CreatureType creatureType;
    public final boolean useModLocationCheck;
    public final boolean shouldSpawn;
    
    public LivingHandler(Class<? extends EntityLiving> entityClass, CreatureType creatureType, boolean useModLocationCheck, boolean shouldSpawn) {
        this.entityClass = entityClass;
        this.useModLocationCheck = useModLocationCheck;
        this.creatureType = creatureType;
        this.shouldSpawn = shouldSpawn;
    }
    
    /**
     * Replacement Method for EntitySpecific isCreatureType. Allows Handler specific 
     * @param entity
     * @param creatureType
     * @return
     */
    public boolean isEntityOfType(Entity entity, CreatureType creatureType) {
        return creatureType.equals(creatureType);
    }
    
    /**
     * Replacement Method for EntitySpecific getCanSpawnHere(). Allows Handler to Override Creature functionality. This
     * allows a Modder handler to be written independently of the modded creature.
     * 
     * @return True if location is valid For entity to spawn, false otherwise
     */
    public final boolean getCanSpawnHere(EntityLiving entity, CreatureType spawnType) {
        if (useModLocationCheck) {
            return entity.getCanSpawnHere();
        } else {
            return isValidLocation(entity);
        }
    }

    /**
     * Alternative getCanSpawnHere independent of the Entity. By default this provides a way for End-Users to Skip the
     * EntitySpecific check implemented by Modders while keeping the generic bounding box style checks in EntityLiving
     * 
     * @param entity
     * @return True if location is valid For entity to spawn, false otherwise
     */
    protected boolean isValidLocation(EntityLiving entity) {
        return entity.worldObj.checkIfAABBIsClear(entity.boundingBox)
                && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty()
                && !entity.worldObj.isAnyLiquid(entity.boundingBox);
    }
}
