package jas.common.spawner.creature.handler;

import jas.common.FileUtilities;
import jas.common.GsonHelper;
import jas.common.ImportedSpawnList;
import jas.common.JASLog;
import jas.common.MVELHelper;
import jas.common.WorldProperties;
import jas.common.spawner.Tags;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;

import org.mvel2.MVEL;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.gson.Gson;

public class LivingHandlerRegistry {
	/* Mapping from GroupID to LivingHandler */
	private ImmutableMap<String, LivingHandler> livingHandlers;

	/* Map from a Content entry (JASName) to LivingHandler Key */
	private ImmutableMultimap<String, String> jasNameToHandler = ImmutableSetMultimap.<String, String> builder()
			.build();

	public LivingHandler getLivingHandler(String handlerID) {
		return livingHandlers.get(handlerID);
	}

	public List<LivingHandler> getLivingHandlers(String jasName) {
		List<LivingHandler> list = new ArrayList<LivingHandler>();
		for (String handlerID : jasNameToHandler.get(jasName)) {
			LivingHandler handler = livingHandlers.get(handlerID);
			if (handler != null) {
				list.add(handler);
			}
		}
		return list;
	}

	public List<LivingHandler> getLivingHandlers(Class<? extends EntityLiving> entityClass) {
		List<LivingHandler> list = new ArrayList<LivingHandler>();
		String jasName = livingGroupRegistry.EntityClasstoJASName.get(entityClass);
		return getLivingHandlers(jasName);
	}

	public Class<? extends EntityLiving> getRandomEntity(String livingID, Random random, Tags tags) {
		LivingHandler livingHandler = getLivingHandler(livingID);
		if (livingHandler.compEntityExpression.isPresent()) {
			String result = MVELHelper.typedExecuteExpression(String.class, livingHandler.compEntityExpression.get(),
					tags, "Error processing compiled entityExpression expression for " + livingHandler.livingID
							+ livingID + ": " + livingHandler.entityExpression);

			Class<? extends EntityLiving> entityClass = livingGroupRegistry.JASNametoEntityClass.get(result);
			if (entityClass == null) {
				JASLog.log().severe("MVEL expression %s yeiled entity mapping %s which does not exist",
						livingHandler.entityExpression, result);
			}
			return entityClass;
		} else if (!livingHandler.namedJASSpawnables.isEmpty()) {
			int selectedEntry = random.nextInt(livingHandler.namedJASSpawnables.size());
			int i = 0;
			for (String jasName : livingHandler.namedJASSpawnables) {
				if (i++ == selectedEntry) {
					return livingGroupRegistry.JASNametoEntityClass.get(jasName);
				}
			}
		}
		return null;
	}

	/**
	 * Creates a Immutable copy of registered livinghandlers
	 * 
	 * @return Immutable copy of Collection of SpawnListEntries
	 */
	public Collection<LivingHandler> getLivingHandlers() {
		return livingHandlers.values();
	}

	public Collection<String> getLivingHandlerKeys() {
		return livingHandlers.keySet();
	}

	private CreatureTypeRegistry creatureTypeRegistry;
	private LivingGroupRegistry livingGroupRegistry;
	private WorldProperties worldProperties;

	public LivingHandlerRegistry(LivingGroupRegistry livingGroupRegistry, CreatureTypeRegistry creatureTypeRegistry,
			WorldProperties worldProperties) {
		this.livingGroupRegistry = livingGroupRegistry;
		this.creatureTypeRegistry = creatureTypeRegistry;
		this.worldProperties = worldProperties;
	}

	/**
	 * Loads settings from Configuration. Currently loaded settings will be lost.
	 * 
	 * @param configDirectory
	 * @param world
	 */
	public void loadFromConfig(File configDirectory, World world, ImportedSpawnList spawnList) {
		Set<LivingHandler> livingHandlers = new HashSet<LivingHandler>();
		Gson gson = GsonHelper.createGson(false, new Type[] { LivingHandlerSaveObject.class },
				new Object[] { new LivingHandlerSaveObject.Serializer() });
		File handlerFileFolder = LivingHandler.getFile(configDirectory,
				worldProperties.getFolderConfiguration().saveName, "");
		File[] files = FileUtilities.getFileInDirectory(handlerFileFolder, ".cfg");
		for (File livingFile : files) {
			LivingHandlerSaveObject read = GsonHelper.readOrCreateFromGson(
					FileUtilities.createReader(livingFile, false), LivingHandlerSaveObject.class, gson);
			if (read.getHandlers().isPresent()) {
				for (LivingHandlerBuilder builder : read.getHandlers().get()) {
					if (isHandlerValid(builder.getHandlerId(), livingGroupRegistry)) {
						LivingHandler handler = builder.build(creatureTypeRegistry, livingGroupRegistry);
						livingHandlers.add(handler);
					}
				}
			}
		}

		//TODO Change this to instead of all mappings, only newly added mappings
		Collection<String> mappings = livingGroupRegistry.jasNametoEntityClass().keySet();
		for (String mapping : mappings) {
			LivingHandlerBuilder builder = new LivingHandlerBuilder(mapping, guessCreatureTypeOfGroup(world, spawnList, mapping));
			builder.contents.add(mapping);
			livingHandlers.add(builder.build(creatureTypeRegistry, livingGroupRegistry));
		}
		ImmutableMap.Builder<String, LivingHandler> handlerBuilder = ImmutableMap.<String, LivingHandler> builder();
		ImmutableSetMultimap.Builder<String, String> jasNameBuilder = ImmutableSetMultimap.<String, String>builder();
		for (LivingHandler handler : livingHandlers) {
			handlerBuilder.put(handler.livingID, handler);
			for (String jasName : handler.namedJASSpawnables) {
				jasNameBuilder.put(jasName, handler.livingID);
			}
		}
		this.livingHandlers = handlerBuilder.build();
		this.jasNameToHandler = jasNameBuilder.build();
	}

