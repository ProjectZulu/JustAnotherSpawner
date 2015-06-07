package jas.spawner.refactor;

import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.configsloader.LivingHandlerLoader;
import jas.spawner.refactor.configsloader.LivingSettingsLoader;
import jas.spawner.refactor.entities.Group.ReversibleGroups;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;
import jas.spawner.refactor.entities.ListContentGroup;
import jas.spawner.refactor.entities.LivingAttributes;
import jas.spawner.refactor.entities.LivingGroupBuilder;
import jas.spawner.refactor.entities.LivingGroupBuilder.LivingGroup;
import jas.spawner.refactor.entities.LivingHandlerBuilder;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.entities.LivingMappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

public class LivingHandlers implements ReversibleGroups<LivingHandler> {
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

	public LivingHandlers(ConfigLoader loader, LivingMappings mappings, LivingAttributes attributes) {
		loadFromConfig(loader, mappings, attributes);
	}

	public void loadFromConfig(ConfigLoader loader, LivingMappings mappings, LivingAttributes attributes) {
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

		ImmutableMapGroupsBuilder<LivingHandler> livingHandlers = new ImmutableMapGroupsBuilder<LivingHandler>(key);
		Builder<String, String> mappingToGroupIDBuilder = ImmutableListMultimap.<String, String> builder();
		for (LivingHandlerBuilder builder : livingBuilders.iDToGroup().values()) {
			ListContentGroup.Parser.parseGroupContents(builder, mappings, attributes);
			livingHandlers.addGroup(builder.build());
			for (String mapping : builder.results()) {
				mappingToGroupIDBuilder.put(mapping, builder.iD());
			}
		}
		this.livingHandlers = livingHandlers.build();
		this.mappingToGroupID = mappingToGroupIDBuilder.build();
	}

	public void saveToConfig(WorldProperties worldProperties, ConfigLoader loader, LivingMappings mappings,
			LivingAttributes attributes) {
		Collection<LivingGroupBuilder> livGrpBuilders = new ArrayList<LivingGroupBuilder>();
		for (LivingGroup attribute : attributes.iDToGroup().values()) {
			livGrpBuilders.add(new LivingGroupBuilder(attribute));
		}
		loader.livingGroupLoader = new LoadedFile(new LivingSettingsLoader(mappings.keyToMapping(), livGrpBuilders));

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
			return LivingHelper.guessModID(groupID);
		}
	}
}
