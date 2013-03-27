package jas.common;

import net.minecraft.entity.EntityLiving;

public class LivingHandler {
    public final Class<? extends EntityLiving> entityClass;
    public final int packSize;
    public final boolean useModLocationCheck;

    public LivingHandler(Class<? extends EntityLiving> entityClass, int packSize, boolean useModLocationCheck) {
        this.entityClass = entityClass;
        this.packSize = packSize;
        this.useModLocationCheck = useModLocationCheck;
    }

    /**
     * Replacement Method for EntitySpecific getCanSpawnHere(). Allows Handler to Override
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