	// This used to check if LivingGroup was declared before building the LivingHandler, this may be unneccesary now
	private boolean isHandlerValid(String handlerId, LivingGroupRegistry registry) {
		return handlerId != null && !handlerId.trim().equals("");
	}

	private String getSaveFileName(String groupID) {
		boolean universalCFG = worldProperties.getSavedFileConfiguration().universalDirectory;
		if (universalCFG) {
			return "Universal";
		} else {
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
	}

	/**
	 * Determines the Default JAS Living Type from the Vanilla EnumCreatureType
	 * 
	 * @param livingClass
	 * @return
	 */
	private String guessCreatureTypeOfGroup(World world, ImportedSpawnList spawnList, String... entityJASNames) {
		/* Find entity and inquire as to type */
		for (String jasName : entityJASNames) {
			Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass.get(jasName);
			EntityLiving creature = LivingHelper.createCreature(livingClass, world);
			for (EnumCreatureType type : EnumCreatureType.values()) {
				boolean isType = creature != null ? creature.isCreatureType(type, true) : type.getClass()
						.isAssignableFrom(livingClass);
				if (isType && creatureTypeRegistry.getCreatureType(type.toString()) != null) {
					return type.toString();
				}
			}
		}
		/* If entity doesnt have type, Search for matching spawnlist and assign type equivalent to Spawnlist */
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null) {
				for (EnumCreatureType creatureType : EnumCreatureType.values()) {
					for (SpawnListEntry entry : spawnList.getSpawnableCreatureList(biome, creatureType)) {
						for (String jasName : entityJASNames) {
							Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass
									.get(jasName);
							if (entry.entityClass.equals(livingClass)) {
								CreatureType type = creatureTypeRegistry.getCreatureType(creatureType.toString());
								if (type != null) {
									return type.typeID;
								}
							}
						}
					}
				}
			}
		}
		return CreatureTypeRegistry.NONE;
	}

	public void saveToConfig(File configDirectory) {
		worldProperties.saveToConfig(configDirectory);
		HashMap<String, HashMap<String, LivingHandlerBuilder>> fileNameToHandlerIdToHandler = new HashMap<String, HashMap<String, LivingHandlerBuilder>>();
		for (LivingHandler handler : livingHandlers.values()) {
			String saveName = getSaveFileName(handler.livingID);
			HashMap<String, LivingHandlerBuilder> idToHandler = fileNameToHandlerIdToHandler.get(saveName);
			if (idToHandler == null) {
				idToHandler = new HashMap<String, LivingHandlerBuilder>();
				fileNameToHandlerIdToHandler.put(saveName, idToHandler);
			}
			idToHandler.put(handler.livingID, new LivingHandlerBuilder(handler));
		}

		Gson gson = GsonHelper.createGson(true, new Type[] { LivingHandlerSaveObject.class },
				new Object[] { new LivingHandlerSaveObject.Serializer() });
		for (Entry<String, HashMap<String, LivingHandlerBuilder>> entry : fileNameToHandlerIdToHandler.entrySet()) {
			File livingfile = LivingHandler.getFile(configDirectory, worldProperties.getFolderConfiguration().saveName,
					entry.getKey());
			GsonHelper.writeToGson(FileUtilities.createWriter(livingfile, true), new LivingHandlerSaveObject(entry
					.getValue().values()), gson);
		}
	}

	public void addLivingHandler(LivingHandlerBuilder builder) {
		LivingHandler handler = builder.build(creatureTypeRegistry, livingGroupRegistry);
		HashMap<String, LivingHandler> map = new HashMap<String, LivingHandler>(livingHandlers);
		if (!map.containsKey(handler.livingID)) {
			map.put(handler.livingID, handler);
		}
		livingHandlers = ImmutableMap.<String, LivingHandler> builder().putAll(map).build();
	}

	public void removeLivingHandler(LivingHandlerBuilder builder) {
		removeLivingHandler(builder.getHandlerId());
	}

	public void removeLivingHandler(String livingID) {
		HashMap<String, LivingHandler> map = new HashMap<String, LivingHandler>(livingHandlers);
		if (map.remove(livingID) != null) {
			livingHandlers = ImmutableMap.<String, LivingHandler> builder().putAll(map).build();
		}
	}

	public void updateLivingHandler(LivingHandlerBuilder builder) {
		LivingHandler handler = builder.build(creatureTypeRegistry, livingGroupRegistry);
		HashMap<String, LivingHandler> map = new HashMap<String, LivingHandler>(livingHandlers);
		map.remove(handler.livingID);
		map.put(handler.livingID, handler);
		livingHandlers = ImmutableMap.<String, LivingHandler> builder().putAll(map).build();
	}
}