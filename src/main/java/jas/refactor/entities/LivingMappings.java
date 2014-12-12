package jas.refactor.entities;

import jas.common.JASLog;
import jas.refactor.ConfigLoader.ConfigLoader;
import jas.refactor.ConfigLoader.EntityGroupingLoader;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import org.apache.logging.log4j.Level;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

public class LivingMappings implements Mappings {
	private final HashMap<String, String> entityPackageToPrefix;
	private final String UNKNOWN_PREFIX;
	{
		UNKNOWN_PREFIX = "unknown";
		entityPackageToPrefix = new HashMap<String, String>(5);
		entityPackageToPrefix.put("net.minecraft.entity.monster", "");
		entityPackageToPrefix.put("net.minecraft.entity.passive", "");
		entityPackageToPrefix.put("net.minecraft.entity.boss", "");
	}

	private List<String> newMappings;
	private ImmutableBiMap<Class<? extends EntityLiving>, String> EntityClasstoJASName;
	private ImmutableBiMap<String, Class<? extends EntityLiving>> JASNametoEntityClass;

	public LivingMappings(ConfigLoader loader) {
		loadFromConfig(loader);
	}

	private void loadFromConfig(ConfigLoader loader) {
		EntityGroupingLoader entityGroupLoader = loader.livingGroupLoader.saveObject;
		List<String> newJASNames = new ArrayList<String>();
		BiMap<Class<? extends EntityLiving>, String> entityClassToJASNameBuilder = HashBiMap.create();
		for (Entry<String, String> entry : entityGroupLoader.fmlToJASName.entrySet()) {
			Class<?> entityClass = (Class<?>) EntityList.stringToClassMapping.get(entry.getKey());
			if (entityClass == null || !EntityLiving.class.isAssignableFrom(entityClass)
					|| Modifier.isAbstract(entityClass.getModifiers())) {
				JASLog.log().warning("Read entity %s does not correspond to an valid FML entry entry", entry.getKey());
				continue;
			}
			@SuppressWarnings("unchecked")
			Class<? extends EntityLiving> livingClass = (Class<? extends EntityLiving>) entityClass;
			entityClassToJASNameBuilder.put(livingClass, entry.getValue());
		}
		@SuppressWarnings("unchecked")
		Set<Entry<Class<?>, String>> fmlNames = EntityList.classToStringMapping.entrySet();
		HashMap<Class<? extends EntityLiving>, String> processedEntitiesAndJASNames = new HashMap<Class<? extends EntityLiving>, String>();
		for (Entry<Class<?>, String> entry : fmlNames) {
			if (!EntityLiving.class.isAssignableFrom(entry.getKey())
					|| Modifier.isAbstract(entry.getKey().getModifiers())) {
				continue;
			}
			@SuppressWarnings("unchecked")
			Class<? extends EntityLiving> livingClass = (Class<? extends EntityLiving>) entry.getKey();
			String jasName;
			if (entry.getValue().contains(".")) {
				jasName = entry.getValue();
			} else {
				String prefix = guessPrefix(entry.getKey(), fmlNames);
				jasName = prefix.trim().equals("") ? entry.getValue() : prefix + "." + entry.getValue();
			}
			if (processedEntitiesAndJASNames.containsKey(livingClass)) {
				JASLog.log().severe(
						"Duplicate entity class detected. Ignoring FML,JasName pair [%s,%s] for pair [%s, %s]",
						livingClass, jasName, livingClass, processedEntitiesAndJASNames.get(livingClass));
			} else if (processedEntitiesAndJASNames.values().contains(jasName)) {
				JASLog.log().severe(
						"Duplicate entity mapping detected. Ignoring FML,JasName pair [%s,%s] for pair [%s, %s]",
						livingClass, jasName, livingClass, processedEntitiesAndJASNames.get(livingClass));
			} else {
				JASLog.log()
						.debug(Level.INFO, "Found new mapping FML,JasName pair [%s,%s] ", entry.getValue(), jasName);
				newJASNames.add(jasName);
				processedEntitiesAndJASNames.put(livingClass, jasName);
				entityClassToJASNameBuilder.forcePut(livingClass, jasName);
			}
		}
	}

	/**
	 * Attempts to Guess the prefix an entity should have.
	 */
	private String guessPrefix(Class<?> entity, Set<Entry<Class<?>, String>> fmlNames) {
		String currentPackage = entity.getName();

		if (currentPackage.lastIndexOf(".") != -1) {
			currentPackage = currentPackage.substring(0, currentPackage.lastIndexOf("."));
		}

		for (Entry<Class<?>, String> entry : fmlNames) {
			String packageName = entry.getKey().getName();
			if (packageName.lastIndexOf(".") != -1) {
				packageName = packageName.substring(0, packageName.lastIndexOf("."));
			}

			if (currentPackage.equals(packageName) && entry.getValue().contains(".")) {
				return entry.getValue().split("\\.")[0];
			}
		}
		String manualPrefix = entityPackageToPrefix.get(currentPackage);
		if (manualPrefix != null) {
			return manualPrefix;
		}
		String[] currentParts = currentPackage.split("\\.");
		return currentParts.length > 1 ? currentParts[0] : UNKNOWN_PREFIX;
	}

	@Override
	public Collection<String> newMappings() {
		return newMappings;
	}

	@Override
	public ImmutableBiMap<Class<? extends EntityLiving>, String> entityClasstoJASName() {
		return EntityClasstoJASName;
	}

	@Override
	public ImmutableBiMap<String, Class<? extends EntityLiving>> jASNametoEntityClass() {
		return JASNametoEntityClass;
	}
}