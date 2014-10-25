package jas.common.spawner.creature.handler;

import jas.common.JASLog;
import jas.common.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
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
			return instantiateEntity(livingClass, world);
		} catch (NoClassDefFoundError exception) {
			JASLog.log().severe("Entity %s references classes that do not exist.", livingClass.getSimpleName());
		} catch (Exception exception) {
			JASLog.log().warning("Entity %s could not be initialized.", livingClass.getSimpleName());
		}
		return null;
	}

    public static <T extends Entity> T instantiateEntity(Class<? extends T> entityClass, World world)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        return entityClass.getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
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
    public static void setAge(EntityLivingBase entity, int value) {
        try {
            ReflectionHelper
                    .setCatchableFieldUsingReflection("field_70708_bq", EntityLivingBase.class, entity, true, value);
        } catch (NoSuchFieldException e) {
            ReflectionHelper.setFieldUsingReflection("entityAge", EntityLivingBase.class, entity, true, value);
        }
    }

    /**
     * Check the entity to see if it can despawn
     * 
     * @param livingClass Class of Desired Creature
     * @return
     */
    public static boolean canDespawn(EntityLivingBase entity) {
        return (Boolean) ReflectionHelper.invokeMethod("canDespawn", "func_70692_ba", entity);
    }
}
