package jas.common.spawner.creature.handler;

import jas.common.JASLog;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class LivingHelper {

    /**
     * Create Instance of Creature Class
     * 
     * @param livingClass Class of Desired Creature
     * @param worldServer Instance of World
     * @return
     */
    public static EntityLiving createCreature(Class<? extends EntityLiving> livingClass, World world) {
        try {
            return livingClass.getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
        } catch (Exception exception) {
            JASLog.warning("Entity %s could not be initialized. Default defaults will be provided for it.",
                    livingClass.getSimpleName());
        }
        return null;
    }

    /**
     * Set Persistence Required of the instance of Entity Provided
     * 
     * @param livingClass Class of Desired Creature
     * @param worldServer Instance of World
     * @return
     */
    public static void setPersistenceRequired(EntityLiving entity, boolean value) {
        try {
            ReflectionHelper
                    .setCatchableFieldUsingReflection("field_82179_bU", EntityLiving.class, entity, true, value);
        } catch (NoSuchFieldException e) {
            ReflectionHelper.setFieldUsingReflection("persistenceRequired", EntityLiving.class, entity, true, value);
        }
    }

    /**
     * Set Persistence Required of the instance of Entity Provided
     * 
     * @param livingClass Class of Desired Creature
     * @param worldServer Instance of World
     * @return
     */
    public static void setAge(EntityLiving entity, int value) {
        try {
            ReflectionHelper
                    .setCatchableFieldUsingReflection("field_82179_bU", EntityLiving.class, entity, true, value);
        } catch (NoSuchFieldException e) {
            ReflectionHelper.setFieldUsingReflection("entityAge", EntityLiving.class, entity, true, value);
        }
    }
}
