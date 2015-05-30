package jas.spawner.refactor;

import jas.common.JASLog;
import jas.common.helper.ReflectionHelper;
import jas.spawner.refactor.entities.LivingMappings;

import java.lang.reflect.InvocationTargetException;

import com.google.common.base.CharMatcher;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
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

	public static Class<? extends EntityLiving> getEntityfromFMLName(String fmlName) {
		return (Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(fmlName);
	}

	public static Class<? extends EntityLiving> getEntityfromJASName(String jasName, LivingMappings mappings) {
		return getEntityfromFMLName(mappings.mappingToKey().get(jasName));
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
			ReflectionHelper.setCatchableFieldUsingReflection("field_70708_bq", EntityLivingBase.class, entity, true,
					value);
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

	/**
	 * Extract SaveFileName (Typically modID) from name of a Group. Usually an EntityID of some sort in the form of
	 * modID.name i.e ProjectZulu.Armadillo
	 * 
	 * "." is used as the delimeter
	 * 
	 * @param groupID i.e. The name to extract the modID from: LivingHandlerID
	 * @return
	 */
	public static String guessModID(String groupID) {
		String modID;
		String[] mobNameParts = groupID.split("\\.");
		if (mobNameParts.length >= 2) {
			String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
			modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
		} else {
			modID = "Vanilla";
		}
		return modID;
	}

	public static String guessVanillaLivingType(World world, Class<? extends EntityLiving> livingClass) {
		EntityLiving entity = createCreature(livingClass, world);
		for (EnumCreatureType type : EnumCreatureType.values()) {
			if (entity.isCreatureType(type, true)) {
				return type.toString().toUpperCase();
			}
		}
		return LivingTypes.NONE;
	}
}
