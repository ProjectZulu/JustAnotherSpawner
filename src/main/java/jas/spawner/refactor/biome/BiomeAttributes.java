package jas.spawner.refactor.biome;

import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.configsloader.BiomeSettingsLoader;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.Group.Parser.LocationContext;
import jas.spawner.refactor.entities.Group.ReversibleGroups;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;

public class BiomeAttributes implements ReversibleGroups {
	private ImmutableMap<String, BiomeGroup> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;

	@Override
	public String key() {
		return "A.";
	}

	@Override
	public Map<String, BiomeGroup> iDToGroup() {
		return iDToGroup;
	}

	/** Reverse Look-up Map to Get All Attributes a Particular Biome is In */
	public ImmutableMultimap<String, String> mappingToID() {
		return mappingToGroupID;
	}

	public BiomeAttributes(ConfigLoader loader, BiomeMappings biomeMappings, Groups dictionary) {
		loadFromConfig(loader, biomeMappings, dictionary);
	}

	private void loadFromConfig(ConfigLoader loader, BiomeMappings biomeMappings, Groups dictionary) {
		BiomeSettingsLoader savedStats = loader.biomeGroupLoader.saveObject;
		ImmutableMapGroupsBuilder<BiomeGroupBuilder> attributeGroups = new ImmutableMapGroupsBuilder<BiomeGroupBuilder>(
				"A|");
		if (savedStats.getConfigNameToAttributeGroups().isPresent()) {
			for (TreeMap<String, BiomeGroupBuilder> entries : savedStats.getConfigNameToAttributeGroups().get()
					.values()) {
				for (BiomeGroupBuilder attributeGroup : entries.values()) {
					if (!"".equals(attributeGroup.getGroupID())) {
						attributeGroups.addGroup(attributeGroup);
					}
				}
			}
		}

		LocationContext context = new LocationContext(biomeMappings, dictionary);
		ListMultimap<String, String> packgNameToAttribIDsBuilder = ArrayListMultimap.create();
		ImmutableMapGroupsBuilder<BiomeGroup> attributeBuilder = new ImmutableMapGroupsBuilder<BiomeGroup>("A|");
		for (BiomeGroupBuilder biomeGroup : attributeGroups.iDToGroup().values()) {
			Group.Parser.parseGroupContents(biomeGroup, context);
			attributeBuilder.addGroup(biomeGroup.build());
			for (String pckgName : biomeGroup.results()) {
				packgNameToAttribIDsBuilder.get(pckgName).add(biomeGroup.iD());
			}
		}

		iDToGroup = attributeBuilder.build();
		mappingToGroupID = ImmutableListMultimap.<String, String> builder().putAll(packgNameToAttribIDsBuilder).build();
	}
}
