package jas.spawner.refactor.biome;

import jas.spawner.refactor.ConfigLoader.BiomeGroupLoader;
import jas.spawner.refactor.ConfigLoader.ConfigLoader;
import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
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

public class BiomeGroups implements ReversibleGroups {
	private ImmutableMap<String, BiomeGroup> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;
	public static String key = "G|";
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

	public BiomeGroups(ConfigLoader loader, BiomeMappings biomeMappings) {
		loadFromConfig(loader, biomeMappings);
	}

	private void loadFromConfig(ConfigLoader loader, BiomeMappings biomeMappings) {
		BiomeGroupLoader savedStats = loader.biomeGroupLoader.saveObject;
		ImmutableMapGroupsBuilder<BiomeGroupBuilder> attributeGroups = new ImmutableMapGroupsBuilder<BiomeGroupBuilder>(
				key);
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

		ImmutableMapGroupsBuilder<BiomeGroup> attributeBuilder = new ImmutableMapGroupsBuilder<BiomeGroup>(key);
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
