package jas.refactor.biome;

import jas.common.spawner.biome.group.BiomeHelper;
import jas.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.refactor.entities.Group;
import jas.refactor.entities.Group.Groups;
import jas.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.List;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class BiomeDictionaryGroups implements Groups {
	private ImmutableMap<String, BiomeGroup> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;

	@Override
	public String key() {
		return "D|";
	}

	@Override
	public Map<String, BiomeGroup> iDToGroup() {
		return iDToGroup;
	}

	public BiomeDictionaryGroups(BiomeMappings biomeMappings) {
		loadFromConfig(biomeMappings);
	}

	private void loadFromConfig(BiomeMappings biomeMappings) {
		ImmutableMapGroupsBuilder<BiomeGroupBuilder> attributeGroups = new ImmutableMapGroupsBuilder<BiomeGroupBuilder>(
				"D|");

		/* Define Group for each BiomeDictionary Type */
		for (Type type : BiomeDictionary.Type.values()) {
			BiomeGroupBuilder attributeGroup = new BiomeGroupBuilder(type.toString());
			for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) {
				attributeGroup.contents().add(biomeMappings.keyToMapping().get(BiomeHelper.getPackageName(biome)));
			}
			attributeGroups.addGroup(attributeGroup);
		}

		List<BiomeGroupBuilder> sortedAttributes = Group.Sorter.getSortedGroups(attributeGroups);
		ListMultimap<String, String> mappingToGroupIDBuilder = ArrayListMultimap.create();
		attributeGroups.clear();

		ImmutableMapGroupsBuilder<BiomeGroup> attributeBuilder = new ImmutableMapGroupsBuilder<BiomeGroup>("D|");
		for (BiomeGroupBuilder biomeGroup : sortedAttributes) {
			Group.Parser.parseGroupContents(biomeGroup, biomeMappings, attributeGroups);
			attributeGroups.addGroup(biomeGroup);
			attributeBuilder.addGroup(biomeGroup.build());
			for (String pckgName : biomeGroup.results()) {
				mappingToGroupIDBuilder.get(pckgName).add(biomeGroup.iD());
			}
		}

		iDToGroup = attributeBuilder.build();
		mappingToGroupID = ImmutableListMultimap.<String, String> builder().putAll(mappingToGroupIDBuilder).build();
	}
}
