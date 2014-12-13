package jas.refactor.biome;

import jas.common.spawner.biome.group.BiomeHelper;
import jas.refactor.ConfigLoader.BiomeGroupLoader;
import jas.refactor.ConfigLoader.ConfigLoader;
import jas.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.refactor.entities.Group;
import jas.refactor.entities.Group.Groups;
import jas.refactor.entities.Group.ReversibleGroups;
import jas.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

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
		} else {
			/* Get Default Groups From BiomeDictionary */
			for (Type type : BiomeDictionary.Type.values()) {
				BiomeGroupBuilder attributeGroup = new BiomeGroupBuilder(type.toString());
				for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) {
					attributeGroup.contents().add(biomeMappings.keyToMapping().get(BiomeHelper.getPackageName(biome)));
				}
				attributeGroups.addGroup(attributeGroup);
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
