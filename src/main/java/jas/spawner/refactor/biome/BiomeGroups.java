package jas.spawner.refactor.biome;

import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.configsloader.BiomeGroupLoader;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.Group.Parser.ExpressionContext;
import jas.spawner.refactor.entities.Group.ReversibleGroups;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;

public class BiomeGroups implements ReversibleGroups {
	private ImmutableMap<String, BiomeGroup> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;
	public static String key = "G.";

	@Override
	public String key() {
		return key;
	}

	@Override
	public Map<String, BiomeGroup> iDToGroup() {
		return iDToGroup;
	}

	/** Reverse Look-up Map to Get All Attributes a Particular Biome is In */
	@Override
	public ImmutableMultimap<String, String> mappingToID() {
		return mappingToGroupID;
	}

	public BiomeGroups(ConfigLoader loader, BiomeMappings biomeMappings, Groups attributes, Groups dictionary) {
		loadFromConfig(loader, biomeMappings, attributes, dictionary);
	}

	private void loadFromConfig(ConfigLoader loader, BiomeMappings biomeMappings, Groups attributes, Groups dictionary) {
		BiomeGroupLoader savedStats = loader.biomeGroupLoader.saveObject;
		ImmutableMapGroupsBuilder<BiomeGroupBuilder> biomeGroups = new ImmutableMapGroupsBuilder<BiomeGroupBuilder>(key);
		if (savedStats.getConfigNameToAttributeGroups().isPresent()) {
			for (TreeMap<String, BiomeGroupBuilder> entries : savedStats.getConfigNameToAttributeGroups().get()
					.values()) {
				for (BiomeGroupBuilder biomeGroup : entries.values()) {
					if (!"".equals(biomeGroup.getGroupID())) {
						biomeGroups.addGroup(biomeGroup);
					}
				}
			}
		}

		/* Create new BiomeGroup for each new Mapping */
		for (String newMapping : biomeMappings.newMappings()) {
			BiomeGroupBuilder builder = new BiomeGroupBuilder(newMapping);
			builder.setContents("Builder().A(" + newMapping + ")");
			biomeGroups.addGroup(builder);
		}

		ExpressionContext context = new ExpressionContext(biomeMappings, dictionary, attributes);
		ListMultimap<String, String> packgNameToBGIDsBuilder = ArrayListMultimap.create();
		ImmutableMapGroupsBuilder<BiomeGroup> biomeGroupBuilder = new ImmutableMapGroupsBuilder<BiomeGroup>(key);
		for (BiomeGroupBuilder biomeGroup : biomeGroups.iDToGroup().values()) {
			Group.Parser.parseGroupContents(biomeGroup, context);
			biomeGroupBuilder.addGroup(biomeGroup.build());
			for (String pckgName : biomeGroup.results()) {
				packgNameToBGIDsBuilder.get(pckgName).add(biomeGroup.iD());
			}
		}

		iDToGroup = biomeGroupBuilder.build();
		mappingToGroupID = ImmutableListMultimap.<String, String> builder().putAll(packgNameToBGIDsBuilder).build();
	}

}
