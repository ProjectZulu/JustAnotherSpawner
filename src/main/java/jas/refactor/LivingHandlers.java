package jas.refactor;

import jas.common.ImportedSpawnList;
import jas.common.WorldProperties;
import jas.refactor.ConfigLoader.ConfigLoader;
import jas.refactor.ConfigLoader.ConfigLoader.LoadedFile;
import jas.refactor.ConfigLoader.EntityGroupingLoader;
import jas.refactor.ConfigLoader.LivingHandlerLoader;
import jas.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.refactor.entities.Group.Groups;
import jas.refactor.entities.Group.ReversibleGroups;
import jas.refactor.entities.Group;
import jas.refactor.entities.ImmutableMapGroupsBuilder;
import jas.refactor.entities.LivingAttributes;
import jas.refactor.entities.LivingGroupBuilder;
import jas.refactor.entities.LivingGroupBuilder.LivingGroup;
import jas.refactor.entities.LivingHandlerBuilder;
import jas.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.refactor.entities.LivingMappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.world.World;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

public class LivingHandlers implements ReversibleGroups {
	/* Mapping from GroupID to LivingHandler */
	private ImmutableMap<String, LivingHandler> livingHandlers;
	private ImmutableListMultimap<String, String> mappingToGroupID;

	public LivingHandler getLivingHandler(String handlerID) {
		return livingHandlers.get(handlerID);
	}

	public static String key = "G|";

	@Override
	public String key() {
		return key;
	}

	@Override
	public Map<String, LivingHandler> iDToGroup() {
		return livingHandlers;
	}

	@Override
	public ImmutableMultimap<String, String> mappingToID() {
		return mappingToGroupID;
	}

	private LivingMappings mappings;
	private LivingAttributes attributes;

	public LivingMappings livingMappings() {
		return mappings;
	}

	public LivingAttributes livingAttributes() {
		return attributes;
	}

	/** BiomeSpawnLists is added so that LivingHandlers can pass on changes to its dependency */
	private BiomeSpawnLists biomeRegistry;

	public LivingHandlers(BiomeSpawnLists biomeRegistry) {
		this.biomeRegistry = biomeRegistry;
	}

	public void loadFromConfig(ConfigLoader loader, World world, ImportedSpawnList spawnList) {
		mappings = new LivingMappings(loader);
		attributes = new LivingAttributes(loader, mappings);

		// Set<LivingHandlerBuilder> livingHandlers = new HashSet<LivingHandlerBuilder>();
		ImmutableMapGroupsBuilder<LivingHandlerBuilder> livingBuilders = new ImmutableMapGroupsBuilder<LivingHandlerBuilder>(
				key);
		for (Entry<String, LoadedFile<LivingHandlerLoader>> entry : loader.livingHandlerLoaders.entrySet()) {
			LivingHandlerLoader handlerLoader = entry.getValue().saveObject;
			if (handlerLoader.getHandlers().isPresent()) {
				for (LivingHandlerBuilder builder : handlerLoader.getHandlers().get()) {
					if (builder.getLivingHandlerID() != null && !builder.getLivingHandlerID().trim().equals("")) {
						livingBuilders.addGroup(builder);
					}
				}
			}
		}

		/* Add new mappings as LivingHandler */
		for (String mapping : mappings.newMappings()) {
			livingBuilders.addGroup(new LivingHandlerBuilder(mapping));
		}

		List<LivingHandlerBuilder> sortedHandlers = Group.Sorter.getSortedGroups(livingBuilders);
		livingBuilders.clear();
		ImmutableMapGroupsBuilder<LivingHandler> livingHandlers = new ImmutableMapGroupsBuilder<LivingHandler>(key);
		Builder<String, String> mappingToGroupIDBuilder = ImmutableListMultimap.<String, String> builder();
		for (LivingHandlerBuilder builder : sortedHandlers) {
			Group.Parser.parseGroupContents(builder, mappings, livingBuilders);
			livingBuilders.addGroup(builder);
			livingHandlers.addGroup(builder.build());
			for (String mapping : builder.results()) {
				mappingToGroupIDBuilder.put(mapping, builder.iD());
			}
		}
		this.livingHandlers = livingHandlers.build();
		this.mappingToGroupID = mappingToGroupIDBuilder.build();
	}

	public void saveToConfig(WorldProperties worldProperties, ConfigLoader loader) {
		Collection<LivingGroupBuilder> livGrpBuilders = new ArrayList<LivingGroupBuilder>();
		for (LivingGroup attribute : attributes.iDToGroup().values()) {
			livGrpBuilders.add(new LivingGroupBuilder(attribute));
		}
		loader.livingGroupLoader = new LoadedFile(new EntityGroupingLoader(mappings.keyToMapping(), livGrpBuilders));

		Map<String, Collection<LivingHandlerBuilder>> fileNameToLivingHandlers = new HashMap<String, Collection<LivingHandlerBuilder>>();
		for (LivingHandler handler : livingHandlers.values()) {
			String saveName = getSaveFileName(worldProperties, handler.livingHandlerID);
			Collection<LivingHandlerBuilder> idToHandler = fileNameToLivingHandlers.get(saveName);
			if (idToHandler == null) {
				idToHandler = new HashSet<LivingHandlerBuilder>();
				fileNameToLivingHandlers.put(saveName, idToHandler);
			}
			idToHandler.add(new LivingHandlerBuilder(handler));
		}

		Map<String, LoadedFile<LivingHandlerLoader>> livingHandlerLoaders = new HashMap<String, LoadedFile<LivingHandlerLoader>>();
		loader.livingHandlerLoaders = new HashMap<String, ConfigLoader.LoadedFile<LivingHandlerLoader>>();
		for (String fileName : fileNameToLivingHandlers.keySet()) {
			loader.livingHandlerLoaders.put(fileName, new LoadedFile<LivingHandlerLoader>(new LivingHandlerLoader(
					fileNameToLivingHandlers.get(fileName))));
		}
	}

	private String getSaveFileName(WorldProperties worldProperties, String groupID) {
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
}
