package jas.spawner.refactor.biome;

import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.configloader.BiomeGroupLoader;
import jas.spawner.refactor.configloader.ConfigLoader;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;
import jas.spawner.refactor.entities.Group.ReversibleGroups;

import java.util.List;
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
		return "A|";
	}

	@Override
	public Map<String, BiomeGroup> iDToGroup() {
		return iDToGroup;
	}

	/** Reverse Look-up Map to Get All Attributes a Particular Biome is In */
	public ImmutableMultimap<String, String> mappingToID() {
		return mappingToGroupID;
	}

	public BiomeAttributes(ConfigLoader loader, BiomeMappings biomeMappings) {
		loadFromConfig(loader, biomeMappings);
	}

	private void loadFromConfig(ConfigLoader loader, BiomeMappings biomeMappings) {
		BiomeGroupLoader savedStats = loader.biomeGroupLoader.saveObject;
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

		List<BiomeGroupBuilder> sortedAttributes = Group.Sorter.getSortedGroups(attributeGroups);
		ListMultimap<String, String> packgNameToAttribIDsBuilder = ArrayListMultimap.create();
		attributeGroups.clear();

		ImmutableMapGroupsBuilder<BiomeGroup> attributeBuilder = new ImmutableMapGroupsBuilder<BiomeGroup>("A|");
		for (BiomeGroupBuilder biomeGroup : sortedAttributes) {
			Group.Parser.parseGroupContents(biomeGroup, biomeMappings, attributeGroups);
			attributeGroups.addGroup(biomeGroup);
			attributeBuilder.addGroup(biomeGroup.build());
			for (String pckgName : biomeGroup.results()) {
				packgNameToAttribIDsBuilder.get(pckgName).add(biomeGroup.iD());
			}
		}

		iDToGroup = attributeBuilder.build();
		mappingToGroupID = ImmutableListMultimap.<String, String> builder().putAll(packgNameToAttribIDsBuilder).build();
	}
}
